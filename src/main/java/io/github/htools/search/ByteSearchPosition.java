package io.github.htools.search;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.PrintTools; 
import io.github.htools.lib.StrTools;
import java.util.Comparator;

/**
 * Contains a position found by an implementation of {@link ByteSearch}.
 * @author Jeroen Vuurens
 */
public class ByteSearchPosition implements Comparator<ByteSearchPosition>, Comparable<ByteSearchPosition> {
   public final int start; 
   public final int end;
   public long offset;
   // indicates end of input was reached and possibly reading more input could
   // lead to a different exists, if the caller has more input (e.g reading from
   // a stream) it should provide it and run exists again.
   public boolean endreached;
   public int pattern;
   public byte[] haystack;

   public ByteSearchPosition(byte haystack[], int start, int end) {
      this.haystack = haystack;
      this.start = start;
      this.end = end;
   }

   public ByteSearchPosition(byte haystack[], int start, int end, boolean endreached) {
      this(haystack, start, end);
      this.endreached = endreached;
   }

   public ByteSearchPosition(byte haystack[], int start, int end, boolean endreached, int pattern) {
      this(haystack, start, end, endreached);
      this.pattern = pattern;
   }

   public static ByteSearchPosition notFound() {
      return new ByteSearchPosition(null, 0, Integer.MIN_VALUE);
   }

   public static ByteSearchPosition endReached() {
      return new ByteSearchPosition(null, 0, Integer.MIN_VALUE, true);
   }

   @Override
   public boolean equals(Object pos) {
      ByteSearchPosition p = (ByteSearchPosition) pos;
      if (end == Integer.MIN_VALUE && p.end == Integer.MIN_VALUE) {
         return true;
      }
      if (endreached && p.endreached) {
         return true;
      }
      return start == p.start && end == p.end && endreached == p.endreached;
   }

   public boolean found() {
      return end > -1;
   }

   public boolean notEmpty() {
      return found() && start < end;
   }

   @Override
   public String toString() {
      return ByteTools.toString(haystack, start, end);
   }

   public byte[] toBytes() {
      return ByteTools.toBytes(haystack, start, end);
   }

   public int length() {
       return end - start;
   }
   
   public byte byteAt(int index) {
      int i = start;
      for (; index >= 0; i++) {
          if (haystack[i] != 0)
              index--;
      }
      return haystack[i - 1];
   }

   public String substring(int start) {
      return StrTools.toString(haystack, this.start + start, end);
   }

   public String substring(int start, int end) {
      return StrTools.toString(haystack, this.start + start, end);
   }

   public String reportString() {
      return PrintTools.sprintf("Pos(start %d end %d endreached %b pattern %d found %b)", start, end, endreached, pattern, found());
   }

    @Override
    public int compare(ByteSearchPosition o1, ByteSearchPosition o2) {
        return o1.start - o2.start;
    }

    @Override
    public int compareTo(ByteSearchPosition o) {
        return start - o.start;
    }

}
