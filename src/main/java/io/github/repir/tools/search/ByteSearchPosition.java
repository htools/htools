package io.github.repir.tools.search;
import io.github.repir.tools.lib.PrintTools; 

/**
 * Contains a position found by an implementation of {@link ByteSearch}.
 * @author Jeroen Vuurens
 */
public class ByteSearchPosition {
   public int start = -1; 
   public int end = Integer.MIN_VALUE;
   public long offset;
   // indicates end of input was reached and possibly reading more input could
   // lead to a different exists, if the caller has more input (e.g reading from
   // a stream) it should provide it and run exists again.
   public boolean endreached;
   public int pattern;
   public byte[] haystack;

  public ByteSearchPosition(byte haystack[]) {
     this.haystack = haystack;
  }

   public ByteSearchPosition(byte haystack[], int start) {
       this.haystack = haystack;
      this.start = start;
   }

   public ByteSearchPosition(byte haystack[], int start, int end) {
       this.haystack = haystack;
      this.start = start;
      this.end = end;
   }

   public ByteSearchPosition(byte haystack[], int start, int end, boolean endreached) {
      this(haystack, start, end);
      this.endreached = endreached;
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

   public String toString() {
      return new String(haystack, start, end - start);
   }

   public String reportString() {
      return PrintTools.sprintf("Pos(start %d end %d endreached %b pattern %d found %b)", start, end, endreached, pattern, found());
   }

}
