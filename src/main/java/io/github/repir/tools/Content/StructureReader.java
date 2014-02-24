package io.github.repir.tools.Content;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Lib.Log;
import java.io.DataInput;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author jbpvuurens
 */
public interface StructureReader {

   public long getOffset();

   public void setOffset(long l);

   public void setCeiling(long s);

   public long getCeiling();

   public void skip(int bytes) throws EOFException;

   public void setBufferSize(int size);
   
   public void fillBuffer() throws EOFException;
   
   public void reset();

   public int getBufferSize();

   public int readInt() throws EOFException;

   public boolean readBoolean() throws EOFException;

   public int readInt2() throws EOFException;

   public int readInt3() throws EOFException;

   public void skipInt() throws EOFException;

   public long readLong() throws EOFException;

   public void skipLong() throws EOFException;

   public double readDouble() throws EOFException;

   public void skipDouble() throws EOFException;

   public int readByte() throws EOFException;

   public byte[] readBytes(int length) throws EOFException;

   public byte[] readByteBlock() throws EOFException;

   public void skipByte() throws EOFException;

   public void skipByteBlock() throws EOFException;

   public int readCInt() throws EOFException;

   public void skipCInt() throws EOFException;

   public long readCLong() throws EOFException;

   public void skipCLong() throws EOFException;

   public double readCDouble() throws EOFException;

   public void skipCDouble() throws EOFException;

   public String readString() throws EOFException;

   public String readString(byte eof[], byte escape) throws EOFException;

   public String readStringWS(byte eof[], byte escape) throws EOFException;

   public String readString(ByteRegex eof) throws EOFException;

   public String readString(byte eof[], byte peekend[], byte escape) throws EOFException;

   public String readStringWS(byte eof[], byte peekend[], byte escape) throws EOFException;

   public boolean peekStringExists(byte eof[]) throws EOFException;

   public boolean peekStringExistsWS(byte eof[]) throws EOFException;

   public boolean peekStringNotExists(byte eof[]) throws EOFException;

   public boolean peekStringNotExistsWS(byte eof[]) throws EOFException;

   public boolean peekStringExists(ByteRegex eof) throws EOFException;

   public void skipFirst(ByteRegex eof) throws EOFException;

   public boolean peekStringNotExists(ByteRegex eof) throws EOFException;

   public StringBuilder readStringBuilder() throws EOFException;

   public void skipString() throws EOFException;

   public void skipStringBuilder() throws EOFException;

   public String readUntil(ByteRegex regex) throws EOFException;

   public String read(ByteRegex regex) throws EOFException;

   public void skipAfter(ByteRegex regex) throws EOFException;

   public void skipBefore(ByteRegex regex) throws EOFException;

   public ByteRegex.Pos find(ByteRegex regex) throws EOFException;
   
   public String readString0() throws EOFException;

   public void skipString0() throws EOFException;

   public void skipString(byte eof[], byte escape) throws EOFException;

   public void skipString(byte eof[], byte peekend[], byte escape) throws EOFException;

   public void skipString(ByteRegex eof) throws EOFException;

   public void skipStringWS(byte eof[], byte escape) throws EOFException;

   public void skipStringWS(byte eof[], byte peekend[], byte escape) throws EOFException;

   public int[] readIntArray() throws EOFException;

   public void skipIntArray() throws EOFException;

   public int[][] readCIntArray2() throws EOFException;

   public void skipCIntArray2() throws EOFException;

   public int[][] readSquaredIntArray2() throws EOFException;

   public int[][] readIntSparse2() throws EOFException;

   public void skipSquaredIntArray2() throws EOFException;

   public void skipIntSparse2() throws EOFException;

   public int[][][] readCIntArray3() throws EOFException;

   public void skipCIntArray3() throws EOFException;

   public int[][][] readIntSparse3() throws EOFException;

   public void skipIntSparse3() throws EOFException;

   public int[][][] readSquaredIntArray3() throws EOFException;

   public void skipSquaredIntArray3() throws EOFException;

   public long[] readLongArray() throws EOFException;

   public void skipLongArray() throws EOFException;

   public long[][] readCLongArray2() throws EOFException;

   public void skipCLongArray2() throws EOFException;

   public long[][] readLongSparse2() throws EOFException;

   public void skipLongSparse2() throws EOFException;

   public double[] readDoubleArray() throws EOFException;

   public void skipDoubleArray() throws EOFException;

   public double[] readDoubleSparse() throws EOFException;

   public void skipDoubleSparse() throws EOFException;

   public String[] readStringArray() throws EOFException;

   public void skipStringArray() throws EOFException;

   public int[] readCIntArray() throws EOFException;

   public int[] readCIntIncr() throws EOFException;

   public ArrayList<Integer> readCIntArrayList() throws EOFException;

   public ArrayList<Integer> readIntArrayList() throws EOFException;

   public ArrayList<String> readStrArrayList() throws EOFException;

   public void skipCIntArray() throws EOFException;

   public long[] readCLongArray() throws EOFException;

   public void skipCLongArray() throws EOFException;

   public long[] readLongSparse() throws EOFException;

   public void skipLongSparse() throws EOFException;

   public int[] readIntSparse() throws EOFException;

   public void skipIntSparse() throws EOFException;

   public Map<Integer, Long> readSparseLongMap() throws EOFException;

   public Map<Integer, Integer> readSparseIntMap() throws EOFException;

   public Map<String, String> readStringPairMap() throws EOFException;

   public void skipStringPairMap() throws EOFException;

   public void openRead();

   public void closeRead();

   public boolean hasMore();

   public void setDataIn(DataIn in);

   public void readBuffer(DataInput in);
}
