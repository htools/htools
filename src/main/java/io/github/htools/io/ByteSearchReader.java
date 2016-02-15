package io.github.htools.io;

import io.github.htools.lib.ByteTools;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;

/**
 *
 * @author jer
 */
public interface ByteSearchReader {
   /**
    * Finds the first occurrence of needle starting from the current position. 
    * The buffer position is moved to the end of the needle, or to the end of the 
    * buffer if no needle is found.. 
    * @param needle
    * @return the content from the current position to the start of the needle
    */
   public String readStringUntil(ByteSearch needle) throws EOCException;
   
   /**
    * The buffer position is moved to the end of the needle, or to the end of the 
    * buffer if no needle is found.
    * @param needle A ByteSearch pattern that is used to find the next occurrence
    * @return The String of the matched needle using {@link ByteTools#toString(byte[], int, int)},
    * or null if nor found.
    */
   public String readString(ByteSearch needle) throws EOCException;
   
   /**
    * The buffer position is moved to the end of the needle, or to the end of the 
    * buffer if no needle is found.
    * @param needle A ByteSearch pattern that is used to find the next occurrence
    * @return The String of the matched needle using {@link ByteTools#toTrimmedString(byte[], int, int)},
    * or null if nor found.
    */
   public String readTrimmedString(ByteSearch needle) throws EOCException;

   /** 
    * The buffer position is moved to the end of the needle, or to the end of the 
    * buffer if no needle is found.
    * @param needle A ByteSearch pattern that is used to find the next occurrence
    * @return The String of the matched needle using {@link ByteTools#toFullTrimmedString(byte[], int, int)},
    * or null if nor found.
    */
   public String readFullTrimmedString(ByteSearch needle) throws EOCException;

   /**
    * The buffer position is moved to the end of the needle, or not moved
    * when not found.
    * @param needle A ByteSearch pattern matched at the current position
    * @return The String of the matched needle using {@link ByteTools#toString(byte[], int, int)},
    * or null if nor found.
    */
   public String readMatchingString(ByteSearch needle) throws EOCException;
   
   /**
    * The buffer position is moved to the end of the needle, or not moved
    * when not found.
    * @param needle A ByteSearch pattern matched at the current position
    * @return The String of the matched needle using {@link ByteTools#toTrimmedString(byte[], int, int)},
    * or null if nor found.
    */
   public String readMatchingTrimmedString(ByteSearch needle) throws EOCException;

   /**
    * The buffer position is moved to the end of the needle, or not moved
    * when not found.
    * @param needle A ByteSearch pattern matched at the current position
    * @return The String of the matched needle using {@link ByteTools#toFullTrimmedString(byte[], int, int)},
    * or null if nor found.
    */
   public String readMatchingFullTrimmedString(ByteSearch needle) throws EOCException;

   /**
    * Moves the buffer position to the start of the next needle. If the current
    * position matches the needle, the position is unchanged. If no needle is found
    * this moves the position to the end of the buffer. 
    * @param needle 
    * @return true if found
    */
   public boolean skipUntil(ByteSearch needle) throws EOCException;
   
   /**
    * Moves the buffer position past the end of the next needle. If the current
    * position matches the needle, this is used. If no needle is found
    * this moves the position to the end of the buffer. 
    * @param needle 
    * @return true if found
    */
   public boolean skipPast(ByteSearch needle) throws EOCException;
   
   /**
    * Does not move the current position
    * @param needle 
    * @return true if the current position matches the needle
    */
   public boolean match(ByteSearch needle) throws EOCException;
   
   /**
    * @param needle 
    * @return ByteSearchPosition of the current position matched against the needle
    */
   public ByteSearchPosition matchPos(ByteSearch needle) throws EOCException;
   
   /**
    * @param needle 
    * @return ByteSearchPosition of the first matched needle from the current
    * position. The current position is moved to the needle start.
    */
   public ByteSearchPosition readPos(ByteSearch needle) throws EOCException;

   /**
    * @param needle 
    * @return ByteSearchPosition of the first matched needle from the current
    * position. The current position is moved to the needle start.
    */
   public ByteSearchSection readSection(ByteSection needle) throws EOCException;

   /**
    * @param needle 
    * @return ByteSearchPosition of the first matched needle from the current
    * position. The current position is moved to the needle start.
    */
   public ByteSearchSection readSectionStart(ByteSection needle) throws EOCException;

   /**
    * Moves the current position to the end of the ByteSearchPosition.
    * @param position 
    */
   public void movePast(ByteSearchPosition position);
}
