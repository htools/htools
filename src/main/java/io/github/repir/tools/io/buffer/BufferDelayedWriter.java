package io.github.repir.tools.io.buffer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.repir.tools.io.struct.StructureWriter;
import io.github.repir.tools.lib.Log;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

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
public class BufferDelayedWriter implements StructureWriter {

   private static Log log = new Log(BufferDelayedWriter.class);
   private static Gson gson = new Gson();
   protected BufferReaderWriter writer = new BufferReaderWriter();
   public ArrayList<Object> list = new ArrayList<Object>();
   public boolean[] whitespace = io.github.repir.tools.lib.ByteTools.getByteArray(" \n\t\r");
   public int size = 0;

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
      list.clear();
      size = 0;
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

   @Override
   public void write(byte b[]) {
       if (b == null)
           write(-1);
       else {
           write(b.length);
           list.add(b);
           size += b.length;
       }
   }

//   public void write(byte b[], byte escape) {
//      StringBuilder sb = new StringBuilder();
//      for (int i = 0; i < b.length; i++) {
//         if (b[i] == escape) {
//            sb.append((char) escape);
//         }
//         sb.append((char) (b[i]));
//      }
//      list.add(sb.toString());
//   }
//
//   @Override
//   public void write(byte b[], byte esc[], byte escape) {
//      if (esc.length == 0) {
//         write(b, escape);
//      } else {
//         StringBuilder sb = new StringBuilder();
//         for (int i = 0; i < b.length; i++) {
//            if (b[i] == escape || io.github.repir.tools.Lib.ByteTools.matchStringWS(b, esc, i)) {
//               sb.append((char) escape);
//            }
//            sb.append((char) (b[i]));
//         }
//         list.add(sb.toString());
//      }
//   }
//
//   @Override
//   public void write(byte b[], byte esc[], byte esc2[], byte escape) {
//      if (esc.length == 0) {
//         write(b, esc2, escape);
//      } else if (esc2.length == 0) {
//         write(b, esc, escape);
//      } else {
//         StringBuilder sb = new StringBuilder();
//         for (int i = 0; i < b.length; i++) {
//            if (b[i] == escape || io.github.repir.tools.Lib.ByteTools.matchStringWS(b, esc, i) || io.github.repir.tools.Lib.ByteTools.matchStringWS(b, esc2, i)) {
//               sb.append((char) escape);
//            }
//            sb.append((char) (b[i]));
//         }
//         list.add(sb.toString());
//      }
//   }

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
         byte b[] = s.getBytes();
         write(b.length);
         writeRaw(b);
      }
   }

   public void write(JsonObject s) {
        BufferDelayedWriter.this.write(s == null?null:s.toString());
   }

   public void write(Object s, Type type) {
       if (s == null)
           BufferDelayedWriter.this.write((String)null);
       else {
            BufferDelayedWriter.this.write(gson.toJson(s, type));
       }
   }

   public void write0(JsonObject s) {
      write0(s == null?null:s.toString());
   }

//   /**
//    * writes an escaped String. If the eof, eol or escape character appears
//    * inside the String, these are preceded by an escape character. The String
//    * is terminated by an eof. If this is the last field in the line, call this
//    * function with eol and eof swapped.
//    * <p/>
//    * @param s the string to write to the output
//    * @param eof the end of field character to use
//    * @param escape the escape character to use
//    * @param eol an additional character to escape (usually the end of line
//    * character)
//    */
//   public void writeEscapedString(String s, byte eof, byte escape) {
//      if (s != null) {
//         byte bb[] = s.getBytes();
//         for (byte b : bb) {
//            if (b == escape || b == eof) {
//               write(escape);
//            }
//            write(b);
//         }
//      }
//      write(eof);
//   }

   @Override
   public void write(StringBuilder s) {
      if (s == null) {
            BufferDelayedWriter.this.write(-1);
      } else {
            BufferDelayedWriter.this.write(s.toString());
      }
   }

   @Override
   public void write(String array[]) {
      if (array == null) {
            BufferDelayedWriter.this.write(-1);
      } else {
            BufferDelayedWriter.this.write(array.length);
         for (String s : array) {
                BufferDelayedWriter.this.write(s);
         }
      }
   }

   @Override
   public void write(long array[]) {
      if (array == null) {
            BufferDelayedWriter.this.write(-1);
      } else {
            BufferDelayedWriter.this.write(array.length);
         for (long s : array) {
                BufferDelayedWriter.this.write(s);
         }
      }
   }

   @Override
   public void write(int array[]) {
      if (array == null) {
            BufferDelayedWriter.this.write(-1);
      } else {
            BufferDelayedWriter.this.write(array.length);
         for (int s : array) {
                BufferDelayedWriter.this.write(s);
         }
      }
   }

   @Override
   public void write(double i[]) {
      if (i == null) {
            BufferDelayedWriter.this.write(-1);
      } else {
            BufferDelayedWriter.this.write(i.length);
         for (double l : i) {
                BufferDelayedWriter.this.write(l);
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
         writeC(io.github.repir.tools.lib.ArrayTools.flatten(array));
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
         writeC(io.github.repir.tools.lib.ArrayTools.flatten(array));
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
            BufferDelayedWriter.this.write((byte) i);
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
        BufferDelayedWriter.this.write((byte) len);
      len = (len < -120) ? -(len + 120) : -(len + 112);
      for (int idx = len; idx != 0; idx--) {
         int shiftbits = (idx - 1) * 8;
         long mask = 0xFFL << shiftbits;
            BufferDelayedWriter.this.write((byte) ((i & mask) >> shiftbits));
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
            BufferDelayedWriter.this.write(mask);
         for (int s = i; s < i + 4; s++) {
            switch (m[s - i]) {
               case 3:
                  writeC(l[s]);
                  break;
               case 2:
                  BufferDelayedWriter.this.write((byte) ((l[s] >> 16) & 0xFF));
               case 1:
                  BufferDelayedWriter.this.write((byte) ((l[s] >> 8) & 0xFF));
               case 0:
                  BufferDelayedWriter.this.write((byte) (l[s] & 0xFF));
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
        BufferDelayedWriter.this.write(ltf);
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
                  BufferDelayedWriter.this.write((byte) ((l[s] >> 24) & 0xFF));
               case 2:
                  BufferDelayedWriter.this.write((byte) ((l[s] >> 16) & 0xFF));
               case 1:
                  BufferDelayedWriter.this.write((byte) ((l[s] >> 8) & 0xFF));
               case 0:
                  BufferDelayedWriter.this.write(((byte) (l[s] & 0xFF)));
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
            BufferDelayedWriter.this.write(mask);
         for (int s = 0; s < 4; s++) {
            switch (m[s]) {
               case 3:
                  BufferDelayedWriter.this.write((byte) ((v[s] >> 24) & 0xFF));
               case 2:
                  BufferDelayedWriter.this.write((byte) ((v[s] >> 16) & 0xFF));
               case 1:
                  BufferDelayedWriter.this.write((byte) ((v[s] >> 8) & 0xFF));
               case 0:
                  BufferDelayedWriter.this.write(((byte) (v[s] & 0xFF)));
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
        BufferDelayedWriter.this.write((byte) ((i >>> 8) & 0xFF));
        BufferDelayedWriter.this.write((byte) ((i) & 0xFF));
   }

   @Override
   public void write3(int i) {
        BufferDelayedWriter.this.write((byte) ((i >>> 16) & 0xFF));
        BufferDelayedWriter.this.write((byte) ((i >>> 8) & 0xFF));
        BufferDelayedWriter.this.write((byte) ((i) & 0xFF));
   }

   @Override
   public void writeUB(int i) {
        BufferDelayedWriter.this.write((byte) ((i) & 0xFF));
   }

   @Override
   public void write0(String s) {
      if (s != null) {
         writeRaw(s.getBytes());
      }
        BufferDelayedWriter.this.write((byte) 0);
   }

   @Override
   public void write(Collection<Integer> al) {
      if (al == null) {
            BufferDelayedWriter.this.write(-1);
      } else {
            BufferDelayedWriter.this.write(al.size());
         for (int l : al) {
                BufferDelayedWriter.this.write(l);
         }
      }
   }

   @Override
   public void writeStr(Collection<String> al) {
      if (al == null) {
            BufferDelayedWriter.this.write(-1);
      } else {
            BufferDelayedWriter.this.write(al.size());
         for (String l : al) {
                BufferDelayedWriter.this.write(l);
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
         writeSparse(io.github.repir.tools.lib.ArrayTools.flatten(array));
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
         writeSparse(io.github.repir.tools.lib.ArrayTools.flatten(array));
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
         writeSparse(io.github.repir.tools.lib.ArrayTools.flatten(array));
      }
   }

   @Override
   public void write(Map<String, String> map) {
        BufferDelayedWriter.this.write(map.size());
      for (Map.Entry<String, String> e : map.entrySet()) {
            BufferDelayedWriter.this.write(e.getKey());
            BufferDelayedWriter.this.write(e.getValue());
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
}
