package io.github.htools.io.buffer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import io.github.htools.io.DataIn;
import io.github.htools.io.DataOut;
import io.github.htools.io.EOCException;
import io.github.htools.io.HDFSIn;
import io.github.htools.io.struct.StructureData;
import io.github.htools.io.struct.StructureWriter;
import io.github.htools.collection.FastMap;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import static io.github.htools.lib.PrintTools.memoryDump;
import static io.github.htools.lib.PrintTools.sprintf;
import io.github.htools.type.Long128;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * This is a general class to read and write binary data to an in memory buffer
 * that can optionally be connected to an input stream.
 * <p>
 * @author jbpvuurens
 */
public class BufferReaderWriter implements StructureData {

    public static Log log = new Log(BufferReaderWriter.class);
    private static Gson gson = new Gson();
    static final int[] CIntArrayLength = initClongArrayLength();

    public static int decodeVIntSize(byte value) {
        if (value >= -112) {
            return 1;
        } else if (value < -120) {
            return -119 - value;
        }
        return -111 - value;
    }

    public static boolean isNegativeVInt(byte value) {
        return value < -120 || (value >= -112 && value < 0);
    }

    public static int[] initClongArrayLength() {
        int a[] = new int[256];
        for (int i = 0; i < 256; i++) {
            int total = 4;
            for (int s = 0; s < 4; s++) {
                int m = (i >> ((s) * 2)) & 3;
                total += m;
            }
            a[i] = total;
        }
        return a;
    }
    public EOCException eof;
    public byte[] buffer;
    public final int DEFAULTBUFFERSIZE = 4096;
    private int requestedbuffersize = -1;
    public long offset = 0;
    public long ceiling = Long.MAX_VALUE;
    public boolean hasmore = true;
    public int bufferpos = 0;
    public int end = 0;
    public DataIn datain = null;
    public DataOut dataout = null;

    public BufferReaderWriter() {
        buffer = new byte[getRequestedBufferSize()];
    }

    public BufferReaderWriter(byte buffer[]) {
        setBuffer(buffer);
    }

    public BufferReaderWriter(DataInput in) {
        readBuffer(in);
    }

    public BufferReaderWriter(BufferReaderWriter in) {
        setBuffer(in.readByteArray());
    }

    public BufferReaderWriter(BufferDelayedWriter out) {
        setBuffer(out.getBytes());
    }

    public BufferReaderWriter(DataIn in) {
        this();
        setDataIn(in);
        resetOffset();
    }

    public BufferReaderWriter(DataOut out) {
        this();
        dataout = out;
        out.setBuffer(this);
    }

    public String toString() {
        return PrintTools.sprintf("Buffer( offset %d ceiling %d pos %d end %d hasmore %b)", offset, ceiling, bufferpos, end, hasmore);
    }

    public final int getRequestedBufferSize() {
        return requestedbuffersize > -1 ? requestedbuffersize : DEFAULTBUFFERSIZE;
    }

    public final boolean hasRequestedBufferSize() {
        return requestedbuffersize > -1;
    }

    @Override
    public void writeBuffer(DataOutput out) {
        try {
            byte b[] = new byte[bufferpos];
            System.arraycopy(buffer, 0, b, 0, bufferpos);
            out.writeInt(b.length);
            out.write(b);
        } catch (IOException ex) {
            log.exception(ex, "writeBuffer( %s ) when writing bytes", out);
        }
    }

    @Override
    public void writeBuffer(StructureWriter writer) {
        byte b[] = new byte[bufferpos];
        System.arraycopy(buffer, 0, b, 0, bufferpos);
        writer.write(b);
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void readBuffer(DataInput in) {
        try {
            int buffersize = in.readInt();
            byte b[] = new byte[buffersize];
            in.readFully(b);
            setBuffer(b);
        } catch (IOException ex) {
            log.exception(ex, "readBuffer( %s ) when reading bytes", in);
        }
    }

    @Override
    public final void setDataIn(DataIn in) {
        if (buffer == null) {
            initBuffer(new byte[this.getRequestedBufferSize()]);
        }
        datain = in;
        in.setBuffer(this);
    }

    public final void setDataOut(DataOut out) {
        if (buffer == null) {
            initBuffer(new byte[this.getRequestedBufferSize()]);
        }
        dataout = out;
        if (out != null) {
            out.setBuffer(this);
        }
        bufferpos = 0;
        setEnd(0);
    }

    public final void setBuffer(byte buffer[]) {
        this.buffer = buffer;
        bufferpos = 0;
        offset = 0;
        if (buffer != null) {
            setEnd(buffer.length);
            if (buffer.length > 0) {
                requestedbuffersize = buffer.length;
            }
        }
    }

    public final void setBuffer(byte buffer[], int pos, int end) {
        //log.info("setBuffer() pos %d end %d", pos, end);
        this.buffer = buffer;
        bufferpos = pos;
        setEnd(end);
    }

    private void initBuffer(byte buffer[]) {
        //log.info("setBuffer() pos %d end %d", pos, end);
        this.buffer = buffer;
        bufferpos = 0;
        offset = 0;
        setEnd(0);
    }

    public final void resetOffset() {
        ceiling = Long.MAX_VALUE;
        offset = bufferpos = 0;
        setEnd(0);
        eof = null;
        hasmore = true;
    }

    public void checkFlush(int size) {
        if (size > buffer.length) {
            setBufferSize(size);
        } else if (bufferpos + size > buffer.length) {
            flushBuffer();
        }
    }

    public void checkIn(int size) throws EOCException {
        //log.info("checkIn( %d ) pos %d end %d bufferlength %d", size, bufferpos, end, buffer.length);
        while (size > end - bufferpos) {
            int inBuffer = end - bufferpos;
            if (size > buffer.length) {
                requestedbuffersize = size;
            }
            fillBuffer();
            if (inBuffer == end - bufferpos) {
                //log.info("checkin exception pos %d end %d", bufferpos, end);
                throw new EOCException("EOF reached");
            }
        }
    }

    @Override
    public void fillBuffer() throws EOCException {
        log.info("fillBuffer datain %s hasmore %b pos %d end %d buffersize %d", datain, hasmore, bufferpos, end, buffer.length);
        if (datain != null) {
            if (!hasmore || !softFillBuffer()) {
                throw getEOF();
            }
        } else {
            hasmore = false;
            //log.info("fillBuffer datain is null pos %d end %d", bufferpos, end);
            throw getEOF();
        }
    }

    public EOCException getEOF() {
        if (eof == null) {
            eof = new EOCException("");
        }
        return eof;
    }

    public boolean softFillBuffer() throws EOCException {
        //log.info("softFillBuffer datain %s hasmore %b pos %d end %d", datain, hasmore, bufferpos, end);
        if (datain != null) {
            if (hasmore) {
                try {
                    if (bufferpos != 0) {
                        shift();
                        //log.info("softFillBuffer shifted pos %d end %d", bufferpos, end);
                    }
                    datain.fillBuffer(this);
                    return true;
                } catch (EOCException ex) {
                    //log.info("softFillBuffer exception");
                    hasmore = false;
                    this.eof = ex;
                }
            }
        }
        return false;
    }

    public int readSpace() throws EOCException {
        int maxread = buffer.length - end;
        if (maxread > ceiling - offset - end) {
            maxread = (int) (ceiling - offset - end);
        }
        if (maxread == 0) {
            if (maxread < ceiling - offset - end) {
                if (this.doublesize()) {
                    maxread = buffer.length - end;
                } else {
                    log.info("warning requested read space exceeds max 100M at %d", offset);
                }
            } else {
                // set maxread beyond ceiling with the hasmore flag to false
                // which allows reading past ceiling
                maxread = buffer.length - end;
                hasmore = false;
            }
        }
        return maxread;
    }

    private boolean doublesize() {
        if (buffer.length < 100000000 || buffer.length < requestedbuffersize) {
            resize(Math.max(buffer.length, this.requestedbuffersize) * 2);
            return true;
        }
        return false;
    }

    public void flushBuffer() {
        dataout.flushBuffer(this);
    }

    public void setAppendOffset(long offset) {
        this.offset = offset;
        this.bufferpos = 0;
        setEnd(0);
    }

    @Override
    public void reuseBuffer() throws IOException {
        setOffset(offset);
        hasmore = true;
    }

    @Override
    public void setOffset(long offset) throws IOException {
        //log.info("setOffset off %d end %d pos %d newoff %d", this.offset, end, bufferpos, offset);
        if (offset >= this.offset && offset < this.offset + end) {
            bufferpos = (int) (offset - this.offset);
            if (datain != null) {
                //shift();
            }
        } else {
            if (offset < this.offset && datain != null) {
                datain.mustMoveBack();
            }
            this.offset = offset;
            this.bufferpos = 0;
            setEnd(0);
        }
        //log.info("new off %d end %d pos %d", this.offset, end, bufferpos);
        hasmore = true;
        eof = null;
    }

    @Override
    public void skip(int bytes) {
        if (this.bufferpos + bytes <= this.end) {
            bufferpos += bytes;
        } else if (bufferpos == end) {
            offset += bytes;
        } else {
            offset += (bytes - (end - bufferpos));
            bufferpos = end;
        }
    }

    public void discard() {
        bufferpos = 0;
        setEnd(0);
    }

    /**
     * The requested buffer size may not be used until the buffer is refilled.
     *
     * @param buffersize
     */
    public void setBufferSize(int buffersize) {
        //log.info("setBufferSize( %d ) currentlength %d dataout %s", buffersize, buffer.length, dataout);
        if (buffersize != requestedbuffersize) {
            requestedbuffersize = buffersize;
            if (buffer != null && getRequestedBufferSize() != buffer.length && bufferpos > 0 && dataout != null) {
                flushBuffer();
            }
            resize(getRequestedBufferSize());
        }
    }

    public int getBufferSize() {
        return buffer.length;
    }

    public void closeWrite() {
        if (dataout != null) {
            //log.info("close offset %d bufferpos %d bufferend %d", offset, bufferpos, end);
            if (buffer != null) {
                flushBuffer();
            }
            dataout.close();
            setBuffer(null);
        }
    }

    @Override
    public void closeRead() {
        if (datain != null) {
            datain.close();
        }
        setBuffer(null);
    }

    @Override
    public boolean hasMore() {
        //log.info("hasMore() hasmore %b offset %d bufferpos %d end %d ceiling %d", hasmore, offset, bufferpos, end, ceiling);
        return (hasmore && (bufferpos < end || offset + bufferpos < ceiling));
    }

    public int readBytes(long offset, byte b[], int pos, int length) {
        if (offset == this.offset + this.bufferpos) {
            if (offset + length <= this.offset + this.end) {
                System.arraycopy(buffer, bufferpos, b, pos, length);
                return length;
            } else {
                int read = datain.readBytes(offset, b, pos, length);
                if (read == 0) {
                    //log.info("EOF reached (%d)", offset);
                    setCeiling(getOffset());
                }
                this.offset = offset + read;
                bufferpos = 0;
                setEnd(read);
                return read;
            }
        } else if (offset > this.offset && offset + length <= this.offset + this.end) {
            System.arraycopy(buffer, (int) (offset - this.offset), b, pos, length);
            return length;
        }
        return datain.readBytes(offset, b, pos, length);
    }

    public long getOffset() {
        return this.offset + this.bufferpos;
    }

    public long getCeiling() {
        return ceiling;
    }

    public void setCeiling(long ceiling) {
        this.ceiling = ceiling;
        if (end > ceiling - offset && ceiling - offset >= 0) {
            setEnd((int) (ceiling - offset));
        }
        if (buffer != null && end > buffer.length - bufferpos) {
            setEnd(buffer.length - bufferpos);
        }
        //log.info("end %d", end);
    }

    public long getLength() {
        try {
            return datain.getLength();
        } catch (IOException ex) {
            log.fatalexception(ex, "getLength() %s", datain);
            return -1;
        }
    }

    public int resize(int buffersize) {
        if (buffer == null) {
            buffer = new byte[buffersize];
        } else if (buffer.length != buffersize) {
            int shift = bufferpos;
            int usedbuffersize = (buffersize >= end - bufferpos) ? buffersize : end - bufferpos;
            byte newbuffer[] = new byte[usedbuffersize];
            for (int i = bufferpos; i < end; i++) {
                newbuffer[i - bufferpos] = buffer[i];
            }
            setEnd(end - bufferpos);
            bufferpos = 0;
            buffer = newbuffer;
            //log.info("resize() size %d end %d", buffer.length, end);
            this.offset += shift;
            return shift;
        }
        return 0;
    }

    public int shift() {
        if (bufferpos == 0) {
            if (getRequestedBufferSize() != buffer.length) {
                return resize(getRequestedBufferSize());
            }
            return 0;
        }
        if (getRequestedBufferSize() != buffer.length) {
            return resize(getRequestedBufferSize());
        } else {
            int shift = bufferpos;
            for (int i = shift; i < end; i++) {
                buffer[i - shift] = buffer[i];
            }
            setEnd(end - bufferpos);
            bufferpos = 0;
            offset += shift;
            return shift;
        }
    }

    public void print(String s) {
        if (s != null) {
            byte b[] = ByteTools.toBytes(s);
            writeRaw(b);
        }
    }

    public void write0(String s) {
        if (s != null) {
            writeRaw(ByteTools.toBytes(s));
        }
        write((byte) 0);
    }

    public int readInt() throws EOCException {
        //log.info("readInt() bufferpos %d", bufferpos);
        checkIn(4);
        int ch1 = buffer[bufferpos++] & 0xFF;
        int ch2 = buffer[bufferpos++] & 0xFF;
        int ch3 = buffer[bufferpos++] & 0xFF;
        int ch4 = buffer[bufferpos++] & 0xFF;
        int result = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
        return result;
    }

    public int readInt2() throws EOCException {
        //log.info("readInt() bufferpos %d", bufferpos);
        checkIn(2);
        int ch1 = buffer[bufferpos++] & 0xFF;
        int ch2 = buffer[bufferpos++] & 0xFF;
        int result = (ch1 << 8) + ch2;
        return result;
    }

    public int readInt3() throws EOCException {
        //log.info("readInt() bufferpos %d", bufferpos);
        checkIn(3);
        int ch1 = buffer[bufferpos++] & 0xFF;
        int ch2 = buffer[bufferpos++] & 0xFF;
        int ch3 = buffer[bufferpos++] & 0xFF;
        int result = ((ch1 << 16) + (ch2 << 8) + (ch3));
        return result;
    }

    public void skipInt() {
        skip(4);
    }

    public int readShort() throws EOCException {
        checkIn(2);
        int ch1 = buffer[bufferpos++] & 0xFF;
        int ch2 = buffer[bufferpos++] & 0xFF;
        int result = ((ch1 << 8) + (ch2));
        return result;
    }

    public void skipShort() {
        skip(2);
    }

    public int readUShort() throws EOCException {
        checkIn(2);
        int ch1 = buffer[bufferpos++] & 0xFF;
        int ch2 = buffer[bufferpos++] & 0xFF;
        int result = (int) ((ch1 << 8) + (ch2));
        return result;
    }

    public void skipUShort() {
        skip(2);
    }

    public double readDouble() throws EOCException {
        long l = readLong();
        return Double.longBitsToDouble(l);
    }

    public void skipDouble() {
        skip(8);
    }

    public long readLong() throws EOCException {
        checkIn(8);
        long ch1 = buffer[bufferpos++] & 0xFF;
        long ch2 = buffer[bufferpos++] & 0xFF;
        long ch3 = buffer[bufferpos++] & 0xFF;
        long ch4 = buffer[bufferpos++] & 0xFF;
        long ch5 = buffer[bufferpos++] & 0xFF;
        long ch6 = buffer[bufferpos++] & 0xFF;
        long ch7 = buffer[bufferpos++] & 0xFF;
        long ch8 = buffer[bufferpos++] & 0xFF;
        long result = ((ch1 << 56) + (ch2 << 48) + (ch3 << 40) + (ch4 << 32)
                + (ch5 << 24) + (ch6 << 16) + (ch7 << 8) + (ch8));
        return result;
    }

    public Long128 readLong128() throws EOCException {
        Long128 result = new Long128();
        result.read(this);
        return result;
    }

    public void skipLong() {
        skip(8);
    }

    public int readByte() throws EOCException {
        checkIn(1);
        return (buffer[bufferpos++] & 0xFF);
    }

    public boolean readBoolean() throws EOCException {
        checkIn(1);
        return (buffer[bufferpos++] == 0) ? false : true;
    }

    public void skipByte() {
        skip(1);
    }

    public String readString() throws EOCException {
        int length = readInt();
        return readString(length);
    }

    public JsonObject readJson() throws EOCException {
        String s = readString();
        return s == null ? null : gson.fromJson(s, JsonObject.class);
    }

    public JsonObject readJson0() throws EOCException {
        String s = readString0();
        return s == null ? null : gson.fromJson(s, JsonObject.class);
    }

    public StringBuilder readStringBuilder() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return null;
        }
        return new StringBuilder(readString(length));
    }

    public void skipString() throws EOCException {
        int length = readInt();
        skip(length);
    }

    public void skipJson() throws EOCException {
        skipString();
    }

    public void skipJson0() throws EOCException {
        skipString0();
    }

    public void skipStringBuilder() throws EOCException {
        skipString();
    }

    public byte[] readBytes(int length) throws EOCException {
        if (length > 100000000) {
            log.info("very long data length=%d offset=%d\n%s", length, this.getOffset(), memoryDump(buffer));
        }
        if (length < 0) {
            return null;
        }
        byte b[] = new byte[length];
        readBytes(b, 0, length);
        return b;
    }

    public byte[] readByteArray() throws EOCException {
        int length = readInt();
        //log.info("readByteArray %d", length);
        return readBytes(length);
    }

    @Override
    public boolean[] readBoolArray() throws EOCException {
        int length = readInt();
        checkIn(length);
        boolean[] b = new boolean[length];
        for (int i = 0; i < length; i++)
            b[i] = (buffer[bufferpos++] == 0) ? false : true;
        return b;
    }

    public void skipByteBlock() throws EOCException {
        int length = readInt();
        if (length > 0) {
            skip(length);
        }
    }

    public void readBytes(byte b[], int offset, int length) throws EOCException {
        if (length > buffer.length) {
            int inbuffer = end - bufferpos;
            System.arraycopy(buffer, bufferpos, b, offset, inbuffer);
            length -= inbuffer;
            offset += inbuffer;
            this.offset += bufferpos + inbuffer;
            bufferpos = 0;
            setEnd(0);
            while (length > 0) {
                int read = datain.readBytes(this.offset, b, offset, length);
                if (read == 0) {
                    log.info("readBytes(%d %d): EOF reached when reading fixed number of bytes read %d", offset, length, read);
                    if (datain instanceof HDFSIn) {
                        log.info("in file %s", ((HDFSIn) datain).path.toString());
                    }
                    log.crash();
                }
                this.offset += read;
                offset += read;
                length -= read;
            }
        } else {
            //log.info("readBytes length %d bpos %d bend %d blength %d", length, bufferpos, end, buffer.length);
            checkIn(length);
            if (bufferpos <= end - length) {
                length += offset;
                while (offset < length) {
                    b[offset++] = buffer[bufferpos++];
                }
            }
        }
    }

    public String readString(int length) throws EOCException {
        if (length > -1) {
            return ByteTools.toString(readBytes(length));
        }
        return null;
    }

    public String readString0() throws EOCException {
        checkIn(1);
        int strend = bufferpos - 1;
        StringBuilder sb = new StringBuilder();
        do {
            if (++strend >= end) {
                sb.append(ByteTools.toString(buffer, bufferpos, strend));
                bufferpos = strend;
                fillBuffer();
                strend = 0;
            }
        } while (buffer[strend] != 0);
        String s = ByteTools.toString(buffer, bufferpos, strend);
        bufferpos = strend + 1;
        if (sb.length() > 0) {
            return sb.append(s).toString();
        }
        return s;
    }

    public void skipString0() throws EOCException {
        bufferpos -= 1;
        do {
            if (++bufferpos >= end) {
                fillBuffer();
            }
        } while (buffer[bufferpos] != 0);
        bufferpos++;
    }

    public String[] readStringArray() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return null;
        }
        String array[] = new String[length];
        for (int i = 0; i < length; i++) {
            array[i] = readString();
        }
        return array;
    }

    public void skipStringArray() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return;
        }
        for (int i = 0; i < length; i++) {
            skipString();
        }
    }

    public long[] readLongArray() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return null;
        }
        long array[] = new long[length];
        for (int i = 0; i < length; i++) {
            array[i] = readLong();
        }
        return array;
    }

    public void skipLongArray() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return;
        }
        skip(8 * length);
    }

    public double[] readDoubleArray() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return null;
        }
        double array[] = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = readDouble();
        }
        return array;
    }

    public void skipDoubleArray() throws EOCException {
        skipLongArray();
    }

    public long[][] readCLongArray2() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        if (length == 0) {
            return new long[0][];
        }
        long array[][] = new long[length][];
        for (int i = 0; i < length; i++) {
            array[i] = readCLongArray();
        }
        return array;
    }

    public void skipCLongArray2() throws EOCException {
        int length = readInt();
        if (length < 1) {
            return;
        }
        for (int i = 0; i < length; i++) {
            skipCLongArray();
        }
    }

    public int[] readIntArray() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return null;
        }
        int array[] = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = readInt();
        }
        return array;
    }

    public void skipIntArray() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return;
        }
        skip(4 * length);
    }

    public void skipBoolArray() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return;
        }
        skip(length);
    }

    public int[][] readSquaredIntArray2() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        if (length == 0) {
            return new int[0][];
        }
        int length2 = readCInt();
        int array[][] = new int[length][length2];
        int input[] = readCIntArray();
        int p = 0;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length2; j++) {
                array[i][j] = input[p++];
            }
        }
        return array;
    }

    public int[][][] readSquaredIntArray3() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        if (length == 0) {
            return new int[0][][];
        }
        int length2 = readCInt();
        int length3 = readCInt();
        int array[][][] = new int[length][length2][length3];
        int input[] = readCIntArray();
        int p = 0;
        for (int d1 = 0; d1 < length; d1++) {
            for (int d2 = 0; d2 < length2; d2++) {
                for (int d3 = 0; d3 < length3; d3++) {
                    array[d1][d2][d3] = input[p++];
                }
            }
        }
        return array;
    }

    public int[][][] readCIntArray3() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        int array[][][] = new int[length][][];
        for (int i = 0; i < length; i++) {
            array[i] = readCIntArray2();
        }
        return array;
    }

    public int[][] readCIntArray2() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        int array[][] = new int[length][];
        for (int i = 0; i < length; i++) {
            array[i] = readCIntArray();
        }
        return array;
    }

    public void skipCIntArray2() throws EOCException {
        int length = readCInt();
        for (int i = 0; i < length; i++) {
            skipCIntArray();
        }
    }

    public void skipCIntArray3() throws EOCException {
        int length = readCInt();
        for (int i = 0; i < length; i++) {
            skipSquaredIntArray2();
        }
    }

    public void skipSquaredIntArray2() throws EOCException {
        int length = this.readCInt();
        if (length < 1) {
            return;
        }
        skipCInt();
        skipCIntArray();
    }

    public void skipSquaredIntArray3() throws EOCException {
        int length = this.readCInt();
        if (length < 1) {
            return;
        }
        skipCInt();
        skipCInt();
        skipCIntArray();
    }

    public void write(int i) {
        checkFlush(4);
        buffer[bufferpos++] = (byte) ((i >>> 24) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 16) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 8) & 0xFF);
        buffer[bufferpos++] = (byte) ((i) & 0xFF);
    }

    public void write2(int i) {
        checkFlush(2);
        buffer[bufferpos++] = (byte) ((i >>> 8) & 0xFF);
        buffer[bufferpos++] = (byte) ((i) & 0xFF);
    }

    public void write3(int i) {
        checkFlush(3);
        buffer[bufferpos++] = (byte) ((i >>> 16) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 8) & 0xFF);
        buffer[bufferpos++] = (byte) ((i) & 0xFF);
    }

    public void write(short i) {
        checkFlush(2);
        buffer[bufferpos++] = (byte) ((i >>> 8) & 0xFF);
        buffer[bufferpos++] = (byte) ((i) & 0xFF);
    }

    public void write(double d) {
        checkFlush(8);
        write(Double.doubleToLongBits(d));
    }

    public void write(long i) {
        checkFlush(8);
        buffer[bufferpos++] = (byte) ((i >>> 56) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 48) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 40) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 32) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 24) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 16) & 0xFF);
        buffer[bufferpos++] = (byte) ((i >>> 8) & 0xFF);
        buffer[bufferpos++] = (byte) ((i) & 0xFF);
    }

    public void write(Long128 i) {
        i.write(this);
    }

    public void write(long i[]) {
        if (i == null) {
            write(-1);
        } else {
            write(i.length);
            for (long l : i) {
                write(l);
            }
        }
    }

    public void write(double i[]) {
        if (i == null) {
            write(-1);
        } else {
            write(i.length);
            for (double l : i) {
                write(l);
            }
        }
    }

    @Override
    public void write(int i[]) {
        if (i == null) {
            write(-1);
        } else {
            write(i.length);
            for (int l : i) {
                write(l);
            }
        }
    }

    @Override
    public void write(boolean b[]) {
        if (b == null) {
            write(-1);
        } else {
            write(b.length);
            for (boolean l : b) {
                write(l);
            }
        }
    }

    @Override
    public void writeIntList(Collection<Integer> i) {
        if (i == null) {
            write(-1);
        } else {
            write(i.size());
            for (int l : i) {
                write(l);
            }
        }
    }

    @Override
    public void writeLongList(Collection<Long> i) {
        if (i == null) {
            write(-1);
        } else {
            write(i.size());
            for (Long l : i) {
                write(l);
            }
        }
    }

    public void writeStringList(Collection<String> i) {
        if (i == null) {
            write(-1);
        } else {
            write(i.size());
            for (String l : i) {
                write(l);
            }
        }
    }

    /**
     * Compresses a squared 2-dim array. Note: all rows in the array must have
     * equal lengths!
     *
     * @param array
     */
    public void writeSquared(int array[][]) {
        if (array == null) {
            writeC(-1);
        }
        if (array.length == 0) {
            writeC(0);
        } else {
            writeC(array.length);
            writeC(array[0].length);
            int flat[] = io.github.htools.lib.ArrayTools.flatten(array);
            if (array.length * array[0].length != flat.length) {
                log.fatal("Can only use writeSquared on squared arrays");
            }
            writeC(flat);
        }
    }

    public void writeSquared(int array[][][]) {
        if (array == null) {
            writeC(-1);
        } else if (array.length == 0) {
            writeC(0);
        } else {
            writeC(array.length);
            writeC(array[0].length);
            writeC(array[0][0].length);
            int flat[] = io.github.htools.lib.ArrayTools.flatten(array);
            if (array.length * array[0].length * array[0][0].length != flat.length) {
                log.fatal("Can only use writeSquared on squared arrays");
            }
            writeC(flat);

        }
    }

    public void writeC(int array[][]) {
        if (array == null) {
            writeC(-1);
        } else {
            writeC(array.length);
            for (int a[] : array) {
                writeC(a);
            }
        }
    }

    public void writeC(int array[][][]) {
        if (array == null) {
            writeC(-1);
        } else {
            writeC(array.length);
            for (int a[][] : array) {
                writeC(a);
            }
        }
    }

    public void writeSparse(int array[][][]) {
        if (array == null) {
            writeC(-1);
        } else if (array.length == 0) {
            writeC(0);
        } else {
            writeC(array.length);
            writeC(array[0].length);
            writeC(array[0][0].length);
            writeSparse(io.github.htools.lib.ArrayTools.flatten(array));
        }
    }

    public void writeSparse(int array[][]) {
        if (array == null) {
            writeC(-1);
        } else if (array.length == 0) {
            writeC(0);
        } else {
            writeC(array.length);
            writeC(array[0].length);
            writeSparse(io.github.htools.lib.ArrayTools.flatten(array));
        }
    }

    public void writeSparse(long array[][]) {
        if (array == null) {
            writeC(-1);
        } else if (array.length == 0) {
            writeC(0);
        } else {
            writeC(array.length);
            writeC(array[0].length);
            writeSparse(io.github.htools.lib.ArrayTools.flatten(array));
        }
    }

    public void skipIntSparse3() throws EOCException {
        int length = readCInt();
        if (length < 1) {
            return;
        }
        skipCInt();
        skipCInt();
        skipIntSparse();
    }

    public void skipIntSparse2() throws EOCException {
        int length = readCInt();
        if (length < 1) {
            return;
        }
        skipCInt();
        skipIntSparse();
    }

    public void skipLongSparse2() throws EOCException {
        int length = readCInt();
        if (length < 1) {
            return;
        }
        skipCInt();
        skipLongSparse();
    }

    public int[][] readIntSparse2() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        } else if (length == 0) {
            return new int[0][];
        } else {
            int length2 = readCInt();
            int array[][] = new int[length][length2];
            int input[] = readIntSparse();
            int pos = 0;
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length2; j++) {
                    array[i][j] = input[pos++];
                }
            }
            return array;
        }
    }

    public long[][] readLongSparse2() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        } else if (length == 0) {
            return new long[0][];
        } else {
            int length2 = readCInt();
            long array[][] = new long[length][length2];
            long input[] = readLongSparse();
            int pos = 0;
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length2; j++) {
                    array[i][j] = input[pos++];
                }
            }
            return array;
        }
    }

    public int[][][] readIntSparse3() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        } else if (length == 0) {
            return new int[0][][];
        } else {
            int length2 = readCInt();
            int length3 = readCInt();
            int array[][][] = new int[length][length2][length3];
            int input[] = readIntSparse();
            int pos = 0;
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length2; j++) {
                    for (int k = 0; k < length3; k++) {
                        array[i][j][k] = input[pos++];
                    }
                }
            }
            return array;
        }
    }

    @Override
    public void writeC(long array[][]) {
        if (array == null) {
            writeC(-1);
        } else {
            writeC(array.length);
            for (long a[] : array) {
                writeC(a);
            }
        }
    }

    public void write(String i[]) {
        if (i == null) {
            write(-1);
        } else {
            write(i.length);
            for (String l : i) {
                write(l);
            }
        }
    }

    public void writeRaw(byte b[]) {
        if (buffer.length == 0) {
            resize(getRequestedBufferSize());
        }
        for (byte i : b) {
            if (bufferpos >= buffer.length) {
                this.flushBuffer();
            }
            buffer[bufferpos++] = i;
        }
    }

    public void writeRaw(String s) {
        writeRaw(ByteTools.toBytes(s));
    }
    
    public void writeRaw(String s, Object ... params) {
        writeRaw(ByteTools.toBytes(sprintf(s, params)));
    }
    
    public void write(byte b[]) {
        if (b == null) {
            write(-1);
        } else {
            write(b.length);
            writeRaw(b);
        }
    }

//    public void write(byte b[], byte escape) {
//        for (int i = 0; i < b.length; i++) {
//            if (b[i] == escape) {
//                if (bufferpos >= buffer.length) {
//                    this.flushBuffer();
//                }
//                buffer[bufferpos++] = escape;
//            }
//            if (bufferpos >= buffer.length) {
//                this.flushBuffer();
//            }
//            buffer[bufferpos++] = b[i];
//        }
//    }
//
//    public void write(byte b[], byte eof[], byte escape) {
//        if (eof.length == 0) {
//            write(b, escape);
//            return;
//        }
//        for (int i = 0; i < b.length; i++) {
//            if (b[i] == escape || io.github.repir.tools.lib.ByteTools.readMatchingString(b, eof, i)) {
//                if (bufferpos >= buffer.length) {
//                    this.flushBuffer();
//                }
//                buffer[bufferpos++] = escape;
//            }
//            if (bufferpos >= buffer.length) {
//                this.flushBuffer();
//            }
//            buffer[bufferpos++] = b[i];
//        }
//    }
//
//    public void write(byte b[], byte end[], byte end2[], byte escape) {
//        if (end2.length == 0) {
//            write(b, end, escape);
//        } else if (end.length == 0) {
//            write(b, end2, escape);
//        } else {
//            for (int i = 0; i < b.length; i++) {
//                if (b[i] == escape || io.github.repir.tools.lib.ByteTools.readMatchingString(b, end, i) || io.github.repir.tools.lib.ByteTools.readMatchingString(b, end2, i)) {
//                    if (bufferpos >= buffer.length) {
//                        this.flushBuffer();
//                    }
//                    buffer[bufferpos++] = escape;
//                }
//                if (bufferpos >= buffer.length) {
//                    this.flushBuffer();
//                }
//                buffer[bufferpos++] = b[i];
//            }
//        }
//    }
//
//    public void writeWS(byte b[], byte eof[], byte escape) {
//        for (int i = 0; i < b.length; i++) {
//            if (bufferpos >= buffer.length) {
//                this.flushBuffer();
//            }
//            if (io.github.repir.tools.lib.ByteTools.matchStringWS(b, eof, i)) {
//                buffer[bufferpos++] = escape;
//                if (bufferpos >= buffer.length) {
//                    this.flushBuffer();
//                }
//            }
//            buffer[bufferpos++] = b[i];
//        }
//    }
    public void write(byte b[], int offset, int length) {
        length = offset + length;
        while (offset < length) {
            if (bufferpos >= buffer.length) {
                this.flushBuffer();
            }
            buffer[bufferpos++] = b[offset++];
        }
    }

    public void write(byte i) {
        checkFlush(1);
        buffer[bufferpos++] = i;
    }

    public void write(boolean i) {
        checkFlush(1);
        buffer[bufferpos++] = (byte) (i ? -1 : 0);
    }

    public void writeUB(int i) {
        checkFlush(1);
        buffer[bufferpos++] = (byte) (i & 0xFF);
    }

    public void writeChar(int c) {
        checkFlush(2);
        buffer[bufferpos++] = (byte) ((c >>> 8) & 0xFF);
        buffer[bufferpos++] = (byte) ((c >>> 0) & 0xFF);
    }

    public void write(String s) {
        if (s == null) {
            write(-1);
        } else {
            try {
                byte b[] = s.getBytes("UTF-8");
                write(b.length);
                writeRaw(b);
            } catch (UnsupportedEncodingException ex) { }
        }
    }

    public void write(Object s, Type type) {
        if (s == null) {
            write((String) null);
        } else {
            write(gson.toJson(s, type));
        }
    }

    public <T> T read(Type type) {
        String s = this.readString();
        return gson.fromJson(s, type);
    }

    public void write(JsonObject s) {
        write(s == null ? null : s.getAsString());
    }

    public void write0(JsonObject s) {
        write0(s == null ? null : s.getAsString());
    }

    public void write(StringBuilder s) {
        if (s == null) {
            write(-1);
        } else {
            write(s.toString());
        }
    }

    public long readCLong() throws EOCException {
        checkIn(1);
        byte firstByte = buffer[bufferpos++];
        int len = decodeVIntSize(firstByte);
        if (len == 1) {
            return firstByte;
        }
        checkIn(len - 1);
        long i = 0;
        for (int idx = 0; idx < len - 1; idx++) {
            i = i << 8;
            i = i | (buffer[bufferpos++] & 0xFF);
        }
        return (isNegativeVInt(firstByte) ? (i ^ -1L) : i);
    }

    public void skipCLong() throws EOCException {
        checkIn(1);
        byte firstByte = buffer[bufferpos++];
        int len = decodeVIntSize(firstByte);
        skip(len - 1);
    }

    public void writeC(int i) {
        writeC((long) i);
    }

    public int readCInt() throws EOCException {
        return (int) readCLong();
    }

    public void skipCInt() throws EOCException {
        skipCLong();
    }

    public void writeC(long i) {
        if (i >= -112 && i <= 127) {
            write((byte) i);
            return;
        }
        int len = -112;
        if (i < 0) {
            i ^= -1L; // take one's complement'
            len = -120;
        }
        long tmp = i;
        while (tmp != 0) {
            tmp = tmp >> 8;
            len--;
        }
        write((byte) len);
        len = (len < -120) ? -(len + 120) : -(len + 112);
        checkFlush(len);
        for (int idx = len; idx != 0; idx--) {
            int shiftbits = (idx - 1) * 8;
            long mask = 0xFFL << shiftbits;
            buffer[bufferpos++] = (byte) ((i & mask) >> shiftbits);
        }
    }

    public byte longmask(long l) {
        if ((l & 0xFFFFFFFFFF000000l) != 0) {
            return 3;
        }
        if ((l & 0xFF0000) != 0) {
            return 2;
        }
        if ((l & 0xFF00) != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public long[] readCLongArray() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        long l[] = new long[length];
        int mainlength = (length / 4) * 4;
        for (int i = 0; i < mainlength; i += 4) {
            checkIn(1);
            int mask = buffer[bufferpos++];
            for (int s = i; s < i + 4; s++) {
                int m = (mask >> ((s - i) * 2)) & 3;
                if (m < 3) {
                    checkIn(m + 1);
                }
                switch (m) {
                    case 3:
                        l[s] = readCLong();
                        break;
                    case 2:
                        l[s] |= ((buffer[bufferpos++] & 0xFF) << 16);
                    case 1:
                        l[s] |= ((buffer[bufferpos++] & 0xFF) << 8);
                    case 0:
                        l[s] |= (buffer[bufferpos++] & 0xFF);
                }
            }
        }
        for (int i = mainlength; i < l.length; i++) {
            l[i] = readCLong();
        }
        return l;
    }

    public void skipCLongArray() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return;
        }
        int mainlength = (length / 4) * 4;
        for (int i = 0; i < mainlength; i += 4) {
            checkIn(1);
            int mask = buffer[bufferpos++];
            for (int s = i; s < i + 4; s++) {
                int m = (mask >> ((s - i) * 2)) & 3;
                if (m == 3) {
                    skipCLong();
                } else {
                    skip(m + 1);
                }
            }
        }
        for (int i = mainlength; i < length; i++) {
            skipCLong();
        }
    }

    public void writeC(long[] l) {
        if (l == null) {
            writeC(-1);
            return;
        }
        writeC(l.length);
        byte m[] = new byte[4];
        int mainlength = (l.length / 4) * 4;
        for (int i = 0; i < mainlength; i += 4) {
            byte mask = 0;
            for (int s = i; s < i + 4; s++) {
                m[s - i] = longmask(l[s]);
                mask |= (m[s - i] << ((s - i) * 2));
            }
            checkFlush(1);
            buffer[bufferpos++] = mask;
            for (int s = i; s < i + 4; s++) {
                if (m[s - i] < 3) {
                    checkFlush(m[s - i] + 1);
                }
                switch (m[s - i]) {
                    case 3:
                        writeC(l[s]);
                        break;
                    case 2:
                        buffer[bufferpos++] = (byte) ((l[s] >> 16) & 0xFF);
                    case 1:
                        buffer[bufferpos++] = (byte) ((l[s] >> 8) & 0xFF);
                    case 0:
                        buffer[bufferpos++] = (byte) (l[s] & 0xFF);
                }
            }
        }
        for (int i = mainlength; i < l.length; i++) {
            writeC(l[i]);
        }
    }

    public void writeSparse(long l[]) {
        writeSparse(l, 0, (l != null) ? l.length : 0);
    }

    public void writeSparse(long l[], int offset, int length) {
        if (l == null) {
            writeC(-1);
            return;
        }
        writeC(length);
        int end = offset + length;
        int mainlength = 0;
        for (int i = offset; i < end; i++) {
            if (l[i] != 0) {
                mainlength++;
            }
        }
        int leap[] = new int[mainlength];
        long ltf[] = new long[mainlength];
        int last = 0, pos = 0;
        for (int i = offset; i < end; i++) {
            if (l[i] > 0) {
                leap[pos] = i - last;
                ltf[pos++] = l[i];
                last = i + 1;
            }
        }
        writeC(leap);
        writeC(ltf);
    }

    public void writeSparse(int l[]) {
        writeSparse(l, 0, (l != null) ? l.length : 0);
    }

    public void writeSparse(int l[], int offset, int length) {
        if (l == null) {
            writeC(-1);
            return;
        }
        writeC(length);
        int end = offset + length;
        int mainlength = 0;
        for (int i = offset; i < end; i++) {
            if (l[i] != 0) {
                mainlength++;
            }
        }
        int leap[] = new int[mainlength];
        int ltf[] = new int[mainlength];
        int last = 0, pos = 0;
        for (int i = offset; i < end; i++) {
            if (l[i] > 0) {
                leap[pos] = i - last;
                ltf[pos++] = l[i];
                last = i + 1;
            }
        }
        writeC(leap);
        writeC(ltf);
    }

    public void writeSparse(double l[]) {
        writeSparse(l, 0, (l != null) ? l.length : 0);
    }

    public void writeSparse(double l[], int offset, int length) {
        if (l == null) {
            writeC(-1);
            return;
        }
        writeC(length);
        int end = offset + length;
        int mainlength = 0;
        for (int i = offset; i < end; i++) {
            if (l[i] != 0) {
                mainlength++;
            }
        }
        int leap[] = new int[mainlength];
        double ltf[] = new double[mainlength];
        int last = 0, pos = 0;
        for (int i = offset; i < end; i++) {
            if (l[i] > 0) {
                leap[pos] = i - last;
                ltf[pos++] = l[i];
                last = i + 1;
            }
        }
        writeC(leap);
        write(ltf);
    }

    public void writeSparseLong(Map<Integer, Long> table) {
        if (table == null) {
            writeC(-1);
            return;
        }
        TreeSet<Integer> keys = new TreeSet<Integer>(table.keySet());
        if (keys.size() > 0) {
            writeC(keys.last() + 1);
            int mainlength = 0;
            for (long value : table.values()) {
                if (value > 0) {
                    mainlength++;
                }
            }
            int leap[] = new int[mainlength];
            long ltf[] = new long[mainlength];
            int last = 0, pos = 0;
            for (int key : keys) {
                long value = table.get(key);
                if (value > 0) {
                    leap[pos] = key - last;
                    ltf[pos++] = value;
                    last = key + 1;
                }
            }
            writeC(leap);
            writeC(ltf);
        } else {
            writeC(0);
            writeC(0);
            writeC(0);
        }
    }

    public void writeSparseInt(Map<Integer, Integer> table) {
        if (table == null) {
            writeC(-1);
            return;
        }
        TreeSet<Integer> keys = new TreeSet<Integer>(table.keySet());
        if (keys.size() > 0) {
            writeC(keys.last() + 1);
            int mainlength = 0;
            for (int value : table.values()) {
                if (value > 0) {
                    mainlength++;
                }
            }
            int leap[] = new int[mainlength];
            int ltf[] = new int[mainlength];
            int last = 0, pos = 0;
            for (int key : keys) {
                int value = table.get(key);
                if (value > 0) {
                    leap[pos] = key - last;
                    ltf[pos++] = value;
                    last = key + 1;
                }
            }
            writeC(leap);
            writeC(ltf);
        } else {
            writeC(0);
            writeC(0);
            writeC(0);
        }
    }

    public long[] readLongSparse() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        int leap[] = readCIntArray();
        long value[] = readCLongArray();
        long l[] = new long[length];
        int last = 0;
        for (int i = 0; i < leap.length; i++) {
            //log.info("%d", value[i]);
            last += leap[i];
            l[last++] = value[i];
        }
        return l;
    }

    public void skipLongSparse() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return;
        }
        skipCIntArray();
        skipCLongArray();
    }

    public double[] readDoubleSparse() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        int leap[] = readCIntArray();
        double value[] = readDoubleArray();
        double l[] = new double[length];
        int last = 0;
        for (int i = 0; i < leap.length; i++) {
            //log.info("%d", value[i]);
            last += leap[i];
            l[last++] = value[i];
        }
        return l;
    }

    public void skipDoubleSparse() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return;
        }
        skipCIntArray();
        skipDoubleArray();
    }

    public int[] readIntSparse() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        int leap[] = readCIntArray();
        int value[] = readCIntArray();
        int l[] = new int[length];
        int last = 0;
        for (int i = 0; i < leap.length; i++) {
            //log.info("%d", value[i]);
            last += leap[i];
            l[last++] = value[i];
        }
        return l;
    }

    public void skipIntSparse() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return;
        }
        skipCIntArray();
        skipCIntArray();
    }

    public HashMap<Integer, Long> readSparseLongMap() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        HashMap<Integer, Long> table = new HashMap<Integer, Long>(length);
        int leap[] = readCIntArray();
        long value[] = readCLongArray();
        int last = 0;
        for (int i = 0; i < leap.length; i++) {
            last += leap[i];
            table.put(last++, value[i]);
        }
        return table;
    }

    public HashMap<Integer, Integer> readSparseIntMap() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        HashMap<Integer, Integer> table = new HashMap<Integer, Integer>(length);
        int leap[] = readCIntArray();
        int value[] = readCIntArray();
        int last = 0;
        for (int i = 0; i < leap.length; i++) {
            last += leap[i];
            table.put(last++, value[i]);
        }
        return table;
    }

    public void writeIncr(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            array[i] -= array[i - 1];
        }
        writeC(array);
    }

    public void writeIncr(ArrayList<Integer> list) {
        Integer arrayI[] = list.toArray(new Integer[list.size()]);
        int arrayi[] = new int[arrayI.length];
        for (int i = arrayI.length - 1; i > 0; i--) {
            arrayi[i] = arrayI[i] - arrayI[i - 1];
        }
        arrayi[0] = arrayI[0];
        writeC(arrayi);
    }

    public void writeC(int[] l) {
        if (l == null) {
            writeC(-1);
            return;
        }
        writeC(l.length);
        int length = (l.length / 4) * 4;
        byte m[] = new byte[4];
        for (int i = 0; i < length; i += 4) {
            byte mask = 0;
            for (int s = i; s < i + 4; s++) {
                m[s - i] = longmask(l[s]);
                mask |= (m[s - i] << ((s - i) * 2));
                //if (l[s] == 2554) {
                //   log.info("m[%d]=%d mask %d bufferpos %d", s-i, m[s-i], mask, i);
                //}
            }
            checkFlush(1);
            buffer[bufferpos++] = mask;
            for (int s = i; s < i + 4; s++) {
                checkFlush(m[s - i] + 1);
                switch (m[s - i]) {
                    case 3:
                        buffer[bufferpos++] = (byte) ((l[s] >>> 24) & 0xFF);
                    case 2:
                        buffer[bufferpos++] = (byte) ((l[s] >>> 16) & 0xFF);
                    case 1:
                        buffer[bufferpos++] = (byte) ((l[s] >>> 8) & 0xFF);
                    case 0:
                        buffer[bufferpos++] = (byte) (l[s] & 0xFF);
                }
            }
        }
        for (int i = length; i < l.length; i++) {
            writeC(l[i]);
        }
    }

    public void writeC(ArrayList<Integer> l) {
        if (l == null) {
            writeC(-1);
            return;
        }
        writeC(l.size());
        int length = (l.size() / 4) * 4;
        byte m[] = new byte[4];
        int v[] = new int[4];
        for (int i = 0; i < length; i += 4) {
            byte mask = 0;
            v[0] = l.get(i);
            v[1] = l.get(i + 1);
            v[2] = l.get(i + 2);
            v[3] = l.get(i + 3);
            m[0] = longmask(v[0]);
            m[1] = longmask(v[1]);
            m[2] = longmask(v[2]);
            m[3] = longmask(v[3]);
            mask = (byte) (m[0] | (m[1] << 2) | (m[2] << 4) | (m[3] << 6));
            checkFlush(1);
            buffer[bufferpos++] = mask;
            for (int s = 0; s < 4; s++) {
                checkFlush(m[s] + 1);
                switch (m[s]) {
                    case 3:
                        buffer[bufferpos++] = (byte) ((v[s] >>> 24) & 0xFF);
                    case 2:
                        buffer[bufferpos++] = (byte) ((v[s] >>> 16) & 0xFF);
                    case 1:
                        buffer[bufferpos++] = (byte) ((v[s] >>> 8) & 0xFF);
                    case 0:
                        buffer[bufferpos++] = (byte) (v[s] & 0xFF);
                }
            }
        }
        for (int i = length; i < l.size(); i++) {
            writeC(l.get(i));
        }
    }

    public int[] readCIntArray() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        int l[] = new int[length];
        int mainlength = (length / 4) * 4;
        //log.info("mainlength %d length %d", mainlength, length);
        for (int i = 0; i < mainlength; i += 4) {
            checkIn(1);
            int mask = buffer[bufferpos++];
            for (int s = i; s < i + 4; s++) {
                int m = (mask >> ((s - i) * 2)) & 3;
                checkIn(m + 1);
                switch (m) {
                    case 3:
                        l[s] = ((buffer[bufferpos++] & 0xFF) << 24);
                    case 2:
                        l[s] |= ((buffer[bufferpos++] & 0xFF) << 16);
                    case 1:
                        l[s] |= ((buffer[bufferpos++] & 0xFF) << 8);
                    case 0:
                        l[s] |= (buffer[bufferpos++] & 0xFF);
                }
            }
        }
        for (int i = mainlength; i < l.length; i++) {
            l[i] = readCInt();
        }
        return l;
    }

    public ArrayList<Integer> readCIntArrayList() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return null;
        }
        ArrayList<Integer> l = new ArrayList<Integer>();
        int mainlength = (length / 4) * 4;
        //log.info("mainlength %d length %d", mainlength, length);
        for (int i = 0; i < mainlength; i += 4) {
            checkIn(1);
            int mask = buffer[bufferpos++];
            for (int s = i; s < i + 4; s++) {
                int m = (mask >> ((s - i) * 2)) & 3;
                checkIn(m + 1);
                int value = 0;
                switch (m) {
                    case 3:
                        value = ((buffer[bufferpos++] & 0xFF) << 24);
                    case 2:
                        value |= ((buffer[bufferpos++] & 0xFF) << 16);
                    case 1:
                        value |= ((buffer[bufferpos++] & 0xFF) << 8);
                    case 0:
                        value |= (buffer[bufferpos++] & 0xFF);
                }
                l.add(value);
            }
        }
        for (int i = mainlength; i < length; i++) {
            l.add(readCInt());
        }
        return l;
    }

    public ArrayList<Integer> readIntList() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return null;
        }
        ArrayList<Integer> l = new ArrayList<Integer>();
        for (int i = 0; i < length; i++) {
            l.add(this.readInt());
        }
        return l;
    }

    public ArrayList<Long> readLongList() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return null;
        }
        ArrayList<Long> l = new ArrayList();
        for (int i = 0; i < length; i++) {
            l.add(this.readLong());
        }
        return l;
    }

    public ArrayList<String> readStringList() throws EOCException {
        int length = readInt();
        if (length == -1) {
            return null;
        }
        ArrayList<String> l = new ArrayList<String>();
        for (int i = 0; i < length; i++) {
            l.add(this.readString());
        }
        return l;
    }

    public void skipCIntArray() throws EOCException {
        int length = readCInt();
        if (length == -1) {
            return;
        }
        int mainlength = (length / 4) * 4;
        for (int i = 0; i < mainlength; i += 4) {
            checkIn(1);
            int mask = buffer[bufferpos++];
            skip(CIntArrayLength[mask]);
        }
        for (int i = mainlength; i < length; i++) {
            skipCInt();
        }
        //log.info("%d", this.getOffset());
    }

    public void writeC(double d) {
        writeC(Double.doubleToLongBits(d));
    }

    public double readCDouble() throws EOCException {
        long l = readCLong();
        return Double.longBitsToDouble(l);
    }

    public void skipCDouble() throws EOCException {
        skipCLong();
    }

    @Override
    public void openRead() throws IOException {
        datain.openRead();
        resize(getRequestedBufferSize());
        hasmore = true;
        eof = null;
    }

    public void openWrite() {
        dataout.openWrite();
        resize(getRequestedBufferSize());
    }

    public void write(Map<String, String> map) {
        write(map.size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            write(e.getKey());
            write(e.getValue());
        }
    }

    public Map<String, String> readStringPairMap() throws EOCException {
        int size = readInt();
        FastMap<String, String> map = new FastMap<String, String>();
        for (int i = 0; i < size; i++) {
            String key = readString();
            String value = readString();
            map.put(key, value);
        }
        return map;
    }

    public void skipStringPairMap() throws EOCException {
        int size = readInt();
        for (int i = 0; i < size; i++) {
            skipString();
            skipString();
        }
    }

//    public boolean sametext(byte a, byte b) {
//        return (a == b || (a >= 0 && b >= 0 && sametext[a] == sametext[b]));
//    }
    /**
     * Fill buffer while reading string. The current position till possible
     * match is stored in the current pos read.
     *
     * @param p
     * @return
     */
    public int readStringFillBuffer(pos p) {
        //log.info("readStringFillBuffer offset %d bufferpos %d pos %d end %d", this.offset, bufferpos, p.pos, end);
        if (p.pos > bufferpos) {
            p.s.append(ByteTools.toString(buffer, bufferpos, p.pos));
        }
        int shift = p.pos;
        bufferpos = p.pos;
        p.pos = 0;
        if (hasMore()) {
            this.softFillBuffer();
        }
        if (!hasMore()) {
            //log.info("set EOF %s", eof);
            p.endoffile = eof;
        }
        //log.info("readStringFillBuffer end offset %d bufferpos %d pos %d", offset, bufferpos, p.pos);
        return shift;
    }

    public int[] readCIntIncr() throws EOCException {
        int value[] = readCIntArray();
        for (int i = 1; i < value.length; i++) {
            value[i] += value[i - 1];
        }
        return value;
    }

    @Override
    public String readStringUntil(ByteSearch needle) throws EOCException {
        pos p = new pos(bufferpos);
        ByteSearchPosition endpos = needle.findPos(buffer, p.pos, end);
        while (p.endoffile == null && !endpos.found() || endpos.endreached) {
            readStringFillBuffer(p);
            endpos = needle.findPos(buffer, p.pos, end);
        }
        if (endpos.found()) {
            p.s.append(ByteTools.toString(buffer, bufferpos, endpos.start));
            bufferpos = endpos.end;
            return p.s.toString();
        }
        bufferpos = end;
        throw new EOCException("readStringUntil(%s)", needle.toString());
    }

    @Override
    public String readString(ByteSearch needle) throws EOCException {
        ByteSearchPosition endpos = readPos(needle);
        if (endpos.found()) {
            bufferpos = endpos.end;
            return ByteTools.toString(buffer, endpos.start, endpos.end);
        }
        bufferpos = end;
        throw new EOCException("findString(%s)", needle.toString());
    }

    @Override
    public String readTrimmedString(ByteSearch needle) throws EOCException {
        ByteSearchPosition endpos = readPos(needle);
        if (endpos.found()) {
            bufferpos = endpos.end;
            return ByteTools.toTrimmedString(buffer, endpos.start, endpos.end);
        }
        bufferpos = end;
        throw new EOCException("findTrimmedString(%s)", needle.toString());
    }


    @Override
    public String readFullTrimmedString(ByteSearch needle) throws EOCException {
        ByteSearchPosition endpos = readPos(needle);
        if (endpos.found()) {
            bufferpos = endpos.end;
            return ByteTools.toFullTrimmedString(buffer, endpos.start, endpos.end);
        }
        bufferpos = end;
        throw new EOCException("findFullTrimmedString(%s)", needle.toString());
    }

    @Override
    public String readMatchingString(ByteSearch needle) throws EOCException {
        ByteSearchPosition endpos = matchPos(needle);
        if (endpos.found()) {
            bufferpos = endpos.end;
            return ByteTools.toString(buffer, endpos.start, endpos.end);
        }
        bufferpos = end;
        throw new EOCException("matchString(%s)", needle.toString());
    }

    @Override
    public String readMatchingTrimmedString(ByteSearch needle) throws EOCException {
        ByteSearchPosition pos = matchPos(needle);
        if (pos.found()) {
            bufferpos = pos.end;
            return ByteTools.toTrimmedString(buffer, pos.start, pos.end);
        }
        throw new EOCException("matchTrimmedString(%s)", needle.toString());
    }

    @Override
    public String readMatchingFullTrimmedString(ByteSearch needle) throws EOCException {
        ByteSearchPosition pos = matchPos(needle);
        if (pos.found()) {
            bufferpos = pos.end;
            return ByteTools.toFullTrimmedString(buffer, pos.start, pos.end);
        }
        throw new EOCException("matchFullTrimmedString(%s)", needle.toString());
    }

    @Override
    public ByteSearchPosition readPos(ByteSearch needle) throws EOCException {
        ByteSearchPosition pos = needle.findPos(buffer, bufferpos, end);
        while (pos.endreached && hasMore()) {
            bufferpos = pos.start;
            softFillBuffer();
            pos = needle.findPos(buffer, bufferpos, end);
        }
        pos.offset = offset + pos.start;
        bufferpos = (pos.start > -1) ? pos.start : end;
        return pos;
    }

    @Override
    public ByteSearchSection readSection(ByteSection needle) throws EOCException {
        ByteSearchSection pos = needle.findPos(buffer, bufferpos, end);
        //log.info("readSection %s end %b more %b", needle.toString(), pos.endreached, hasMore());
        while (pos.endreached && hasMore()) {
            bufferpos = pos.start;
            softFillBuffer();
            pos = needle.findPos(buffer, bufferpos, end);
            //log.info("softfill end %b more %b off %d pos %d end %d", pos.endreached, hasMore(), offset, bufferpos, end);
        }
        pos.offset = offset + pos.start;
        bufferpos = (pos.start > -1) ? pos.end : end;
        return pos;
    }

    public ByteSearchSection readSectionStart(ByteSection needle) throws EOCException {
        ByteSearchSection pos = readSection(needle);
        bufferpos = (pos.start > -1) ? pos.start : end;
        return pos;
    }

    @Override
    public boolean skipUntil(ByteSearch needle) throws EOCException {
        ByteSearchPosition pos = readPos(needle);
        if (pos.found()) {
            bufferpos = pos.start;
            return true;
        }
        bufferpos = end;
        return false;
    }

    @Override
    public boolean skipPast(ByteSearch needle) throws EOCException {
        ByteSearchPosition pos = readPos(needle);
        if (pos.found()) {
            bufferpos = pos.end;
            return true;
        }
        bufferpos = end;
        return false;
    }

    @Override
    public boolean match(ByteSearch needle) throws EOCException {
        return matchPos(needle).found();
    }

    @Override
    public ByteSearchPosition matchPos(ByteSearch needle) throws EOCException {
        ByteSearchPosition pos = needle.matchPos(buffer, bufferpos, end);
        while (pos.endreached && hasMore()) {
            bufferpos = pos.start;
            softFillBuffer();
            pos = needle.matchPos(buffer, bufferpos, end);
        }
        pos.offset = offset + pos.start;
        return pos;
    }

    @Override
    public void movePast(ByteSearchPosition position) {
        bufferpos = position.end;
    }

    class pos {

        int pos;
        StringBuilder s = new StringBuilder();
        EOCException endoffile;

        public pos(int pos) {
            this.pos = pos;
            if (!hasMore()) {
                endoffile = eof;
            }
        }
    }
}
