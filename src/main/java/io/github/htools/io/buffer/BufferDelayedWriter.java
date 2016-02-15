package io.github.htools.io.buffer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.htools.io.struct.StructureWriter;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.type.Long128;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import static io.github.htools.lib.PrintTools.sprintf;

/**
 * This class is intended to remove all the Java fuzz regarding files. There is
 * just one class RFile that provides methods to read a line, read the entire
 * thing, write stuff to it, without having bother about which stream to use.
 * However, Java objects like properly opened FileInputStream and FileChannel.
 * <br><br> Some methods are provided that will more easily allow to get
 * information on the file, such as the parent Dir object, the filename,
 * extension, etc. <br><br> Some static methods are provided to do big file
 * operations, such as copying, moving, running and converting a File to a
 * primitive.
 * <p>
 * @author jbpvuurens
 */
public class BufferDelayedWriter implements StructureWriter {

    private static Log log = new Log(BufferDelayedWriter.class);
    private static Gson gson = new Gson();
    protected BufferReaderWriter writer = new BufferReaderWriter();
    private ArrayList<Object> list = new ArrayList<Object>();
    private boolean[] whitespace = io.github.htools.lib.ByteTools.getByteArray(" \n\t\r");
    private int size = 0;

    public BufferDelayedWriter() {
    }

    public byte[] getValue(boolean erase) {
        writer.setBuffer(new byte[size]);
        return writeToWriter(erase);
    }

    public byte[] getAsByteBlock(boolean erase) {
        writer.setBuffer(new byte[size + 4]);
        writer.write(size);
        return writeToWriter(erase);
    }

    public byte[] writeToWriter(boolean erase) {
        for (Object o : list) {
            if (o instanceof Integer) {
                writer.write((Integer) o);
            } else if (o instanceof Boolean) {
                writer.write((Boolean) o);
            } else if (o instanceof Byte) {
                writer.write((Byte) o);
            } else if (o instanceof Long) {
                writer.write((Long) o);
            } else if (o instanceof Short) {
                writer.write((Short) o);
            } else if (o instanceof byte[]) {
                writer.writeRaw((byte[]) o);
            }
        }
        if (erase) {
            clear();
        }
        return writer.buffer;
    }

    public void clear() {
        list = new ArrayList();
        size = 0;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void writeBuffer(DataOutput out) {
        try {
            byte b[] = getBytes();
            out.writeInt(b.length);
            out.write(b);
        } catch (IOException ex) {
            log.exception(ex, "BufferDelayedWriter.writeBuffer when writing bytes to output %s", out);
        }
    }

    @Override
    public void writeBuffer(StructureWriter writer) {
        writer.write(getBytes());
    }

    public byte[] getBytes() {
        return getValue(true);
    }

    public byte[] getAsByteBlock() {
        return getAsByteBlock(true);
    }

    @Override
    public void write(int i) {
        list.add(i);
        size += 4;
    }

    @Override
    public void write(boolean i) {
        list.add(i);
        size += 1;
    }

    public void write(short i) {
        list.add(i);
        size += 2;
    }

    @Override
    public void write(double d) {
        list.add(Double.doubleToLongBits(d));
        size += 8;
    }

    @Override
    public void write(long i) {
        list.add(i);
        size += 8;
    }

    @Override
    public void write(Long128 i) {
        i.write(this);
    }

    public void overwrite(int pos, long i) {
        list.set(pos, i);
    }

    public void overwrite(int pos, int i) {
        list.set(pos, i);
    }

    @Override
    public void writeRaw(byte b[]) {
        list.add(b);
        size += b.length;
    }

    public void writeRaw(String s) {
        writeRaw(ByteTools.toBytes(s));
    }

    public void writeRaw(String s, Object... params) {
        writeRaw(ByteTools.toBytes(sprintf(s, params)));
    }

    @Override
    public void write(byte b[]) {
        if (b == null) {
            write(-1);
        } else {
            write(b.length);
            list.add(b);
            size += b.length;
        }
    }

    @Override
    public void write(byte b[], int offset, int length) {
        byte bb[] = new byte[length];
        for (int i = 0; i < length; i++) {
            bb[i] = b[i + offset];
        }
        list.add(bb);
        size += length;
    }

    @Override
    public void write(byte i) {
        list.add(i);
        size++;
    }

    @Override
    public void write(String s) {
        if (s == null) {
            write(-1);
        } else {
            byte b[] = ByteTools.toBytes(s);
            write(b.length);
            writeRaw(b);
        }
    }

    public void write(JsonObject s) {
        write(s == null ? null : s.toString());
    }

    public void write(Object s, Type type) {
        if (s == null) {
            write((String) null);
        } else {
            write(gson.toJson(s, type));
        }
    }

    public void write0(JsonObject s) {
        write0(s == null ? null : s.toString());
    }

    @Override
    public void write(StringBuilder s) {
        if (s == null) {
            write(-1);
        } else {
            write(s.toString());
        }
    }

    @Override
    public void write(String array[]) {
        if (array == null) {
            write(-1);
        } else {
            write(array.length);
            for (String s : array) {
                write(s);
            }
        }
    }

    @Override
    public void write(long array[]) {
        if (array == null) {
            write(-1);
        } else {
            write(array.length);
            for (long s : array) {
                write(s);
            }
        }
    }

    @Override
    public void writeLongList(Collection<Long> array) {
        if (array == null) {
            write(-1);
        } else {
            write(array.size());
            for (Long s : array) {
                write(s);
            }
        }
    }

    @Override
    public void write(int array[]) {
        if (array == null) {
            write(-1);
        } else {
            write(array.length);
            for (int s : array) {
                write(s);
            }
        }
    }

    @Override
    public void write(boolean array[]) {
        if (array == null) {
            write(-1);
        } else {
            write(array.length);
            for (boolean s : array) {
                write(s);
            }
        }
    }

    @Override
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
    public void writeSquared(int array[][]) {
        if (array == null) {
            writeC(-1);
        } else if (array.length == 0) {
            writeC(0);
        } else {
            writeC(array.length);
            writeC(array[0].length);
            writeC(io.github.htools.lib.ArrayTools.flatten(array));
        }
    }

    @Override
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

    @Override
    public void writeSquared(int array[][][]) {
        if (array == null) {
            writeC(-1);
        } else if (array.length == 0) {
            writeC(0);
        } else {
            writeC(array.length);
            writeC(array[0].length);
            writeC(array[0][0].length);
            writeC(io.github.htools.lib.ArrayTools.flatten(array));
        }
    }

    @Override
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

    @Override
    public void writeC(long array[][]) {
        if (array == null) {
            writeC(-1);
        } else if (array.length == 0) {
            writeC(0);
        } else {
            writeC(array.length);
            for (long a[] : array) {
                writeC(a);
            }
        }
    }

    @Override
    public void writeC(int i) {
        writeC((long) i);
    }

    @Override
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
        for (int idx = len; idx != 0; idx--) {
            int shiftbits = (idx - 1) * 8;
            long mask = 0xFFL << shiftbits;
            write((byte) ((i & mask) >> shiftbits));
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

    @Override
    public void writeC(long[] l) {
        if (l == null) {
            writeC(-1);
            return;
        }
        writeC(l.length);
        byte m[] = new byte[4];
        int mainlength = (l.length / 4) * 4;
        //log.debug("write(long[]) mainlength %d", mainlength);
        for (int i = 0; i < mainlength; i += 4) {
            byte mask = 0;
            for (int s = i; s < i + 4; s++) {
                m[s - i] = longmask(l[s]);
                //log.info("longmask %d %d", m[s-i], l[s]);
                mask |= (m[s - i] << ((s - i) * 2));
            }
            write(mask);
            for (int s = i; s < i + 4; s++) {
                switch (m[s - i]) {
                    case 3:
                        writeC(l[s]);
                        break;
                    case 2:
                        write((byte) ((l[s] >> 16) & 0xFF));
                    case 1:
                        write((byte) ((l[s] >> 8) & 0xFF));
                    case 0:
                        write((byte) (l[s] & 0xFF));
                }
            }
        }
        for (int i = mainlength; i < l.length; i++) {
            writeC(l[i]);
        }
    }

    @Override
    public void writeSparse(long l[]) {
        writeSparse(l, 0, (l == null) ? 0 : l.length);
    }

    public void writeSparse(long l[], int offset, int length) {
        if (l == null) {
            writeC(-1);
            return;
        }
        int end = offset + length;
        int mainlength = 0;
        for (int i = offset; i < end; i++) {
            if (l[i] != 0) {
                mainlength++;
            }
        }
        //log.debug("writeNo0(long[]) offset %d length %d mainlength %d", offset, length, mainlength);
        int leap[] = new int[mainlength];
        long ltf[] = new long[mainlength];
        int last = offset, pos = 0;
        for (int i = 0; i < end; i++) {
            if (l[i] > 0) {
                leap[pos] = i - last;
                ltf[pos++] = l[i];
                last = i + 1;
                //log.debug( "entry %d %d", i, l[i]);
            }
        }
        writeC(length);
        writeC(leap);
        writeC(ltf);
    }

    @Override
    public void writeSparse(double l[]) {
        writeSparse(l, 0, (l == null) ? 0 : l.length);
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

    @Override
    public void writeIncr(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            array[i] -= array[i - 1];
        }
        writeC(array);
    }

    @Override
    public void writeIncr(ArrayList<Integer> list) {
        Integer arrayI[] = list.toArray(new Integer[list.size()]);
        int arrayi[] = new int[arrayI.length];
        for (int i = arrayI.length - 1; i > 0; i--) {
            arrayi[i] = arrayI[i] - arrayI[i - 1];
        }
        arrayi[0] = arrayI[0];
        writeC(arrayi);
    }

    @Override
    public void writeC(int[] l) {
        if (l == null) {
            writeC(-1);
            return;
        }
        byte m[] = new byte[4];
        writeC(l.length);
        int mainlength = (l.length / 4) * 4;
      //log.info("write() %d %d", l.length, length);
        //log.debug("write(int[]) mainlength %d", mainlength);
        for (int i = 0; i < mainlength; i += 4) {
            byte mask = 0;
            for (int s = i; s < i + 4; s++) {
                m[s - i] = longmask(l[s]);
                mask |= (m[s - i] << ((s - i) * 2));
            }
            BufferDelayedWriter.this.write(mask);
            for (int s = i; s < i + 4; s++) {
                switch (m[s - i]) {
                    case 3:
                        write((byte) ((l[s] >> 24) & 0xFF));
                    case 2:
                        write((byte) ((l[s] >> 16) & 0xFF));
                    case 1:
                        write((byte) ((l[s] >> 8) & 0xFF));
                    case 0:
                        write(((byte) (l[s] & 0xFF)));
                }
            }
        }
        for (int i = mainlength; i < l.length; i++) {
            writeC(l[i]);
        }
    }

    @Override
    public void writeC(ArrayList<Integer> l) {
        if (l == null) {
            writeC(-1);
            return;
        }
        byte m[] = new byte[4];
        int v[] = new int[4];
        writeC(l.size());
        int mainlength = (l.size() / 4) * 4;
      //log.info("write() %d %d", l.length, length);
        //log.debug("write(int[]) mainlength %d", mainlength);
        for (int i = 0; i < mainlength; i += 4) {
            byte mask = 0;
            for (int s = 0; s < 4; s++) {
                v[s] = l.get(i + s);
                m[s] = longmask(v[s]);
                mask |= (m[s] << (s * 2));
            }
            write(mask);
            for (int s = 0; s < 4; s++) {
                switch (m[s]) {
                    case 3:
                        write((byte) ((v[s] >> 24) & 0xFF));
                    case 2:
                        write((byte) ((v[s] >> 16) & 0xFF));
                    case 1:
                        write((byte) ((v[s] >> 8) & 0xFF));
                    case 0:
                        write(((byte) (v[s] & 0xFF)));
                }
            }
        }
        for (int i = mainlength; i < l.size(); i++) {
            writeC(l.get(i));
        }
    }

    public void writeC(double d) {
        writeC(Double.doubleToLongBits(d));
    }

    @Override
    public void writeSparseLong(Map<Integer, Long> table) {
        TreeSet<Integer> keys = new TreeSet<Integer>(table.keySet());
        if (keys.size() != 0) {
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

    @Override
    public void writeSparseInt(Map<Integer, Integer> table) {
        TreeSet<Integer> keys = new TreeSet<Integer>(table.keySet());
        if (keys.size() != 0) {
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

    @Override
    public void write2(int i) {
        write((byte) ((i >>> 8) & 0xFF));
        write((byte) ((i) & 0xFF));
    }

    @Override
    public void write3(int i) {
        write((byte) ((i >>> 16) & 0xFF));
        write((byte) ((i >>> 8) & 0xFF));
        write((byte) ((i) & 0xFF));
    }

    @Override
    public void writeUB(int i) {
        write((byte) ((i) & 0xFF));
    }

    @Override
    public void write0(String s) {
        if (s != null) {
            writeRaw(ByteTools.toBytes(s));
        }
        this.write((byte) 0);
    }

    @Override
    public void writeIntList(Collection<Integer> al) {
        if (al == null) {
            write(-1);
        } else {
            write(al.size());
            for (int l : al) {
                write(l);
            }
        }
    }

    @Override
    public void writeStringList(Collection<String> al) {
        if (al == null) {
            write(-1);
        } else {
            write(al.size());
            for (String l : al) {
                write(l);
            }
        }
    }

    @Override
    public void writeSparse(int[][] array) {
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

    @Override
    public void writeSparse(int[][][] array) {
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

    @Override
    public void writeSparse(long[][] array) {
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

    @Override
    public void write(Map<String, String> map) {
        write(map.size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            write(e.getKey());
            write(e.getValue());
        }
    }

    @Override
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

    @Override
    public long getOffset() {
        return size;
    }

    @Override
    public void setBufferSize(int i) {
    }

    @Override
    public int getBufferSize() {
        return Integer.MAX_VALUE;
    }

    private byte[] readPos(int pos) {
        Object o = list.get(pos);
        if (o instanceof byte[]) {
            return (byte[]) o;
        }
        return ByteTools.toBytes(o.toString());
    }

    /**
     * @return An InputStream that allows to read the buffered content as a byte
     * stream, keeping the buffer after it was read.
     */
    public InputStream getAsInputStreamAndKeep() {
        return new BufferBackedInputStream(false);
    }
    
    /**
     * @return An InputStream that allows to read the buffered content as a byte
     * stream, erasing the buffer when finished reading read (does not erase between
     * reads to the InputStream, so a new InputStream starts reading from the start).
     */
    public InputStream getAsInputStream() {
        return new BufferBackedInputStream(true);
    }
    
    private class BufferBackedInputStream extends InputStream {
        boolean erase;
        int arraypos = 0;
        byte[] currentItem = new byte[0];
        int itempos = 0;

        BufferBackedInputStream(boolean erase) {
            this.erase = erase;
        }
        
        @Override
        public int read() throws IOException {
            if (arraypos >= list.size() && itempos >= currentItem.length) {
                if (erase)
                    BufferDelayedWriter.this.clear();
                return -1;
            }
            while (itempos >= currentItem.length) {
                itempos = 0;
                currentItem = readPos(arraypos++);
            }
            if (itempos >= currentItem.length) {
                if (erase) {
                    BufferDelayedWriter.this.clear();
                }
                return -1;
            }
            return currentItem[itempos++] & 0xFF;
        }

        public int read(byte[] bytes, int off, int len) throws IOException {
            if (arraypos >= list.size() && itempos >= currentItem.length) {
                if (erase) {
                    BufferDelayedWriter.this.clear();
                }
                return -1;
            }
            int copiedSize = 0;
            int lastLength = 0;
            if (itempos < currentItem.length) {
                lastLength = Math.min(len, currentItem.length - itempos);
                System.arraycopy(currentItem, itempos, bytes, copiedSize, lastLength);
                copiedSize += lastLength;
                len -= lastLength;
            }
            if (copiedSize < len && arraypos < list.size()) {
                itempos = 0;
                while (len > 0 && arraypos < list.size()) {
                    currentItem = readPos(arraypos++);
                    lastLength = Math.min(len, currentItem.length);
                    System.arraycopy(currentItem, 0, bytes, copiedSize, lastLength);
                    copiedSize += lastLength;
                    len -= lastLength;
                }
            }
            if (len > 0 && erase)
                BufferDelayedWriter.this.clear();
            itempos += lastLength;
            return copiedSize;
        }
    }
}
