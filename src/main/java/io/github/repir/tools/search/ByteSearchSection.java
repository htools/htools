package io.github.repir.tools.search;

import io.github.repir.tools.io.ByteSearchReader;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.lib.ByteTools;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import java.util.ArrayList;

/**
 * Marks a section in a byte array, which can be used for reading with the
 * ByteSearchReader interface.
 * @author Jeroen Vuurens
 */
public class ByteSearchSection extends ByteSearchPosition implements ByteSearchReader {
   public static Log log = new Log(ByteSearchSection.class);
   public int innerstart; 
   public int innerend;
   public int currentpos = -1;

  public ByteSearchSection(byte haystack[], int start, int innerstart, int innerend, int end ) {
     super( haystack, start, end );
     this.innerstart = innerstart;
     this.innerend = innerend;
     this.currentpos = innerstart;
  }

  public ByteSearchSection(byte haystack[], ByteSearchPosition start, ByteSearchPosition end) {
     super(haystack, start.start, end.end );
     this.innerstart = start.end;
     this.innerend = end.start;
     this.currentpos = start.end;
  }

  public ByteSearchSection(ByteSearchSection s) {
      this(s.haystack, s.start, s.innerstart, s.innerend, s.end);
  }
  
   @Override
   public String readStringUntil(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = needle.findPos(haystack, currentpos, innerend);
      if (pos.found()) {
         String result = ByteTools.toString(haystack, currentpos, pos.start);
         currentpos = pos.end;
         return result;
      }
      currentpos = innerend;
      throw new EOCException("readStringUntil(%s)", needle.toString());
   }

   @Override
   public String findString(ByteSearch needle) throws EOCException {
      //log.info("findString pos %d %d", currentpos, innerend);
      ByteSearchPosition pos = findPos(needle);
      //log.info("findString pos %d %d", pos.start, pos.end);
      if (pos.found()) {
         currentpos = pos.end;
         return ByteTools.toString(haystack, pos.start, pos.end);
      }
      currentpos = innerend;
      throw new EOCException("findString(%s)", needle.toString());
   }

   @Override
   public String findTrimmedString(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = findPos(needle);
      if (pos.found()) {
         currentpos = pos.end;
         return ByteTools.toTrimmedString(haystack, pos.start, pos.end);
      }
      currentpos = innerend;
      throw new EOCException("findTrimmedString(%s)", needle.toString());
   }

   @Override
   public String findFullTrimmedString(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = findPos(needle);
      if (pos.found()) {
         currentpos = pos.end;
         return ByteTools.toFullTrimmedString(haystack, pos.start, pos.end);
      }
      currentpos = innerend;
      throw new EOCException("findFullTrimmedString(%s)", needle.toString());
   }

   @Override
   public String matchString(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = matchPos(needle);
      if (pos.found()) {
         currentpos = pos.end;
         return ByteTools.toString(haystack, pos.start, pos.end);
      }
      currentpos = innerend;
      throw new EOCException("matchString(%s)", needle.toString());
   }

   @Override
   public String matchTrimmedString(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = matchPos(needle);
      if (pos.found()) {
         currentpos = pos.end;
         return ByteTools.toTrimmedString(haystack, pos.start, pos.end);
      }
      currentpos = innerend;
      throw new EOCException("matchTrimmedString(%s)", needle.toString());
   }

   @Override
   public ByteSearchPosition findPos(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = needle.findPos(haystack, currentpos, innerend);
      currentpos = pos.start;
      return pos;
   }

   public int find(ByteSearch needle) throws EOCException {
      return needle.find(haystack, currentpos, innerend);
   }

   public int find(ByteSearch needle, int startpos) throws EOCException {
      return needle.find(haystack, startpos, innerend);
   }

   public int findEnd(ByteSearch needle) throws EOCException {
      return needle.findEnd(haystack, currentpos, innerend);
   }

   public int findEnd(ByteSearch needle, int startpos) throws EOCException {
      return needle.findEnd(haystack, startpos, innerend);
   }

   @Override
   public ByteSearchSection findSection(ByteSection needle) throws EOCException {
      ByteSearchSection pos = needle.findPos(haystack, currentpos, innerend);
      currentpos = pos.end;
      return pos;
   }

   @Override
   public ByteSearchSection findSectionStart(ByteSection needle) throws EOCException {
      ByteSearchSection pos = needle.findPos(haystack, currentpos, innerend);
      currentpos = pos.start;
      return pos;
   }

   public ByteSearchSection findSectionDontMove(ByteSection needle) throws EOCException {
      return needle.findPos(haystack, currentpos, innerend);
   }

   public ArrayList<ByteSearchSection> findAllSectionsDontMove(ByteSection needle) throws EOCException {
      int oldcurrentpos = currentpos;
      ArrayList<ByteSearchSection> sections = new ArrayList<ByteSearchSection>();
      do {
         ByteSearchSection pos = needle.findPos(haystack, currentpos, innerend);
         if (pos.found()) {
            sections.add(pos);
            movePast(pos);
         } else {
            break;
         }
      } while(true);
      currentpos = oldcurrentpos;
      return sections;
   }

   @Override
   public boolean skipStart(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = findPos(needle);
      if (pos.found()) {
         currentpos = pos.start;
         return true;
      }
      currentpos = innerend;
      return false;
   }

   @Override
   public boolean skipEnd(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = findPos(needle);
      if (pos.found()) {
         currentpos = pos.end;
         return true;
      }
      currentpos = innerend;
      return false;
   }

   @Override
   public boolean match(ByteSearch field) throws EOCException {
      return field.match(haystack, currentpos, innerend);
   }

   public boolean contains(ByteSearch field) throws EOCException {
      return field.exists(haystack, currentpos, innerend);
   }

   @Override
   public ByteSearchPosition matchPos(ByteSearch field) throws EOCException {
      return field.matchPos(haystack, currentpos, innerend);
   }
   
   @Override
   public void movePast(ByteSearchPosition section) {
      currentpos = section.end;
   }

   @Override
   public String matchFullTrimmedString(ByteSearch needle) throws EOCException {
      ByteSearchPosition pos = matchPos(needle);
      if (pos.found()) {
         currentpos = pos.end;
         return ByteTools.toFullTrimmedString(haystack, pos.start, pos.end);
      }
      currentpos = innerend;
      throw new EOCException("matchTrimmedString(%s)", needle.toString());
   }
   
   public String reportString() {
      return PrintTools.sprintf("Section( content %s start %d innerstart %d innerend %d end %d endreached %b found %b)", 
              ByteTools.toString(haystack, start, end), start, innerstart, innerend, end, endreached, found());
   }

   @Override
   public String toString() {
      return ByteTools.toString(haystack, innerstart, innerend);
   }

   public String toOuterString() {
      return ByteTools.toString(haystack, start, end);
   }

   public String toTrimmedString() {
      return ByteTools.toTrimmedString(haystack, innerstart, innerend);
   }

   public String toFullTrimmedString() {
      return ByteTools.toFullTrimmedString(haystack, innerstart, innerend);
   }
   
   public byte byteAt(int pos) {
       return haystack[innerstart + pos];
   }
   
   public String subString(int pos) {
       return ByteTools.toString(haystack, innerstart + pos, innerend);
   }
   
   public String subString(int pos, int end) {
       if (end > innerend - innerstart)
           end = innerend - innerstart;
       return ByteTools.toString(haystack, innerstart + pos, innerstart + end);
   }
   
   public ByteSearchSection trim() {
       for (; innerstart < innerend && ByteTools.isWhiteSpaceZero(haystack[innerstart]); innerstart++);
       for (; innerend > innerstart && ByteTools.isWhiteSpaceZero(haystack[innerend-1]); innerend--);
       return this;
   }
}
