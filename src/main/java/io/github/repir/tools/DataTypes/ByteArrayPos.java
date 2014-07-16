package io.github.repir.tools.DataTypes;
import io.github.repir.tools.Lib.Log;
import java.util.Arrays; 
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 *
 * @author Jeroen Vuurens
 */
public class ByteArrayPos {
  public static Log log = new Log( ByteArrayPos.class ); 
  public byte content[];
  public int start;
  public int length;
  
  public ByteArrayPos(byte content[], int start, int length) {
     this.content = content;
     this.start = start;
     this.length = length;
  }

  @Override
  public String toString() {
     return new String( content, start, length );
  }
  
  public int length() {
     return length;
  }
  
  public byte byteAt(int p) {
     return content[start + p];
  }

  public void append(byte b) {
     content[start + (length++)] = b;
  }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 43 * hash + Arrays.hashCode(this.content);
      return hash;
   }
  
  public boolean equals( Object o ) {
     if (!(o instanceof ByteArrayPos))
        return false;
     ByteArrayPos p = (ByteArrayPos)o;
     if (length != p.length)
        return false;
     for (int i = 0; i < length; i++)
        if (content[start + i] != p.content[p.start + i])
           return false;
     return true;
  }
  
  /**
   * destructive trim
   */
  public void trim() {
     int end = start + length;
     int i = start;
     for (; i < end && content[i] != 0 && content[i] != 32; i++);
     int p = i;
     for (i++; i < end; i++) {
        if (content[i] != 0 && content[i] != 32)
           content[p++] = content[i];
     }
     length = p - start;
  }
  
}
