package io.github.repir.tools.Content;

import java.io.DataInput;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author jbpvuurens
 */
public interface StructureReader extends ByteSearchReader {

   public long getOffset();

   public void setOffset(long l);

   public void setCeiling(long s);

   public long getCeiling();

   public void skip(int bytes) throws EOCException;

   public void setBufferSize(int size);
   
   public void fillBuffer() throws EOCException;
   
   public void reset();

   public int getBufferSize();

   public int readInt() throws EOCException;

   public boolean readBoolean() throws EOCException;

   public int readInt2() throws EOCException;

   public int readInt3() throws EOCException;

   public void skipInt() throws EOCException;

   public long readLong() throws EOCException;

   public void skipLong() throws EOCException;

   public double readDouble() throws EOCException;

   public void skipDouble() throws EOCException;

   public int readByte() throws EOCException;

   public byte[] readBytes(int length) throws EOCException;

   public byte[] readByteBlock() throws EOCException;

   public void skipByte() throws EOCException;

   public void skipByteBlock() throws EOCException;

   public int readCInt() throws EOCException;

   public void skipCInt() throws EOCException;

   public long readCLong() throws EOCException;

   public void skipCLong() throws EOCException;

   public double readCDouble() throws EOCException;

   public void skipCDouble() throws EOCException;

   public String readString() throws EOCException;

   public StringBuilder readStringBuilder() throws EOCException;

   public void skipString() throws EOCException;

   public void skipStringBuilder() throws EOCException;

   public String readString0() throws EOCException;

   public void skipString0() throws EOCException;

   public int[] readIntArray() throws EOCException;

   public void skipIntArray() throws EOCException;

   public int[][] readCIntArray2() throws EOCException;

   public void skipCIntArray2() throws EOCException;

   public int[][] readSquaredIntArray2() throws EOCException;

   public int[][] readIntSparse2() throws EOCException;

   public void skipSquaredIntArray2() throws EOCException;

   public void skipIntSparse2() throws EOCException;

   public int[][][] readCIntArray3() throws EOCException;

   public void skipCIntArray3() throws EOCException;

   public int[][][] readIntSparse3() throws EOCException;

   public void skipIntSparse3() throws EOCException;

   public int[][][] readSquaredIntArray3() throws EOCException;

   public void skipSquaredIntArray3() throws EOCException;

   public long[] readLongArray() throws EOCException;

   public void skipLongArray() throws EOCException;

   public long[][] readCLongArray2() throws EOCException;

   public void skipCLongArray2() throws EOCException;

   public long[][] readLongSparse2() throws EOCException;

   public void skipLongSparse2() throws EOCException;

   public double[] readDoubleArray() throws EOCException;

   public void skipDoubleArray() throws EOCException;

   public double[] readDoubleSparse() throws EOCException;

   public void skipDoubleSparse() throws EOCException;

   public String[] readStringArray() throws EOCException;

   public void skipStringArray() throws EOCException;

   public int[] readCIntArray() throws EOCException;

   public int[] readCIntIncr() throws EOCException;

   public ArrayList<Integer> readCIntArrayList() throws EOCException;

   public ArrayList<Integer> readIntArrayList() throws EOCException;

   public ArrayList<String> readStrArrayList() throws EOCException;

   public void skipCIntArray() throws EOCException;

   public long[] readCLongArray() throws EOCException;

   public void skipCLongArray() throws EOCException;

   public long[] readLongSparse() throws EOCException;

   public void skipLongSparse() throws EOCException;

   public int[] readIntSparse() throws EOCException;

   public void skipIntSparse() throws EOCException;

   public Map<Integer, Long> readSparseLongMap() throws EOCException;

   public Map<Integer, Integer> readSparseIntMap() throws EOCException;

   public Map<String, String> readStringPairMap() throws EOCException;

   public void skipStringPairMap() throws EOCException;

   public void openRead();

   public void closeRead();

   public boolean hasMore();

   public void setDataIn(DataIn in);

   public void readBuffer(DataInput in);
}
