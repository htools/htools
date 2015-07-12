package io.github.repir.tools.io.struct;

import com.google.gson.JsonObject;
import io.github.repir.tools.io.ByteSearchReader;
import io.github.repir.tools.io.DataIn;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.type.Long128;
import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Type;
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
   
   /**
    * requests the buffer to be replenished by reading more data from file. This
    * is typically done when the end of the buffer is encountered while reading.
    * When using the read methods in StructureReader, this is triggered automatically.
    * Throws an EOCException when the StructureReader is not connected to an input
    * source, or when an attempt is made to fillBuffer when hasMore() already 
    * returned false.
    * @throws EOCException 
    */
   public void fillBuffer() throws EOCException;
   
   /**
    * reuses the buffered part, by setting back the read pointer top the offset
    * of the buffer, and resets hasMore() to true. This is useful when (part of) a file is being
    * read several times, and typically needs the buffer size to be set to
    * at least the amount of data being reread.
    */
   public void reuseBuffer();

   /**
    * @return The allocated number of bytes for the buffer (can be greater than
    * the number of bytes read or the file size).
    */
   public int getBufferSize();

   public int readInt() throws EOCException;

   public boolean readBoolean() throws EOCException;

   public int readInt2() throws EOCException;

   public int readInt3() throws EOCException;

   public void skipInt() throws EOCException;

   public long readLong() throws EOCException;

   public Long128 readLong128() throws EOCException;

   public void skipLong() throws EOCException;

   public double readDouble() throws EOCException;

   public void skipDouble() throws EOCException;

   public int readByte() throws EOCException;

   public byte[] readBytes(int length) throws EOCException;

   public byte[] readByteArray() throws EOCException;

   public boolean[] readBoolArray() throws EOCException;

   public <T> T read(Type t)  throws EOCException;
        
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

   public JsonObject readJson0() throws EOCException;

   public JsonObject readJson() throws EOCException;

   public void skipString0() throws EOCException;

   public void skipJson0() throws EOCException;

   public void skipJson() throws EOCException;

   public int[] readIntArray() throws EOCException;

   public void skipIntArray() throws EOCException;

   public void skipBoolArray() throws EOCException;

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

   public ArrayList<Integer> readIntList() throws EOCException;

   public ArrayList<Long> readLongList() throws EOCException;

   public ArrayList<String> readStringList() throws EOCException;

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

   public void openRead() throws IOException;

   public void closeRead();

   public boolean hasMore();

   public void setDataIn(DataIn in);

   public void readBuffer(DataInput in);
}
