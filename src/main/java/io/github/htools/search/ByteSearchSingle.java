package io.github.htools.search;

import io.github.htools.search.ByteSearchPosition;
import io.github.htools.lib.Log;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;

/**
 * Fast string search in Byte Array.
 *
 * @author Jeroen Vuurens
 */
public class ByteSearchSingle extends ByteSearch {

   public static Log log = new Log(ByteSearchSingle.class);
   byte b;

   public ByteSearchSingle(byte b) {
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
      int posend = end;
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
            posend = start + 1;
            break;
         }
      }
       
      if (start >= end) {
         ByteSearchPosition pos = new ByteSearchPosition(haystack, start, -1);
         pos.endreached = true;
         return pos;
      }
      return new ByteSearchPosition(haystack, start, posend);
   }

   @Override
   public ByteSearchPosition findPosDoubleQuoteSafe(byte haystack[], int start, int end) {
      int posend = end;
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
         }
         if (b == haystack[start]) {
            posend = start + 1;
            break;
         }
      }
      if (start < end) {
         return new ByteSearchPosition(haystack, start, posend);
      }
      return new ByteSearchPosition(haystack, start, -1, true);
   }

   @Override
   public ByteSearchPosition findPosNoQuoteSafe(byte haystack[], int start, int end) {
      int posend = end;
      for (; start < end; start++) {
         if (haystack[start] == b) {
            posend = start + 1;
            break;
         }
      }
      if (start < end) {
         return new ByteSearchPosition(haystack, start, posend);
      }
      return new ByteSearchPosition(haystack, start, -1, true);
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
      int posstart = (haystack[position] == b) ? position : Integer.MIN_VALUE;
      int posend = (haystack[position] == b) ? position + 1 : Integer.MIN_VALUE;
      boolean endreached = haystack[position] != b;
      return new ByteSearchPosition(haystack, posstart, posend, endreached);
   }
}
