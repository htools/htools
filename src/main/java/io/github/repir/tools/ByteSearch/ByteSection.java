package io.github.repir.tools.ByteSearch;

import io.github.repir.tools.Lib.ByteTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.util.ArrayList;

/**
 * Fast string search in Byte Array.
 *
 * @author Jeroen Vuurens
 */
public class ByteSection extends ByteSearch {

   public static Log log = new Log(ByteSection.class);
   protected final ByteSearch first;
   protected final ByteSearch second;
   protected static final boolean trimspace[] = getTrimSpace();

   public ByteSection(ByteSearch start, ByteSearch end) {
      this.first = start;
      this.second = end;
   }

   public ByteSection(String start, String end) {
      this.first = ByteSearch.create(start);
      this.second = ByteSearch.create(end);
   }

   public String toString() {
      return PrintTools.sprintf("ByteSection(%s, %s)", first, second);
   }
   
   @Override
   public ByteSection QuoteSafe() {
      quotesafe = true;
      return this;
   }
   
   public ByteSection innerQuoteSafe() {
      second.quotesafe = true;
      return this;
   }
   
   public static boolean[] getTrimSpace() {
      boolean trimspace[] = new boolean[256];
      trimspace[ 0] = true;
      trimspace[ 32] = true;
      trimspace[ '\n'] = true;
      trimspace[ '\r'] = true;
      trimspace[ '\t'] = true;
      return trimspace;
   }

   public String getFirstString(byte[] haystack, int startpos, int endpos) {
      startpos = first.findEnd(haystack, startpos, endpos);
      if (startpos > -1) {
         endpos = second.find(haystack, startpos, endpos);
         if (endpos > -1) {
            return ByteTools.toTrimmedString(haystack, startpos, endpos);
         }
      }
      return "";
   }

   public String getFirstString(String text) {
     byte bytes[] = text.getBytes();
     return getFirstString(bytes, 0, bytes.length);
   }

   /**
    * Does not use quote safe before matching the first part, but between the first
    * part and the second part.
    */
   @Override
   public int find(byte[] haystack, int start, int end) {
      ByteSearchPosition pos = first.findPos(haystack, start, end);
      if (pos.found()) {
         if (second.findQuoteSafe(haystack, pos.end, end) > -1)
            return pos.start;
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public int findEnd(byte[] haystack, int start, int end) {
      int pos = first.findEnd(haystack, start, end);
      if (pos > -1) {
         return second.find(haystack, pos, end);
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public ByteSearchSection findPos(byte[] haystack, int start, int end) {
      ByteSearchPosition pos = first.findPos(haystack, start, end);
      if (pos.found()) {
         ByteSearchPosition pos2 = second.findPos(haystack, pos.end, end);
         ByteSearchSection section = new ByteSearchSection(haystack, pos.start, pos.end, pos2.start, pos2.end);
         section.endreached = pos.endreached | pos2.endreached;
         return section;
      } else {
         ByteSearchSection section = new ByteSearchSection(haystack, pos.start, pos.end, pos.end, pos.end);
         section.endreached = true;
         return section;
      }
   }
   
   @Override
   public ByteSearchSection findPos(String text) {
      byte bytes[] = text.getBytes();
      return findPos(bytes, 0, bytes.length);
   }
   
   @Override
   public boolean match(byte[] haystack, int position, int end) {
      int pos = first.matchEnd(haystack, position, end);
      if (pos > -1) {
         return second.exists(haystack, pos, end);
      }
      return false;
   }

   @Override
   public int matchEnd(byte[] haystack, int position, int end) {
      int pos = first.matchEnd(haystack, position, end);
      if (pos > -1) {
         return second.findEnd(haystack, pos, end);
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public ByteSearchSection matchPos(byte[] haystack, int start, int end) {
      ByteSearchPosition pos = first.matchPos(haystack, start, end);
      if (pos.found()) {
         ByteSearchPosition pos2 = second.findPos(haystack, pos.end, end);
         ByteSearchSection section = new ByteSearchSection(haystack, pos.start, pos.end, pos2.start, pos2.end);
         section.endreached = pos.endreached | pos2.endreached;
         return section;
      } else {
         ByteSearchSection section = new ByteSearchSection(haystack, pos.start, pos.end, pos.end, pos.end);
         section.endreached = true;
         return section;
      }
   }
   
   public ArrayList<ByteSearchSection> findAllSections(byte b[], int start, int end) {
      ArrayList<ByteSearchSection> list = new ArrayList<ByteSearchSection>();
      while (start <= end) {
         ByteSearchSection p = findPos(b, start, end);
         if (p.found()) {
            list.add(p);
            if (start == p.end)
               start++;
            else
               start = p.end;
         } else {
            break;
         }
      }
      return list;
   }
}
