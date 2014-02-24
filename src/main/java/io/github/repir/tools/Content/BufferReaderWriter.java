package io.github.repir.tools.Content;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.tools.DataTypes.ArrayMap;
import io.github.repir.tools.Lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.ByteTools;

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
 * <p/>
 * @author jbpvuurens
 */
public class BufferReaderWriter implements StructureData, StructureReader, StructureWriter {

   public static Log log = new Log(BufferReaderWriter.class);
   public EOFException eof;
   public byte[] buffer;
   public boolean[] whitespace = io.github.repir.tools.Lib.ByteTools.whitespace;
   public byte[] sametext = io.github.repir.tools.Lib.ByteTools.sametext;
   public long offset = 0;
   public long ceiling = Long.MAX_VALUE;
   public boolean hasmore = true;
   public int bufferpos = 0;
   public int end;
   public DataIn datain = null;
   public DataOut dataout = null;

   public BufferReaderWriter() {
      buffer = new byte[1000];
   }

   public BufferReaderWriter(byte buffer[]) {
      setBuffer(buffer);
   }

   public BufferReaderWriter(DataInput in) {
      readBuffer(in);
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

   public void setEnd( int end ) {
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

   @Override
   public final void setDataIn(DataIn in) {
      //log.info("setDataIn %s %s", this, in);
      datain = in;
      in.setBuffer(this);
   }

   public final void setDataOut(DataOut out) {
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
      setEnd(buffer.length);
   }

   public final void setBuffer(byte buffer[], int pos, int end) {
      //log.info("setBuffer() pos %d end %d", pos, end);
      this.buffer = buffer;
      bufferpos = pos;
      setEnd(end);
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

   public void checkIn(int size) throws EOFException {
      //log.info("checkIn( %d ) bufferlegth %d", size, buffer.length);
      if (size > buffer.length) {
         resize(size);
      }
      if (end - bufferpos < size) {
         fillBuffer();
         if (end - bufferpos < size) {
            throw new EOFException("EOF reached");
         }
      }
   }

   @Override
   public void fillBuffer() throws EOFException {
      if (datain != null) {
         shift();
         if (hasmore) {
            try {
               //log.info("fillBuffer %s %s", this, datain);
               datain.fillBuffer(this);
            } catch (EOFException ex) {
               hasmore = false;
               this.eof = ex;
               throw ex;
            }
         } else {
            throw eof;
         }
      } else {
         hasmore = false;
         throw new EOFException();
      }
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
   public void reset() {
      setOffset(offset);
      hasmore = true;
   }

   @Override
   public void setOffset(long offset) {
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

   public void setBufferSize(int buffersize) {
      //log.info("setBufferSize( %d ) currentlength %d dataout %s", buffersize, buffer.length, dataout);
      if (buffer != null && buffersize != buffer.length && bufferpos > 0 && dataout != null) {
         flushBuffer();
      }
      resize(buffersize);
   }

   public int getBufferSize() {
      return buffer.length;
   }

   public void closeWrite() {
      //log.info("closeWrite()");
      if (dataout != null) {
         //log.info("close offset %d bufferpos %d bufferend %d", offset, bufferpos, end);
         flushBuffer();
         setBuffer(new byte[0]);
         dataout.close();
      }
   }

   @Override
   public void closeRead() {
      if (datain != null) {
         datain.close();
      }
      setBuffer(new byte[0]);
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
      if (end > buffer.length - bufferpos) {
         setEnd(buffer.length - bufferpos);
      }
      //log.info("end %d", end);
   }

   public long getLength() {
      return datain.getLength();
   }

   public int resize(int size) {
      //log.info("resize( %d )", size);
      if (buffer == null) {
         buffer = new byte[size];
      } else if (size > buffer.length) {
         int shift = bufferpos;
         byte newbuffer[] = new byte[size];
         for (int i = bufferpos; i < end; i++) {
            newbuffer[ i - bufferpos] = buffer[i];
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

   public int expand(int size) {
      //log.info("expand( %d )", size);
      if (size > buffer.length) {
         return resize(size);
      }
      return 0;
   }

   public int shift() {
      //log.info("shift()");
      int shift = bufferpos;
      for (int i = shift; i < end; i++) {
         buffer[ i - shift] = buffer[i];
      }
      setEnd(end-bufferpos);
      //log.info("shift() end %d", end);
      bufferpos = 0;
      offset += shift;
      return shift;
   }

   public void print(String s) {
      if (s != null) {
         byte b[] = s.getBytes();
         write(b);
      }
   }

   public void write0(String s) {
      if (s != null) {
         write(s.getBytes());
      }
      write((byte) 0);
   }

   public int readInt() throws EOFException {
      //log.info("readInt() bufferpos %d", bufferpos);
      checkIn(4);
      int ch1 = buffer[bufferpos++] & 0xFF;
      int ch2 = buffer[bufferpos++] & 0xFF;
      int ch3 = buffer[bufferpos++] & 0xFF;
      int ch4 = buffer[bufferpos++] & 0xFF;
      int result = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
      return result;
   }

   public int readInt2() throws EOFException {
      //log.info("readInt() bufferpos %d", bufferpos);
      checkIn(2);
      int ch1 = buffer[bufferpos++] & 0xFF;
      int ch2 = buffer[bufferpos++] & 0xFF;
      int result = (ch1 << 8) + ch2;
      return result;
   }

   public int readInt3() throws EOFException {
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

   public int readShort() throws EOFException {
      checkIn(2);
      int ch1 = buffer[bufferpos++] & 0xFF;
      int ch2 = buffer[bufferpos++] & 0xFF;
      int result = ((ch1 << 8) + (ch2));
      return result;
   }

   public void skipShort() {
      skip(2);
   }

   public int readUShort() throws EOFException {
      checkIn(2);
      int ch1 = buffer[bufferpos++] & 0xFF;
      int ch2 = buffer[bufferpos++] & 0xFF;
      int result = (int) ((ch1 << 8) + (ch2));
      return result;
   }

   public void skipUShort() {
      skip(2);
   }

   public double readDouble() throws EOFException {
      long l = readLong();
      return Double.longBitsToDouble(l);
   }

   public void skipDouble() {
      skip(8);
   }

   public long readLong() throws EOFException {
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

   public void skipLong() {
      skip(8);
   }

   public int readByte() throws EOFException {
      checkIn(1);
      return (buffer[bufferpos++] & 0xFF);
   }

   public boolean readBoolean() throws EOFException {
      checkIn(1);
      return (buffer[bufferpos++] == 0) ? false : true;
   }

   public void skipByte() {
      skip(1);
   }

   public String readString() throws EOFException {
      int length = readInt();
      return readString(length);
   }

   public StringBuilder readStringBuilder() throws EOFException {
      int length = readInt();
      if (length == -1) {
         return null;
      }
      return new StringBuilder(readString(length));
   }

   public void skipString() throws EOFException {
      int length = readInt();
      skip(length);
   }

   public void skipStringBuilder() throws EOFException {
      skipString();
   }

   public byte[] readBytes(int length) throws EOFException {
      if (length > 1000000) {
         log.info("very long data length=%d offset=%d", length, this.getOffset());
      }
      if (length < 0) {
         return null;
      }
      byte b[] = new byte[length];
      readBytes(b, 0, length);
      return b;
   }

   public byte[] readByteBlock() throws EOFException {
      int length = readInt();
      return readBytes(length);
   }

   public void skipByteBlock() throws EOFException {
      int length = readInt();
      skip(length);
   }

   public void readBytes(byte b[], int offset, int length) throws EOFException {
      if (length > buffer.length) {
         for (; bufferpos < end; bufferpos++, length--) {
            b[offset++] = buffer[bufferpos];
         }
         int read = datain.readBytes(this.offset + this.bufferpos, b, offset, length);
         if (read != length) {
            log.info("readBytes(%d %d): EOF reached when reading fixed number of bytes", offset, length);
            if (datain instanceof HDFSIn) {
               log.info("in file %s", ((HDFSIn) datain).path.toString());
            }
            log.crash();
         }
         this.offset += bufferpos + length;
         bufferpos = 0;
         setEnd(0);
      } else {
         checkIn(length);
         if (bufferpos <= end - length) {
            length += offset;
            while (offset < length) {
               b[offset++] = buffer[bufferpos++];
            }
         }
      }
   }

   public String readString(int length) throws EOFException {
      if (length > -1) {
         return ByteTools.toString(readBytes(length));
      }
      return null;
   }

   public String readString0() throws EOFException {
      checkIn(1);
      int strend = bufferpos - 1;
      StringBuilder sb = new StringBuilder();
      do {
         if (++strend >= end) {
            sb.append(ByteTools.toString(buffer, bufferpos, strend - bufferpos));
            bufferpos = strend;
            fillBuffer();
            strend = 0;
         }
      } while (buffer[strend] != 0);
      String s = ByteTools.toString(buffer, bufferpos, strend - bufferpos);
      bufferpos = strend + 1;
      if (sb.length() > 0) {
         return sb.append(s).toString();
      }
      return s;
   }

   public void skipString0() throws EOFException {
      bufferpos -= 1;
      do {
         if (++bufferpos >= end) {
            fillBuffer();
         }
      } while (buffer[bufferpos] != 0);
      bufferpos++;
   }

   public String readUntil(ByteRegex regex) throws EOFException {
      Pos first = find(regex);
      String s = new String(buffer, bufferpos, first.start - bufferpos);
      bufferpos = first.end;
      return s;
   }

   public String read(ByteRegex regex) throws EOFException {
      Pos first = find(regex);
      String s = new String(buffer, first.start, first.end - first.start);
      bufferpos = first.end;
      return s;
   }

   public void skipAfter(ByteRegex regex) throws EOFException {
      Pos first = find(regex);
      bufferpos = first.end;
   }

   public void skipBefore(ByteRegex regex) throws EOFException {
      Pos first = find(regex);
      bufferpos = first.start;
   }

   public Pos find(ByteRegex regex) throws EOFException {
      if (!hasMore())
         throw this.eof;
      Pos first = regex.find(buffer, bufferpos, end);
      if (first.endreached && hasMore()) {
         fillBuffer();
         first = regex.find(buffer, bufferpos, end);
      }
      //log.info("%d %d %d", first.start, bufferpos, end);
      if (first.found()) {
         return first;
      } else {
         throw new EOFException();
      }
   }

   public String[] readStringArray() throws EOFException {
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

   public void skipStringArray() throws EOFException {
      int length = readInt();
      if (length == -1) {
         return;
      }
      for (int i = 0; i < length; i++) {
         skipString();
      }
   }

   public long[] readLongArray() throws EOFException {
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

   public void skipLongArray() throws EOFException {
      int length = readInt();
      if (length == -1) {
         return;
      }
      skip(8 * length);
   }

   public double[] readDoubleArray() throws EOFException {
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

   public void skipDoubleArray() throws EOFException {
      skipLongArray();
   }

   public long[][] readCLongArray2() throws EOFException {
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

   public void skipCLongArray2() throws EOFException {
      int length = readInt();
      if (length < 1) {
         return;
      }
      for (int i = 0; i < length; i++) {
         skipCLongArray();
      }
   }

   public int[] readIntArray() throws EOFException {
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

   public void skipIntArray() throws EOFException {
      int length = readInt();
      if (length == -1) {
         return;
      }
      skip(4 * length);
   }

   public int[][] readSquaredIntArray2() throws EOFException {
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

   public int[][][] readSquaredIntArray3() throws EOFException {
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
               array[ d1][ d2][ d3] = input[p++];
            }
         }
      }
      return array;
   }

   public int[][][] readCIntArray3() throws EOFException {
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

   public int[][] readCIntArray2() throws EOFException {
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

   public void skipCIntArray2() throws EOFException {
      int length = readCInt();
      for (int i = 0; i < length; i++) {
         skipCIntArray();
      }
   }

   public void skipCIntArray3() throws EOFException {
      int length = readCInt();
      for (int i = 0; i < length; i++) {
         skipSquaredIntArray2();
      }
   }

   public void skipSquaredIntArray2() throws EOFException {
      int length = this.readCInt();
      if (length < 1) {
         return;
      }
      skipCInt();
      skipCIntArray();
   }

   public void skipSquaredIntArray3() throws EOFException {
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

   public void write(Collection<Integer> i) {
      if (i == null) {
         write(-1);
      } else {
         write(i.size());
         for (int l : i) {
            write(l);
         }
      }
   }

   public void writeStr(Collection<String> i) {
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
         int flat[] = io.github.repir.tools.Lib.ArrayTools.flatten(array);
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
         int flat[] = io.github.repir.tools.Lib.ArrayTools.flatten(array);
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
         writeSparse(io.github.repir.tools.Lib.ArrayTools.flatten(array));
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
         writeSparse(io.github.repir.tools.Lib.ArrayTools.flatten(array));
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
         writeSparse(io.github.repir.tools.Lib.ArrayTools.flatten(array));
      }
   }

   public void skipIntSparse3() throws EOFException {
      int length = readCInt();
      if (length < 1) {
         return;
      }
      skipCInt();
      skipCInt();
      skipIntSparse();
   }

   public void skipIntSparse2() throws EOFException {
      int length = readCInt();
      if (length < 1) {
         return;
      }
      skipCInt();
      skipIntSparse();
   }

   public void skipLongSparse2() throws EOFException {
      int length = readCInt();
      if (length < 1) {
         return;
      }
      skipCInt();
      skipLongSparse();
   }

   public int[][] readIntSparse2() throws EOFException {
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

   public long[][] readLongSparse2() throws EOFException {
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

   public int[][][] readIntSparse3() throws EOFException {
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

   public void write(byte b[]) {
      for (byte i : b) {
         if (bufferpos >= buffer.length) {
            this.flushBuffer();
         }
         buffer[bufferpos++] = i;
      }
   }

   public void writeByteBlock(byte b[]) {
      write(b.length);
      write(b);
   }

   public void write(byte b[], byte escape) {
      for (int i = 0; i < b.length; i++) {
         if (b[i] == escape) {
            if (bufferpos >= buffer.length) {
               this.flushBuffer();
            }
            buffer[bufferpos++] = escape;
         }
         if (bufferpos >= buffer.length) {
            this.flushBuffer();
         }
         buffer[bufferpos++] = b[i];
      }
   }

   public void write(byte b[], byte eof[], byte escape) {
      if (eof.length == 0) {
         write(b, escape);
         return;
      }
      for (int i = 0; i < b.length; i++) {
         if (b[i] == escape || io.github.repir.tools.Lib.ByteTools.matchString(b, eof, i)) {
            if (bufferpos >= buffer.length) {
               this.flushBuffer();
            }
            buffer[bufferpos++] = escape;
         }
         if (bufferpos >= buffer.length) {
            this.flushBuffer();
         }
         buffer[bufferpos++] = b[i];
      }
   }

   public void write(byte b[], byte end[], byte end2[], byte escape) {
      if (end2.length == 0) {
         write(b, end, escape);
      } else if (end.length == 0) {
         write(b, end2, escape);
      } else {
         for (int i = 0; i < b.length; i++) {
            if (b[i] == escape || io.github.repir.tools.Lib.ByteTools.matchString(b, end, i) || io.github.repir.tools.Lib.ByteTools.matchString(b, end2, i)) {
               if (bufferpos >= buffer.length) {
                  this.flushBuffer();
               }
               buffer[bufferpos++] = escape;
            }
            if (bufferpos >= buffer.length) {
               this.flushBuffer();
            }
            buffer[bufferpos++] = b[i];
         }
      }
   }

   public void writeWS(byte b[], byte eof[], byte escape) {
      for (int i = 0; i < b.length; i++) {
         if (bufferpos >= buffer.length) {
            this.flushBuffer();
         }
         if (io.github.repir.tools.Lib.ByteTools.matchStringWS(b, eof, i)) {
            buffer[bufferpos++] = escape;
            if (bufferpos >= buffer.length) {
               this.flushBuffer();
            }
         }
         buffer[bufferpos++] = b[i];
      }
   }

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
      buffer[ bufferpos++] = (byte) ((c >>> 8) & 0xFF);
      buffer[ bufferpos++] = (byte) ((c >>> 0) & 0xFF);
   }

   public void write(String s) {
      if (s == null) {
         write(-1);
      } else {
         byte b[] = s.getBytes();
         write(b.length);
         write(b);
      }
   }

   public void write(StringBuilder s) {
      if (s == null) {
         write(-1);
      } else {
         write(s.toString());
      }
   }

   public long readCLong() throws EOFException {
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

   public void skipCLong() throws EOFException {
      checkIn(1);
      byte firstByte = buffer[bufferpos++];
      int len = decodeVIntSize(firstByte);
      skip(len - 1);
   }

   public void writeC(int i) {
      writeC((long) i);
   }

   public int readCInt() throws EOFException {
      return (int) readCLong();
   }

   public void skipCInt() throws EOFException {
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

   public long[] readCLongArray() throws EOFException {
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

   public void skipCLongArray() throws EOFException {
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

   public long[] readLongSparse() throws EOFException {
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

   public void skipLongSparse() throws EOFException {
      int length = readCInt();
      if (length == -1) {
         return;
      }
      skipCIntArray();
      skipCLongArray();
   }

   public double[] readDoubleSparse() throws EOFException {
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

   public void skipDoubleSparse() throws EOFException {
      int length = readCInt();
      if (length == -1) {
         return;
      }
      skipCIntArray();
      skipDoubleArray();
   }

   public int[] readIntSparse() throws EOFException {
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

   public void skipIntSparse() throws EOFException {
      int length = readCInt();
      if (length == -1) {
         return;
      }
      skipCIntArray();
      skipCIntArray();
   }

   public HashMap<Integer, Long> readSparseLongMap() throws EOFException {
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

   public HashMap<Integer, Integer> readSparseIntMap() throws EOFException {
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
            switch (m[ s - i]) {
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

   public int[] readCIntArray() throws EOFException {
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

   public ArrayList<Integer> readCIntArrayList() throws EOFException {
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

   public ArrayList<Integer> readIntArrayList() throws EOFException {
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

   public ArrayList<String> readStrArrayList() throws EOFException {
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
   static final int CIntArrayLength[] = initClongArrayLength();

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

   public void skipCIntArray() throws EOFException {
      int length = readCInt();
      if (length == -1) {
         return;
      }
      int mainlength = (length / 4) * 4;
      for (int i = 0; i < mainlength; i += 4) {
         checkIn(1);
         int mask = buffer[bufferpos++];
         skip(CIntArrayLength[ mask]);
      }
      for (int i = mainlength; i < length; i++) {
         skipCInt();
      }
      //log.info("%d", this.getOffset());
   }

   public void writeC(double d) {
      writeC(Double.doubleToLongBits(d));
   }

   public double readCDouble() throws EOFException {
      long l = readCLong();
      return Double.longBitsToDouble(l);
   }

   public void skipCDouble() throws EOFException {
      skipCLong();
   }

   public void openRead() {
      datain.openRead();
      hasmore = true;
      eof = null;
   }

   public void openWrite() {
      dataout.openWrite();
   }

   public void openAppend() {
      dataout.openAppend();
   }

   public void write(Map<String, String> map) {
      write(map.size());
      for (Map.Entry<String, String> e : map.entrySet()) {
         write(e.getKey());
         write(e.getValue());
      }
   }

   public Map<String, String> readStringPairMap() throws EOFException {
      int size = readInt();
      ArrayMap<String, String> map = new ArrayMap<String, String>();
      for (int i = 0; i < size; i++) {
         String key = readString();
         String value = readString();
         map.put(key, value);
      }
      return map;
   }

   public void skipStringPairMap() throws EOFException {
      int size = readInt();
      for (int i = 0; i < size; i++) {
         skipString();
         skipString();
      }
   }

   /**
    * Reads the buffer until end-of-field is found.
    * <p/>
    * @param eof end-of-field sequence
    * @param escape escape character that must not precede eof
    * @return the String up till the point of the end-of-field
    * @throws EOFException
    */
   public String readString(byte[] eof, byte escape) throws EOFException {
      //log.info("readString( %s )", new String(eof));
      int current2 = bufferpos;
      int match = 0;
      pos p = new pos(bufferpos);
      while (match < eof.length && p.endoffile == null) {
         current2 = p.pos;
         for (match = 0; match < eof.length && hasMore();) {
            if (current2 >= end) {
               current2 -= fillBuffer(p);
            }
            if (buffer[current2] == escape) {
               p.pos = current2 + 1;
               break;
            } else if (buffer[current2] == eof[match]) {
               match++;
               current2++;
            } else {
               break;
            }
         }
         if (match < eof.length && p.endoffile == null) {
            ++p.pos;
         }
      }
      if (p.endoffile != null && match < eof.length) {
         throw p.endoffile;
      }
      if (p.pos > bufferpos) {
         p.s.append(ByteTools.toString(buffer, bufferpos, p.pos - bufferpos));
      }
      bufferpos = current2;
      //log.info("end readString( %s )", new String(eof));
      return p.s.toString();
   }

   public String readString(byte[] eof, byte peekend[], byte escape) throws EOFException {
      if (eof.length == 0) {
         return readStringPeekEnd(peekend, escape);
      }
      //log.info("readString( '%s', '%s' )", new String(eof), new String(peekend));
      int current2 = bufferpos;
      int match = 0;
      pos p = new pos(bufferpos);
      while (match < eof.length && p.endoffile == null) {
         if (!peekStringNotExists(peekend, p)) {
            current2 = p.pos;
            break;
         }
         if (buffer[p.pos] == escape) {
            p.pos++;
            fillBuffer(p);
            ++p.pos;
         } else {
            current2 = p.pos;
            for (match = 0; match < eof.length && this.hasMore();) {
               if (current2 >= end) {
                  current2 -= fillBuffer(p);
               } else if (buffer[current2] == eof[match]) {
                  match++;
                  current2++;
               } else {
                  break;
               }
            }
            if (match < eof.length && p.endoffile == null) {
               ++p.pos;
            }
         }
      }
      if (p.endoffile != null && match < eof.length) {
         throw p.endoffile;
      }
      if (p.pos > bufferpos) {
         p.s.append(ByteTools.toString(buffer, bufferpos, p.pos - bufferpos));
      }
      //log.info("end");
      bufferpos = current2;
      return p.s.toString();
   }

   public String readStringPeekEnd(byte peekend[], byte escape) throws EOFException {
      pos p = new pos(bufferpos);
      while (p.endoffile == null || p.pos < end) {
         if (!peekStringNotExists(peekend, p)) {
            if (p.pos > bufferpos) {
               p.s.append(ByteTools.toString(buffer, bufferpos, p.pos - bufferpos));
            }
            bufferpos = p.pos;
            return p.s.toString();
         }
         if (buffer[p.pos] == escape) {
            p.pos++;
            fillBuffer(p);
            ++p.pos;
         } else {
            ++p.pos;
         }
      }
      throw p.endoffile;
   }

   public String readStringPeekEndWS(byte peekend[], byte escape) throws EOFException {
      pos p = new pos(bufferpos);
      while (p.endoffile == null || p.pos < end) {
         if (!peekStringNotExistsWS(peekend, p)) {
            if (p.pos > bufferpos) {
               p.s.append(ByteTools.toString(buffer, bufferpos, p.pos - bufferpos));
            }
            bufferpos = p.pos;
            return p.s.toString();
         }
         if (buffer[p.pos] == escape) {
            p.pos++;
            fillBuffer(p);
            ++p.pos;
         } else {
            ++p.pos;
         }
      }
      throw p.endoffile;
   }

   /**
    * Reads the buffer until end-of-field is found. Spaces in the eof sequence,
    * matchWS zero or more whitespace characters.
    * <p/>
    * @param eof end-of-field sequence
    * @return the String up till the point of the end-of-field
    * @throws EOFException
    */
   public String readStringWS(byte[] eof, byte escape) throws EOFException {
      //log.info("start readStringWS '%s'", new String(eof));
      int current2 = bufferpos;
      int match = 0;
      pos p = new pos(bufferpos);
      //log.info("readStringWS %s %d %d %b %s", new String(eof), matchWS, eof.length, matchWS < eof.length, new String(buffer, bufferpos, 10));
      while (match < eof.length && p.endoffile == null) {
         current2 = p.pos;
         //log.info("readString %s bufferpos %d end %d current %d matchWS %d", new String(eof), bufferpos, end, p.pos, match);
         for (match = 0; match < eof.length;) {
            if (current2 >= end) {
               //log.info("readStringWS fillBuffer");
               current2 -= fillBuffer(p);
               //log.info("after fillbuffer %d %d '%s'", bufferpos, current2, new String(buffer, 0, 50));
            }
            //log.info("matchWS %d %d %d '%s' %d", match, current2, buffer[current2], DataTypes.ByteTools.listBytesAsString(buffer[current2]), eof[match]);
            if (current2 >= end) {
               for (; match < eof.length && whitespace[eof[match]]; match++);
               break;
            } else if (buffer[current2] == escape) {
               p.pos = current2 + 1;
               break;
            } else if (whitespace[eof[match]]) {
               if (buffer[current2] >= 0 && whitespace[buffer[current2]]) {
                  current2++;
               } else if (whitespace[eof[match]]) {
                  match++;
               }
            } else if (sametext(buffer[current2], eof[match])) {
               match++;
               current2++;
            } else {
               break;
            }
         }
         if (match < eof.length && p.endoffile == null) {
            ++p.pos;
         }
      }
      if (p.endoffile != null && match < eof.length) {
         throw p.endoffile;
      }
      if (p.pos > bufferpos) {
         p.s.append(ByteTools.toString(buffer, bufferpos, p.pos - bufferpos));
      }
      bufferpos = current2;
      //log.info("return");
      return p.s.toString();
   }

   /**
    * Reads the buffer until end-of-field is found. Spaces in the eof sequence,
    * matchWS zero or more whitespace characters.
    * <p/>
    * @param eof end-of-field sequence
    * @return the String up till the point of the end-of-field
    * @throws EOFException
    */
   public String readString(ByteRegex eof) throws EOFException {
      pos p = new pos(bufferpos);
      //log.info("endos start %d found %b endreached %b", endpos.start, endpos.found(), endpos.endreached);
      while (p.endoffile == null) {
         if (p.pos < end && buffer[p.pos] == '\\') {
            if (++p.pos >= end) {
               fillBuffer(p);
            }
            p.pos++;
            continue;
         }
         Pos endpos = eof.find(buffer, p.pos, end);
         if (endpos.endreached) {
            p.pos = endpos.start;
            fillBuffer(p);
            if (p.pos < end) {
               continue;
            }
            if (!endpos.found()) {
               throw new EOFException();
            }
         }
         if (endpos.found()) {
            p.s.append(ByteTools.toString(buffer, bufferpos, endpos.start - bufferpos));
            bufferpos = endpos.end;
            return p.s.toString();
         }
         p.pos++;
      }
      throw p.endoffile;
   }

   /**
    * Peek in the buffer if regex is found next
    * <p/>
    * @param regex
    * @param p
    * @return
    * @throws EOFException
    */
   public Pos peekStringExists(ByteRegex regex, pos p) throws EOFException {
      //log.info("peekString( %s )", new String(sof));
      //regex.print();
      Pos endpos = regex.findFirst(buffer, p.pos, end);
      if (!endpos.found() && endpos.endreached) {
         fillBuffer(p);
         endpos = regex.findFirst(buffer, p.pos, end);
      }
      //log.info("end peekString() start %d end %d found %b", endpos.start, endpos.end, endpos.found());
      return endpos;
   }

   @Override
   public void skipString(ByteRegex regex) throws EOFException {
      readString(regex);
   }

   @Override
   public boolean peekStringExists(ByteRegex sof) throws EOFException {
      return sof.isEmpty() || peekStringExists(sof, new pos(bufferpos)).found();
   }

   public void skipFirst(ByteRegex sof) throws EOFException {
      if (!sof.isEmpty()) {
         Pos peekStringExists = peekStringExists(sof, new pos(bufferpos));
         if (peekStringExists.found()) {
            bufferpos = peekStringExists.end;
         }
      }
   }

   @Override
   public boolean peekStringNotExists(ByteRegex sof) throws EOFException {
      return sof.isEmpty() || !peekStringExists(sof, new pos(bufferpos)).found();
   }

   public boolean peekStringNotExists(ByteRegex sof, pos p) throws EOFException {
      return sof.isEmpty() || !peekStringExists(sof, p).found();
   }

   public boolean sametext(byte a, byte b) {
      return (a == b || (a >= 0 && b >= 0 && sametext[a] == sametext[b]));
   }

   public String readStringWS(byte[] eof, byte peekend[], byte escape) throws EOFException {
      if (eof.length == 0) {
         return readStringPeekEndWS(peekend, escape);
      }
      //log.info("start readStringWS '%s' '%s'", new String(eof), new String(peekend));
      int current2 = bufferpos;
      int match = 0;
      pos p = new pos(bufferpos);
      //log.info("readStringWS %s %d %d %b %s", new String(eof), match, eof.length, match < eof.length, new String(buffer, bufferpos, 10));
      while (match < eof.length && p.endoffile == null) {
         if (!peekStringNotExistsWS(peekend, p)) {
            current2 = p.pos;
            break;
         }
         current2 = p.pos;
         for (match = 0; match < eof.length;) {
            if (current2 >= end) {
               //log.info("readStringWS fillBuffer");
               current2 -= fillBuffer(p);
               //log.info("after fillbuffer %d %d '%s'", bufferpos, current2, new String(buffer, 0, 50));
            }
            //log.info("matchWS %d %d %d %d '%s' %d buffer '%s'", match, p.pos, current2, buffer[current2], DataTypes.ByteTools.listBytesAsString(buffer[current2]), eof[match],
            //        new String( buffer, p.pos, Math.min(40, end-p.pos)));
            if (current2 >= end) {
               for (; match < eof.length && whitespace[eof[match]]; match++);
            } else if (buffer[current2] == escape) {
               p.pos = current2 + 1;
               break;
            } else if (whitespace[eof[match]]) {
               if (buffer[current2] >= 0 && whitespace[buffer[current2]]) {
                  current2++;
               } else {
                  match++;
               }
            } else if (sametext(buffer[current2], eof[match])) {
               match++;
               current2++;
            } else {
               break;
            }
         }
         if (match < eof.length && p.endoffile == null) {
            ++p.pos;
         }
      }
      if (p.endoffile != null && match < eof.length) {
         throw p.endoffile;
      }
      if (p.pos > bufferpos) {
         //log.info("store '%s' bufferpos %d pos %d next %d", new String(buffer, bufferpos, current2 - bufferpos), bufferpos, p.pos, current2);
         p.s.append(ByteTools.toString(buffer, bufferpos, p.pos - bufferpos));
      }
      bufferpos = current2;
      //log.info("return");
      return p.s.toString();
   }

   public int fillBuffer(pos p) {
      //log.info("fillBuffer offset %d bufferpos %d pos %d end %d", this.offset, bufferpos, p.pos, end);
      if (p.pos > bufferpos) {
         p.s.append(ByteTools.toString(buffer, bufferpos, p.pos - bufferpos));
      }
      int shift = p.pos;
      bufferpos = p.pos;
      p.pos = 0;
      try {
         if (this.hasmore) {
            fillBuffer();
         }
      } catch (EOFException ex) {
      }
      if (!hasmore) {
         //log.info("set EOF %s", eof);
         p.endoffile = eof;
      }
      //log.info("fillBuffer end offset %d bufferpos %d pos %d", offset, bufferpos, p.pos);
      return shift;
   }

   public int[] readCIntIncr() throws EOFException {
      int value[] = readCIntArray();
      for (int i = 1; i < value.length; i++) {
         value[i] += value[i - 1];
      }
      return value;
   }

   class pos {

      int pos;
      StringBuilder s = new StringBuilder();
      EOFException endoffile;

      public pos(int pos) {
         this.pos = pos;
         if (!hasMore()) {
            endoffile = eof;
         }
      }
   }

   /**
    * Checks if next in the buffer the start-of-field is found. Spaces in the
    * sof sequence, matchWS zero or more whitespace characters.
    * <p/>
    * @param sof start-of-field sequence
    * @return true if the at the current position the buffer matches
    * start-of-field
    */
   public boolean peekStringExists(byte[] sof, pos p) throws EOFException {
      //log.info("peekString( %s )", new String(sof));
      int match = 0;
      int pos = p.pos;
      for (match = 0; match < sof.length;) {
         if (pos >= end) {
            pos -= fillBuffer(p);
         }
         if (buffer[pos] == sof[match]) {
            match++;
            pos++;
         } else {
            break;
         }
      }
      //log.info("end peekString( %s ) end %b", new String(sof), match >= sof.length);
      return (match >= sof.length);
   }

   public boolean peekStringExists(byte[] sof) throws EOFException {
      return sof.length == 0 || peekStringExists(sof, new pos(bufferpos));
   }

   public boolean peekStringNotExists(byte[] sof) throws EOFException {
      return sof.length == 0 || !peekStringExists(sof, new pos(bufferpos));
   }

   public boolean peekStringNotExists(byte[] sof, pos p) throws EOFException {
      return sof.length == 0 || !peekStringExists(sof, p);
   }

   /**
    * Checks if next in the buffer the start-of-field is found. Spaces in the
    * sof sequence, matchWS zero or more whitespace characters.
    * <p/>
    * @param sof start-of-field sequence
    * @return true if the at the current position the buffer matches
    * start-of-field
    */
   public boolean peekStringExistsWS(byte[] sof, pos p) throws EOFException {
      //log.info("peekStringWS( %s )", new String(sof));
      int match = 0;
      int pos = p.pos;
      for (match = 0; match < sof.length;) {
         if (pos >= end) {
            //log.info("filling buffer");
            pos -= fillBuffer(p);

            //log.info("after fillbuffer %d '%s'", bufferpos, new String(buffer, 0, 50));
         }
         //log.info("peekStringExists %d '%s' buffer %d '%s' '%s'", match, new String(sof, match, 1), pos, new String(buffer, pos, 1),
         //        new String(buffer, p.pos, Math.min(40, end -p.pos)));
         if (whitespace[sof[match]]) {
            if (buffer[pos] >= 0 && whitespace[buffer[pos]]) {
               pos++;
            } else {
               match++;
            }
         } else if (sametext(buffer[pos], sof[match])) {
            match++;
            pos++;
         } else {
            break;
         }
      }
      for (; match < sof.length && whitespace[sof[match]]; match++);
      //log.info("peekStringWS( %s ) return %b", new String(sof), match >= sof.length);
      return (match >= sof.length);
   }

   public boolean peekStringExistsWS(byte[] sof) throws EOFException {
      return sof.length == 0 || peekStringExistsWS(sof, new pos(bufferpos));
   }

   public boolean peekStringNotExistsWS(byte[] sof) throws EOFException {
      return sof.length == 0 || !peekStringExistsWS(sof, new pos(bufferpos));
   }

   public boolean peekStringNotExistsWS(byte[] sof, pos p) throws EOFException {
      return sof.length == 0 || !peekStringExistsWS(sof, p);
   }

   /**
    * reads past the first occurrence of end-of-field. Whitespaces in the eof
    * sequence, matchWS zero or more whitespace characters.
    * <p/>
    * @param eof end-of-field sequence
    * @throws EOFException
    */
   public void skipString(byte[] eof, byte escape) throws EOFException {
      //log.info("skipString()");
      int match = 0;
      int current = bufferpos;
      pos p = new pos(bufferpos);
      while (match < eof.length) {
         if (current >= end) {
            current -= fillBuffer(p);
         }
         if (buffer[current] == eof[match]) {
            current++;
            if (++match == eof.length) {
               break;
            }
         } else if (buffer[current] == escape) {
            p.pos++;
            fillBuffer(p);
            current = ++p.pos;
            match = 0;
         } else {
            match = 0;
            current = ++p.pos;
         }
      }
      bufferpos = current + match;
      //log.info("end skipString()");
   }

   /**
    * reads past the first occurrence of end-of-field. Whitespaces in the eof
    * sequence, matchWS zero or more whitespace characters.
    * <p/>
    * @param eof end-of-field sequence
    * @throws EOFException
    */
   public void skipString(byte[] eof, byte peekend[], byte escape) throws EOFException {
      //log.info("skipString(eof peekend, escape)");
      int match = 0;
      int current = bufferpos;
      pos p = new pos(bufferpos);
      while (match < eof.length) {
         if (!peekStringNotExistsWS(peekend, p)) {
            current = p.pos;
            break;
         }
         current = p.pos + match;
         if (buffer[current] == eof[match]) {
            current++;
            if (++match == eof.length) {
               break;
            }
         } else if (buffer[current] == escape) {
            p.pos++;
            fillBuffer(p);
            current = ++p.pos;
            match = 0;
         } else {
            match = 0;
            current = ++p.pos;
         }
      }
      bufferpos = current;
      //log.info("end skipString(eof peekend, escape)");
   }

   /**
    * reads past the first occurrence of end-of-field. Whitespaces in the eof
    * sequence, matchWS zero or more whitespace characters.
    * <p/>
    * @param eof end-of-field sequence
    * @throws EOFException
    */
   public void skipStringWS(byte[] eof, byte escape) throws EOFException {
      //log.info("skipStringWS( %s )", new String(eof));
      int match = 0;
      int current2 = bufferpos;
      pos p = new pos(bufferpos);
      while (match < eof.length) {
         // log.info("match eof %d '%s' buffer %d '%s'", match, new String(eof, match, 1), current2, new String(buffer, current2, 1));
         if (current2 >= end) {
            bufferpos = p.pos;
            current2 -= fillBuffer(p);
         }
         if (buffer[current2] == escape) {
            ++p.pos;
            fillBuffer(p);
            current2 = ++p.pos;
            match = 0;
         } else if (whitespace[eof[match]]) {
            if (buffer[current2] >= 0 && whitespace[buffer[current2]]) {
               current2++;
            } else {
               match++;
            }
         } else if (sametext(buffer[current2], eof[match])) {
            match++;
            current2++;
         } else {
            current2 = ++p.pos;
            match = 0;
         }
      }
      bufferpos = current2;
      //log.info("end skipStringWS( %s )", new String(eof));
   }

   /**
    * reads past the first occurrence of end-of-field. Whitespaces in the eof
    * sequence, matchWS zero or more whitespace characters.
    * <p/>
    * @param eof end-of-field sequence
    * @throws EOFException
    */
   public void skipStringWS(byte[] eof, byte peekend[], byte escape) throws EOFException {
      //log.info("skipStringWS( eof, peekend, escape )");
      if (peekend.length == 0) {
         skipStringWS(eof, escape);
      } else {
         int match = 0;
         int current2 = bufferpos;
         pos p = new pos(bufferpos);
         while (match < eof.length && peekStringNotExistsWS(peekend, p)) {
            // log.info("match eof %d '%s' buffer %d '%s'", match, new String(eof, match, 1), current2, new String(buffer, current2, 1));
            if (current2 >= end) {
               bufferpos = p.pos;
               current2 -= fillBuffer(p);
            }
            if (buffer[current2] == escape) {
               ++p.pos;
               fillBuffer(p);
               current2 = ++p.pos;
               match = 0;
            } else if (whitespace[eof[match]]) {
               if (buffer[current2] >= 0 && whitespace[buffer[current2]]) {
                  current2++;
               } else {
                  match++;
               }
            } else if (sametext(buffer[current2], eof[match])) {
               match++;
               current2++;
            } else {
               current2 = ++p.pos;
               match = 0;
            }
         }
         bufferpos = current2;
      }
      //log.info("end skipStringWS( %s )", new String(eof));
   }
}
