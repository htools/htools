package io.github.repir.tools.ByteSearch;

import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;

/**
 * Fast string search in Byte Array.
 *
 * @author Jeroen Vuurens
 */
public class ByteSearchSingle extends ByteSearch {

   public static Log log = new Log(ByteSearchSingle.class);
   byte b;

   protected ByteSearchSingle(byte b) {
      this.b = b;
   }

   public String toString() {
      return PrintTools.sprintf("ByteSearchSingle( %s )", (char)b);
   }
   
   @Override
   public int findQuoteSafe(byte haystack[], int start, int end) {
      LOOP:
      for (; start < end; start++) {
         switch (haystack[start]) {
            case '"':
               for (start++; start < end; start++) {
                  if (haystack[start] == '\\') {
                     start++;
                  } else if (haystack[start] == '"') {
                     continue LOOP;
                  }
               }
               break LOOP;
            case '\'':
               for (start++; start < end; start++) {
                  if (haystack[start] == '\\') {
                     start++;
                  } else if (haystack[start] == '\'') {
                     continue LOOP;
                  }
               }
               break LOOP;
         }
         if (b == haystack[start]) {
            return start;
         }
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public int findNoQuoteSafe(byte haystack[], int start, int end) {
      for (; start < end; start++) {
         if (haystack[start] == b) {
            return start;
         }
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public ByteSearchPosition findPosQuoteSafe(byte haystack[], int start, int end) {
      ByteSearchPosition pos = new ByteSearchPosition();
      LOOP:
      for (; start < end; start++) {
         switch (haystack[start]) {
            case '"':
               for (start++; start < end; start++) {
                  if (haystack[start] == '\\') {
                     start++;
                  } else if (haystack[start] == '"') {
                     continue LOOP;
                  }
               }
               break LOOP;
            case '\'':
               for (start++; start < end; start++) {
                  if (haystack[start] == '\\') {
                     start++;
                  } else if (haystack[start] == '\'') {
                     continue LOOP;
                  }
               }
               break LOOP;
         }
         if (b == haystack[start]) {
            pos.end = start + 1;
            break;
         }
      }
      pos.start = start;
      if (!pos.found())
         pos.endreached = true;
      return pos;
   }

   @Override
   public ByteSearchPosition findPosNoQuoteSafe(byte haystack[], int start, int end) {
      ByteSearchPosition pos = new ByteSearchPosition(start);
      for (; start < end; start++) {
         if (haystack[start] == b) {
            pos.end = start + 1;
            break;
         }
      }
      pos.start = start;
      if (!pos.found())
         pos.endreached = true;
      return pos;
   }

   @Override
   public int findEnd(byte haystack[], int start, int end) {
      int pos = find(haystack, start, end);
      return (pos > -1) ? pos + 1 : Integer.MIN_VALUE;
   }

   @Override
   public boolean match(byte[] haystack, int position, int end) {
      return haystack[position] == b;
   }

   @Override
   public int matchEnd(byte[] haystack, int position, int end) {
      return (haystack[position] == b) ? position + 1 : Integer.MIN_VALUE;
   }

   @Override
   public ByteSearchPosition matchPos(byte[] haystack, int position, int end) {
      ByteSearchPosition pos = new ByteSearchPosition();
      pos.start = (haystack[position] == b) ? position : Integer.MIN_VALUE;
      pos.end = (haystack[position] == b) ? position + 1 : Integer.MIN_VALUE;
      if (!pos.found())
         pos.endreached = true;
      return pos;
   }
}
