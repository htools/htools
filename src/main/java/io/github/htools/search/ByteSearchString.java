package io.github.htools.search;

import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import java.util.ArrayList;

/**
 * Fast string search in Byte Array.
 *
 * @author Jeroen Vuurens
 */
public class ByteSearchString extends ByteSearch {

   public static Log log = new Log(ByteSearchString.class);
   public String originalpattern;
   protected final boolean pattern[][];
   private int laststart;
   private int lastend;

   private ByteSearchString(int length) {
      pattern = new boolean[length][256];
   }

   protected ByteSearchString(String originalpattern, ArrayList<Node> list) {
      this.originalpattern = originalpattern; 
      pattern = new boolean[list.size()][256];
      for (int i = 0; i < pattern.length; i++) {
         pattern[i] = list.get(i).allowed;
      }
   }
   
   public String toString() {
     return PrintTools.sprintf("ByteSearchString( %s )", originalpattern);
   }

   @Override
   public int findQuoteSafe(byte haystack[], int start, int end) {
      int match = 0;
      laststart = start;
      LOOP:
      for (lastend = start; lastend < end; lastend++) {
         if (haystack[lastend] != 0) { // skip \0 bytes
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

            if (pattern[match][haystack[lastend] & 0xFF]) {
               if (++match == pattern.length) {
                  lastend++;
                  return laststart;
               }
            } else {
               match = 0;
               laststart = lastend + 1;
            }
         }
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public int findNoQuoteSafe(byte haystack[], int start, int end) {
      int match = 0;
      laststart = start;
      for (lastend = start; lastend < end; lastend++) {
         if (haystack[lastend] != 0) { // skip \0 bytes
            if (lastend > haystack.length - 1)
                log.info("%d %d %s %d %d", start, end, new String(haystack), match, pattern.length);
            if (pattern[match][haystack[lastend] & 0xFF]) {
               if (++match == pattern.length) {
                  lastend++;
                  return laststart;
               }
            } else {
               match = 0;
               laststart = lastend + 1;
            }
         }
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public int findEnd(byte haystack[], int start, int end) {
      if (find(haystack, start, end) > -1) {
         return lastend;
      } else {
         return Integer.MIN_VALUE;
      }
   }

   @Override
   public boolean match(byte[] haystack, int position, int end) {
      lastend = position;
      for (int match = 0; lastend < end; lastend++) {
         if (haystack[lastend] != 0) { // skip \0 bytes
            if (pattern[match][haystack[lastend] & 0xFF]) {
               if (++match == pattern.length) {
                  lastend++;
                  return true;
               }
            } else {
               return false;
            }
         }
      }
      return false;
   }

   /**
    * Is NOT Thread safe
    */
   @Override
   public int matchEnd(byte[] haystack, int position, int end) {
      return match(haystack, position, end) ? lastend : Integer.MIN_VALUE;
   }

   /**
    * Is NOT Thread safe
    */
   @Override
   public ByteSearchPosition findPos(byte[] haystack, int start, int end) {
      ByteSearchPosition p = new ByteSearchPosition(haystack);
      int pos = find(haystack, start, end);
      if (pos < 0) {
         p.start = laststart;
         p.end = Integer.MIN_VALUE;
         p.endreached = true;
      } else {
         p.start = pos;
         p.end = lastend;
      }
      return p;
   }

   /**
    * NOT Thread safe
    */
   @Override
   public ByteSearchPosition matchPos(byte[] haystack, int position, int end) {
      ByteSearchPosition p = new ByteSearchPosition(haystack);
      if (match(haystack, position, end)) {
         p.start = laststart;
         p.end = Integer.MIN_VALUE;
         p.endreached = true;
      } else {
         p.start = laststart;
         p.end = lastend;
      }
      return p;
   }
   
    public static void main(String[] args) {
        ByteSearchString a = (ByteSearchString)ByteSearch.create("Eintein");
        log.info("%b", a.exists("this Albert einstein and this too"));
    }
}
