package io.github.htools.search;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import java.util.ArrayList;

/**
 * Finds sections marked by a start and end {@link ByteSearch}, that do not 
 * contain nonexists.
 *
 * @author Jeroen Vuurens
 */
public class ByteSectionScanned extends ByteSection {

   public static Log log = new Log(ByteSectionScanned.class);
   final ByteSearch nonexists;

   public ByteSectionScanned(ByteSearch start, ByteSearch nonexists, ByteSearch end) {
      super(start, end);
      this.nonexists = nonexists;
   }

   public ByteSectionScanned(String start, String nonexists, String end) {
      super(start, end);
      this.nonexists = ByteSearch.create(nonexists);
   }

   public String toString() {
      return PrintTools.sprintf("ByteSectionScanned(%s, %s, %s)", first, nonexists, second);
   }

   public String getFirstString(byte[] haystack, int startpos, int endpos) {
      while (startpos < endpos) {
         ByteSearchPosition pos = first.findPos(haystack, startpos, endpos);
         if (!pos.found()) {
            break;
         }
         endpos = second.find(haystack, pos.end, endpos);
         if (endpos < 0) {
            break;
         }
         ByteSearchPosition posn = nonexists.findPos(haystack, pos.end, endpos);
         if (!posn.found()) {
            return ByteTools.toTrimmedString(haystack, pos.start, endpos);
         }
         startpos = posn.end;
      }
      return "";
   }

   /**
    * Does not use quote safe before matching the first part, but between the
    * first part and the second part.
    */
   @Override
   public int find(byte[] haystack, int start, int end) {
      while (start < end) {
         ByteSearchPosition pos = first.findPos(haystack, start, end);
         if (!pos.found()) {
            break;
         }
         int endfound = second.findEnd(haystack, pos.end, end);
         if (endfound < 0) {
            break;
         }
         ByteSearchPosition posn = nonexists.findPos(haystack, pos.end, end);
         if (!posn.found()) {
            return pos.start;
         }
         start = posn.end;
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public int findEnd(byte[] haystack, int start, int end) {
      while (start < end) {
         ByteSearchPosition pos = first.findPos(haystack, start, end);
         if (!pos.found()) {
            break;
         }
         int endfound = second.findEnd(haystack, pos.end, end);
         if (endfound < 0) {
            break;
         }
         ByteSearchPosition posn = nonexists.findPos(haystack, pos.end, end);
         if (!posn.found()) {
            return endfound;
         }
         start = posn.end;
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public ByteSearchSection findPos(byte[] haystack, int start, int end) {
      ByteSearchPosition pos = null;
      ByteSearchPosition pos2 = null;
      while (start < end) {
         pos = first.findPos(haystack, start, end);
         if (!pos.found()) {
            break;
         }
         if (pos2 == null || pos2.start < pos.end) {
            pos2 = second.findPos(haystack, pos.end, end);
            if (!pos2.found()) {
               break;
            }
         }
         ByteSearchPosition posn = nonexists.findPos(haystack, pos.end, pos2.start);
         if (!posn.found()) {
            ByteSearchSection section = new ByteSearchSection(haystack, pos.start, pos.end, pos2.start, pos2.end);
            section.endreached = pos.endreached | pos2.endreached;
            return section;
         } else {
            start = posn.start;
         }
      }
      ByteSearchSection section = new ByteSearchSection(haystack, (pos != null) ? pos.start : end, (pos != null)?pos.end : end, Integer.MIN_VALUE, Integer.MIN_VALUE);
      section.endreached = true;
      return section;
   }

   @Override
   public boolean match(byte[] haystack, int position, int end) {
      int endfirst = first.matchEnd(haystack, position, end);
      if (endfirst > -1) {
         int startsecond = second.find(haystack, endfirst, end);
         if (startsecond > -1) {
            return !nonexists.exists(haystack, endfirst, startsecond);
         }
      }
      return false;
   }

   @Override
   public int matchEnd(byte[] haystack, int position, int end) {
      int endfirst = first.matchEnd(haystack, position, end);
      if (endfirst > -1) {
         ByteSearchPosition pos = second.findPos(haystack, endfirst, end);
         if (pos.found() && !nonexists.exists(haystack, endfirst, pos.start)) {
            return pos.end;
         }
      }
      return Integer.MIN_VALUE;
   }

   @Override
   public ByteSearchSection matchPos(byte[] haystack, int start, int end) {
      ByteSearchPosition pos = first.matchPos(haystack, start, end);
      if (pos.found()) {
         ByteSearchPosition pos2 = second.findPos(haystack, pos.end, end);
         if (pos2.found() && !nonexists.exists(haystack, pos.end, pos2.start)) {
            ByteSearchSection section = new ByteSearchSection(haystack, pos.start, pos.end, pos2.start, pos2.end);
            section.endreached = pos.endreached | pos2.endreached;
            return section;
         }
      }
      ByteSearchSection section = new ByteSearchSection(haystack, pos.start, pos.end, pos.end, pos.end);
      section.endreached = true;
      return section;
   }
}
