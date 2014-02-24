package io.github.repir.tools.Content;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Content.StructuredTagStream2.Field;
import io.github.repir.tools.Lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * This class wraps three ways to read/write data to a file: read the whole file
 * buffered in sequence, write the whole file buffered in sequence or random
 * access (go to a position and start reading or writing from there).
 * <p/>
 * @author Jeroen
 */
public class Datafile implements StructureReader, StructureWriter, Comparable<Object> {

   public static Log log = new Log(Datafile.class);         
   
   public enum Status {

      CLOSED,
      READ,
      WRITE,
      APPEND
   }
   private String filename;
   protected String tempdir;
   protected boolean intemp = false;
   private int buffersize = 4096;
   protected FileSystem fs;
   public Status status = Status.CLOSED;
   protected boolean hasbeenopened = false;
   public final BufferReaderWriter rwbuffer = new BufferReaderWriter();
   protected ArrayList<Integer> intlist = new ArrayList<Integer>();

   protected Datafile(String filename, Status status) {
      this.filename = filename;
      open(status);
   }

   public Datafile(String filename) {
      this.filename = filename;
   }

   protected Datafile(String filename, String dir) {
      this.filename = filename;
      tempdir = dir;
      intemp = true;
   }

   public Datafile(FileSystem fs, String filename) {
      this.fs = fs;
      this.filename = filename;
   }

   public Datafile(FileSystem fs, Path path) {
      this.fs = fs;
      this.filename = path.toString();
   }

   public Datafile(Datafile df) {
      this.fs = df.fs;
      this.filename = df.filename;
   }

   public Datafile(Datafile df, String suffix) {
      this.fs = df.fs;
      this.filename = df.filename + "." + suffix;
   }

   protected Datafile(FSDataInputStream in, long offset, long end) {
      rwbuffer.setDataIn(new HDFSIn(in));
      rwbuffer.setOffset(offset);
      rwbuffer.setCeiling(end);
      rwbuffer.setBufferSize(buffersize);
      status = Status.READ;
   }

   public void readBuffer(DataInput in) {
      rwbuffer.readBuffer(in);
   }

   public void writeBuffer(DataOutput out) {
      rwbuffer.writeBuffer(out);
   }

   public String readString(byte[] eof, byte escape) throws EOFException {
      return rwbuffer.readString(eof, escape);
   }

   public String readString(ByteRegex eof) throws EOFException {
      return rwbuffer.readString(eof);
   }

   public boolean peekStringExists(byte[] eof) throws EOFException {
      return rwbuffer.peekStringExists(eof);
   }

   public boolean peekStringNotExists(byte[] eof) throws EOFException {
      return rwbuffer.peekStringNotExists(eof);
   }

   public boolean peekStringExists(ByteRegex eof) throws EOFException {
      return rwbuffer.peekStringExists(eof);
   }

   public void skipFirst(ByteRegex eof) throws EOFException {
      rwbuffer.skipFirst(eof);
   }

   public boolean peekStringNotExists(ByteRegex eof) throws EOFException {
      return rwbuffer.peekStringNotExists(eof);
   }

   public void skipString(byte[] eof, byte escape) throws EOFException {
      rwbuffer.skipString(eof, escape);
   }

   public void skipString(ByteRegex eof) throws EOFException {
      rwbuffer.skipString(eof);
   }

   public String readStringWS(byte[] eof, byte escape) throws EOFException {
      return rwbuffer.readStringWS(eof, escape);
   }

   public boolean peekStringExistsWS(byte[] eof) throws EOFException {
      return rwbuffer.peekStringExistsWS(eof);
   }

   public boolean peekStringNotExistsWS(byte[] eof) throws EOFException {
      return rwbuffer.peekStringNotExistsWS(eof);
   }

   public void skipStringWS(byte[] eof, byte escape) throws EOFException {
      rwbuffer.skipString(eof, escape);
   }

   public void write(byte[] b, byte[] esc, byte escape) {
      rwbuffer.write(b, esc, escape);
   }

   public void write(byte[] b, byte[] esc, byte esc2[], byte escape) {
      rwbuffer.write(b, esc, esc2, escape);
   }

   public String readString(byte[] eof, byte[] peekend, byte escape) throws EOFException {
      return rwbuffer.readString(eof, peekend, escape);
   }

   public String readStringWS(byte[] eof, byte[] peekend, byte escape) throws EOFException {
      return rwbuffer.readStringWS(eof, peekend, escape);
   }

   public void skipString(byte[] eof, byte[] peekend, byte escape) throws EOFException {
      rwbuffer.skipString(eof, peekend, escape);
   }

   public void skipStringWS(byte[] eof, byte[] peekend, byte escape) throws EOFException {
      rwbuffer.skipStringWS(eof, peekend, escape);
   }

   public int[] readCIntIncr() throws EOFException {
      return rwbuffer.readCIntIncr();
   }

   public void writeIncr(int[] s) {
      rwbuffer.writeIncr(s);
   }

   public void writeIncr(ArrayList<Integer> list) {
      rwbuffer.writeIncr(list);
   }

   public void delete() {
      if (fs != null) {
         try {
            fs.delete(new Path(filename), false);
         } catch (IOException ex) {
            log.exception(ex, "delete() when deleting file %s using FileSystem %s", filename, fs);
         }
      } else {
         new FSFile(filename).delete();
      }
   }

   public void closeRead() {
      close();
   }

   public void closeWrite() {
      this.close();
   }

   public boolean exists() {
      if (this.fs != null) {
         return HDFSDir.isFile(fs, new Path(filename));
      } else {
         return FSFile.exists(filename);
      }
   }

   public String getFilename() {
      return filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);
   }

   public String getFullPath() {
      return filename;
   }

   public void setFileSystem(FileSystem fs) {
      this.fs = fs;
   }

   public Datafile getSubFile(String ext) {
      Datafile df = new Datafile(fs, this.filename + ext);
      return df;
   }

   public Datafile otherDir(Dir dir) {
      Datafile df = new Datafile(fs, dir.getFilename(this.getFilename()));
      return df;
   }

   public FileSystem getFileSystem() {
      return fs;
   }

   public Dir getDir() {
      int dirpos = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
      if (fs != null) {
         if (HDFSDir.isDir(fs, new Path(this.filename))) {
            log.info("1");
            return new HDFSDir(fs, filename);
         } else {
            log.info("2 %s", filename.substring(0, dirpos));
            return new HDFSDir(fs, filename.substring(0, dirpos));
         }
      } else {
         if (FSDir.isDir(filename)) {
            return new FSDir(filename);
         } else {
            return new FSDir(filename.substring(0, dirpos));
         }
      }
   }

   @Override
   public void setCeiling(long ceiling) {
      //openRead();
      rwbuffer.setCeiling(ceiling);
   }

   @Override
   public void setOffset(long offset) {
      //openRead();
      rwbuffer.setOffset(offset);
   }

   public void setAppendOffset(long offset) {
      rwbuffer.setAppendOffset(offset);
   }

   public void close() {
      switch (status) {
         case WRITE:
            rwbuffer.closeWrite();
            break;
         case READ:
            rwbuffer.closeRead();
            break;
      }
      //rwbuffer.setDataIn( null );
      status = Status.CLOSED;
      hasbeenopened = false;
   }

   public void openReadKeepOffset() {
      long offset = rwbuffer.offset;
      long ceiling = rwbuffer.ceiling;
      openRead();
      setOffset(offset);
      setCeiling(ceiling);
   }

   public void resetOffsetCeiling() {
      rwbuffer.offset = rwbuffer.bufferpos = 0;
      rwbuffer.setEnd(0);
      rwbuffer.ceiling = Long.MAX_VALUE;
   }

   public void openWrite() {
      open(Status.WRITE);
   }

   public void openAppend() {
      open(Status.APPEND);
   }

   public boolean isClosed() {
      return status == Status.CLOSED;
   }
   
   public boolean isReadOpen() {
      return status == Status.READ;
   }
   
   public boolean isWriteOpen() {
      return status == Status.WRITE;
   }
   
   public void openRead() {
      if (status == Status.READ) {
         return;
      }
      if (status == Status.WRITE) {
         close();
      }
      //log.info("open() %b %b offset %d ceiling %d", this.getOffset(), this.getCeiling());
      open(Status.READ);
      //log.info("open() offset %d ceiling %d", this.getOffset(), this.getCeiling());
   }

   @Override
   public void setBufferSize(int buffersize) {
      this.buffersize = buffersize;
      if (rwbuffer != null)
         rwbuffer.setBufferSize(buffersize);
   }
   
   @Override
   public int getBufferSize() {
      return rwbuffer.getBufferSize();
   }

   @Override
   public void fillBuffer() throws EOFException {
      this.rwbuffer.fillBuffer();
   }
   
   @Override
   public void reset() {
      this.rwbuffer.reset();
   }
   
   protected void open(Status newstatus) {
      //log.info("openFirst %s %s", newstatus, this.getfilename());
      if (status == newstatus) {
         return;
      }
      if (status != Status.CLOSED) {
         close();
      }
      switch (newstatus) {
         case CLOSED:
            status = Status.CLOSED;
            break;
         case READ:
            if (fs == null) {
               if (intemp) {
                  rwbuffer.setDataIn(new FSFileInBuffer(FSFile.tempfilename(filename, tempdir)));
               } else {
                  rwbuffer.setDataIn(new FSFileInBuffer(filename));
               }
            } else {
               //log.info("openRead() %s %d %b", filename, rwbuffer.getBufferSize(), this.exists());
               rwbuffer.setDataIn(new HDFSIn(fs, filename));
            }
            rwbuffer.setBufferSize(buffersize);
            status = newstatus;
            break;
         case WRITE:
            if (fs == null) {
               if (intemp) {
                  rwbuffer.setDataOut(new FSFileOutBuffer(FSFile.tempfilename(filename, tempdir)));
               } else {
                  rwbuffer.setDataOut(new FSFileOutBuffer(filename));
               }
            } else {
               rwbuffer.setDataOut(new HDFSOut(fs, filename, buffersize));
            }
            rwbuffer.setBufferSize(buffersize);
            rwbuffer.openWrite();
            status = Status.WRITE;
            break;
         case APPEND:
            if (fs != null) {
               rwbuffer.setDataOut(new HDFSOut(fs, filename, buffersize));
               rwbuffer.openWrite();
               rwbuffer.openAppend();
               status = Status.WRITE;
            }
      }
      hasbeenopened = true;
   }

   /**
    * determines if end of file has not been reached in either buffered reading
    * or random access mode.
    * <p/>
    * @return true if EOF has not been reached false if end of file has been
    * reached, in case of an exception, or if the file was not in buffered
    * reading or random reading mode
    */
   public boolean hasMore() {
      //log.info("hasmore %s", status);
      if (status == Status.READ) {
         return rwbuffer.hasMore();
      }
      return false;
   }

   public void flush() {
      rwbuffer.flushBuffer();
   }

   public void write(Map<String, String> map) {
      rwbuffer.write(map);
   }

   public Map<String, String> readStringPairMap() throws EOFException {
      return rwbuffer.readStringPairMap();
   }

   public void skip(int bytes) {
      rwbuffer.skip(bytes);
   }

   public void skipInt() {
      rwbuffer.skipInt();
   }

   public void skipLong() {
      rwbuffer.skipInt();
   }

   public void skipDouble() {
      rwbuffer.skipDouble();
   }

   public void skipByte() {
      rwbuffer.skipByte();
   }

   public void skipByteBlock() throws EOFException {
      rwbuffer.skipByteBlock();
   }

   public void skipCInt() throws EOFException {
      rwbuffer.skipCInt();
   }

   public void skipCLong() throws EOFException {
      rwbuffer.skipCLong();
   }

   public void skipCDouble() throws EOFException {
      rwbuffer.skipCDouble();
   }

   public void skipString() throws EOFException {
      rwbuffer.skipString();
   }

   public void skipStringBuilder() throws EOFException {
      rwbuffer.skipStringBuilder();
   }

   public void skipString0() throws EOFException {
      rwbuffer.skipString0();
   }

   public void skipIntArray() throws EOFException {
      rwbuffer.skipIntArray();
   }

   public void skipSquaredIntArray2() throws EOFException {
      rwbuffer.skipSquaredIntArray2();
   }

   public void skipLongArray() throws EOFException {
      rwbuffer.skipLongArray();
   }

   public void skipCLongArray2() throws EOFException {
      rwbuffer.skipCLongArray2();
   }

   public void skipDoubleArray() throws EOFException {
      rwbuffer.skipDoubleArray();
   }

   public void skipDoubleSparse() throws EOFException {
      rwbuffer.skipDoubleSparse();
   }

   public void skipStringArray() throws EOFException {
      rwbuffer.skipStringArray();
   }

   public void skipCIntArray() throws EOFException {
      rwbuffer.skipCIntArray();
   }

   public void skipCLongArray() throws EOFException {
      rwbuffer.skipCLongArray();
   }

   public void skipLongSparse() throws EOFException {
      rwbuffer.skipLongSparse();
   }

   public void skipIntSparse() throws EOFException {
      rwbuffer.skipIntSparse();
   }

   public void skipStringPairMap() throws EOFException {
      rwbuffer.skipStringPairMap();
   }

   public void setDataIn(DataIn in) {
      this.rwbuffer.setDataIn(in);
      rwbuffer.setBufferSize(buffersize);
   }

   public void setDataOut(DataOut out) {
      this.rwbuffer.setDataOut(out);
      rwbuffer.setBufferSize(buffersize);
   }

   public ArrayList<Integer> readCIntArrayList() throws EOFException {
      return this.rwbuffer.readCIntArrayList();
   }

   public ArrayList<Integer> readIntArrayList() throws EOFException {
      return this.rwbuffer.readIntArrayList();
   }

   public ArrayList<String> readStrArrayList() throws EOFException {
      return this.rwbuffer.readStrArrayList();
   }

   public void writeC(ArrayList<Integer> s) {
      rwbuffer.writeC(s);
   }

   public void writeStr(Collection<String> s) {
      rwbuffer.writeStr(s);
   }

   public int compareTo(Object o) {
      return this.getFullPath().compareTo(((Datafile) o).getFullPath());
   }

   public int[][] readIntSparse2() throws EOFException {
      return rwbuffer.readIntSparse2();
   }

   public void skipIntSparse2() throws EOFException {
      rwbuffer.skipIntSparse2();
   }

   public void writeSparse(int[][] i) {
      rwbuffer.writeSparse(i);
   }

   public int[][][] readIntSparse3() throws EOFException {
      return rwbuffer.readIntSparse3();
   }

   public void skipIntSparse3() throws EOFException {
      rwbuffer.skipIntSparse3();
   }

   public void writeSparse(int[][][] i) {
      rwbuffer.writeSparse(i);
   }

   public int[][][] readSquaredIntArray3() throws EOFException {
      return rwbuffer.readSquaredIntArray3();
   }

   public void skipSquaredIntArray3() throws EOFException {
      rwbuffer.skipSquaredIntArray3();
   }

   public void writeSquared(int[][][] i) {
      rwbuffer.writeSquared(i);
   }

   public long[][] readLongSparse2() throws EOFException {
      return rwbuffer.readLongSparse2();
   }

   public void skipLongSparse2() throws EOFException {
      rwbuffer.skipLongSparse2();
   }

   public void writeSparse(long[][] l) {
      rwbuffer.writeSparse(l);
   }

   public int[][] readCIntArray2() throws EOFException {
      return rwbuffer.readCIntArray2();
   }

   public void skipCIntArray2() throws EOFException {
      rwbuffer.skipCIntArray2();
   }

   public void writeC(int[][] i) {
      rwbuffer.writeC(i);
   }

   public int[][][] readCIntArray3() throws EOFException {
      return rwbuffer.readCIntArray3();
   }

   public void skipCIntArray3() throws EOFException {
      rwbuffer.skipCIntArray3();
   }

   public void writeC(int[][][] i) {
      rwbuffer.writeC(i);
   }

   /**
    * reads a 4 byte int from the current file position. The file position
    * advances by 4. If the file was not in buffered reading or random access
    * mode, the file is closed and opened in buffered reading mode.
    * <p/>
    * @return int
    */
   public int readInt() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readInt();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   public int readInt2() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readInt2();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   public int readInt3() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readInt3();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   public double readDouble() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readDouble();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   /**
    * reads an 8 byte long from the current file position. The file position
    * advances by 8. If the file was not in buffered reading or random access
    * mode, the file is closed and opened in buffered reading mode.
    * <p/>
    * @return
    */
   public long readLong() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readLong();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   public long readCLong() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readCLong();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   public long[] readLongArray() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readLongArray();
      }
      return null;
   }

   public long[][] readCLongArray2() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readCLongArray2();
      }
      return null;
   }

   public String[] readStringArray() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readStringArray();
      }
      return null;
   }

   public long[] readCLongArray() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readCLongArray();
      }
      return null;
   }

   public long[] readLongSparse() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readLongSparse();
      }
      return null;
   }

   public int[] readIntSparse() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readIntSparse();
      }
      return null;
   }

   public double[] readDoubleSparse() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readDoubleSparse();
      }
      return null;
   }

   public HashMap<Integer, Long> readSparseLongMap() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readSparseLongMap();
      }
      return null;
   }

   public HashMap<Integer, Integer> readSparseIntMap() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readSparseIntMap();
      }
      return null;
   }

   public double readCDouble() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readCDouble();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   public int readCInt() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readCInt();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   public int[] readIntArray() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readIntArray();
      }
      return null;
   }

   public int[][] readSquaredIntArray2() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readSquaredIntArray2();
      }
      return null;
   }

   public int[] readCIntArray() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readCIntArray();
      }
      return null;
   }

   /**
    * reads 1 byte from the current file position. The file position advances by
    * 1. If the file was not in buffered reading or random access mode, the file
    * is closed and opened in buffered reading mode.
    */
   public int readByte() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readByte();
      }
      return io.github.repir.tools.Lib.Const.NULLINT;
   }

   public byte[] readByteBlock() throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readByteBlock();
      }
      return null;
   }

   public boolean readBoolean() throws EOFException {
      return readByte() == 0 ? false : true;
   }

   public byte[] readBytes(int count) throws EOFException {
      switch (status) {
         case READ:
            return rwbuffer.readBytes(count);
      }
      return null;
   }

   public int readBytes(long offset, byte[] b, int pos, int len) {
      switch (status) {
         case READ:
            return rwbuffer.readBytes(offset, b, pos, len);
      }
      return 0;
   }

   public int readBytes(byte[] b, int pos, int len) {
      switch (status) {
         case READ:
            return rwbuffer.readBytes(rwbuffer.getOffset(), b, pos, len);
      }
      return 0;
   }

   public byte[] readFully() {
      switch (status) {
         case READ:
            byte[] b = new byte[(int) this.getLength()];
            rwbuffer.readBytes(0, b, 0, (int) this.getLength());
            return b;
      }
      return null;
   }

   public static int[] readInts(String filename, long offset, int count) {
      FileInputStream df = null;
      try {
         df = new FileInputStream(filename);
         byte b[] = new byte[count * 4];
         df.skip(offset);
         df.read(b);
         int i[] = new int[count];
         for (int j = 0; j < count; j++) {
            int o = j * 4;
            i[j] = ((b[o] & 0xFF) << 24)
                    + ((b[o + 1] & 0xFF) << 16)
                    + ((b[o + 2] & 0xFF) << 8)
                    + (b[o + 3] & 0xFF);
         }
         df.close();
         return i;
      } catch (Exception ex) {
         log.exception(ex, "readInts( %s, %d, %d) when reading file", filename, offset, count);
      }
      return null;
   }

   /**
    * reads a 4 byte int that indicates the String length from the current file
    * position. Then reads as many bytes as the length dictates into a String.
    * The file position advances by 4 + length of the String. If the file was
    * not in buffered reading or random access mode, the file is closed and
    * opened in buffered reading mode.
    */
   public String readString() throws EOFException {
      return rwbuffer.readString();
   }

   public StringBuilder readStringBuilder() throws EOFException {
      return rwbuffer.readStringBuilder();
   }

   public String readString(int length) throws EOFException {
      return rwbuffer.readString(length);
   }

   /**
    * writes a 4 byte int to the file at the current file position. The file
    * position advances by 4. This only works in buffered writing or random
    * access mode.
    * <p/>
    * @param i
    */
   public void write(int i) {
      if (status == Status.WRITE) {
         rwbuffer.write(i);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write2(int i) {
      if (status == Status.WRITE) {
         rwbuffer.write2(i);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write3(int i) {
      if (status == Status.WRITE) {
         rwbuffer.write3(i);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   /**
    * writes 1 byte to the file at the current file position. The file position
    * advances by 1. This only works in buffered writing or random access mode.
    * <p/>
    * @param b
    */
   public void write(byte b) {
      if (status == Status.WRITE) {
         rwbuffer.write(b);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write(boolean b) {
      write((byte) (b ? -1 : 0));
   }

   public void writeUB(int b) {
      write((byte) ((b > 255) ? 255 : (b & 0xFF)));
   }

   /**
    * writes an 8 byte long to the file at the current file position. The file
    * position advances by 8. This only works in buffered writing or random
    * access mode.
    * <p/>
    * @param l
    */
   public void write(long l) {
      if (status == Status.WRITE) {
         rwbuffer.write(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeC(double l) {
      if (status == Status.WRITE) {
         rwbuffer.writeC(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeC(long l) {
      if (status == Status.WRITE) {
         rwbuffer.write(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write(long l[]) {
      if (status == Status.WRITE) {
         rwbuffer.write(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeC(long l[][]) {
      if (status == Status.WRITE) {
         rwbuffer.writeC(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeSquared(int i[][]) {
      if (status == Status.WRITE) {
         rwbuffer.writeSquared(i);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write(String l[]) {
      if (status == Status.WRITE) {
         rwbuffer.write(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeC(long l[]) {
      if (status == Status.WRITE) {
         rwbuffer.writeC(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeSparse(long l[]) {
      if (status == Status.WRITE) {
         rwbuffer.writeSparse(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeSparse(double l[]) {
      if (status == Status.WRITE) {
         rwbuffer.writeSparse(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeSparseLong(Map<Integer, Long> l) {
      if (status == Status.WRITE) {
         rwbuffer.writeSparseLong(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeSparseInt(Map<Integer, Integer> l) {
      if (status == Status.WRITE) {
         rwbuffer.writeSparseInt(l);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeC(int i) {
      if (status == Status.WRITE) {
         rwbuffer.writeC(i);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write(int i[]) {
      if (status == Status.WRITE) {
         rwbuffer.write(i);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write(Collection<Integer> al) {
      if (status == Status.WRITE) {
         rwbuffer.write(al);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeC(int i[]) {
      if (status == Status.WRITE) {
         rwbuffer.writeC(i);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write(double d) {
      if (status == Status.WRITE) {
         rwbuffer.write(d);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   /**
    * writes the string length as a 4 byte int to the file at the current file
    * position. Then writes the amount of bytes the string consists of. Every
    * character is written as a single byte. The file position advances by 4 +
    * string length. This only works in buffered writing or random access mode.
    * <p/>
    * @param s
    */
   public void write(String s) {
      if (status == Status.WRITE) {
         rwbuffer.write(s);
      } else {
         log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getFullPath());
      }
   }

   /**
    * writes the StringBuilder as {@link #write(java.lang.String) }
    * <p/>
    * @param s
    */
   public void write(StringBuilder s) {
      if (status == Status.WRITE) {
         rwbuffer.write(s);
      } else {
         log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getFullPath());
      }
   }

   @Override
   public String readString0() throws EOFException {
      if (status == Status.READ) {
         return rwbuffer.readString0();
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
      return null;
   }

   public String readUntil(ByteRegex regex) throws EOFException {
      if (status == Status.READ) {
         return rwbuffer.readUntil(regex);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
      return null;
   }

   public String read(ByteRegex regex) throws EOFException {
      if (status == Status.READ) {
         return rwbuffer.read(regex);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
      return null;
   }

   public void skipAfter(ByteRegex regex) throws EOFException {
      if (status == Status.READ) {
         rwbuffer.skipAfter(regex);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void skipBefore(ByteRegex regex) throws EOFException {
      if (status == Status.READ) {
         rwbuffer.skipBefore(regex);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public ByteRegex.Pos find(ByteRegex regex) throws EOFException {
      if (status == Status.READ) {
         return rwbuffer.find(regex);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
      return null;
   }
   
   @Override
   public void write0(String s) {
      if (status == Status.WRITE) {
         rwbuffer.write0(s);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public double[] readDoubleArray() throws EOFException {
      if (status == Status.READ) {
         return rwbuffer.readDoubleArray();
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
      return null;
   }

   public void write(double[] s) {
      if (status == Status.WRITE) {
         rwbuffer.write(s);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write(byte bytes[]) {
      if (status == Status.WRITE) {
         rwbuffer.write(bytes);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeByteBlock(byte bytes[]) {
      if (status == Status.WRITE) {
         rwbuffer.writeByteBlock(bytes);
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void write(byte bytes[], int pos, int endpos) {
      if (status == Status.WRITE) {
         for (; pos < endpos; pos++) {
            rwbuffer.write(bytes[pos]);
         }
      } else {
         log.fatal("DataFile has to be put in a specific write mode before writing");
      }
   }

   public void writeSparse(int[] l) {
      rwbuffer.writeSparse(l);
   }

   public void writeSparse(int[] l, int offset, int length) {
      rwbuffer.writeSparse(l, offset, length);
   }

   /**
    * returns the current file position of the DataFile.
    * <p/>
    * @return current file position or -1 if closed or an exception occurred.
    */
   public long getOffset() {
      return rwbuffer.getOffset();
   }

   public long getCeiling() {
      return rwbuffer.getCeiling();
   }

   public long getLength() {
      if (fs == null) {
         if (intemp) {
            return new FSFile(FSFile.tempfilename(filename, tempdir)).getLength();
         } else {
            return new FSFile(filename).getLength();
         }
      } else {
         return HDFSIn.getLength(fs, new Path(filename));
      }
   }

   public void printf(String format, Object... args) {
      this.openWrite();
      String s = io.github.repir.tools.Lib.PrintTools.sprintf(format, args);
      //log.info("printf( %s )", s);
      rwbuffer.print(s);
   }

   public String readAsString() {
      this.openRead();
      byte b[] = new byte[(int) this.getLength()];
      this.readBytes(b, 0, (int) this.getLength());
      this.close();
      return new String(b);
   }

   public InputStream getInputStream() {
      openRead();
      return this.rwbuffer.datain.getInputStream();
   }

   public OutputStream getOutputStream() {
      openWrite();
      return this.rwbuffer.dataout.getOutputStream();
   }
}
