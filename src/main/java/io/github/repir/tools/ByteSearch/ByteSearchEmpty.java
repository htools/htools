package io.github.repir.tools.ByteSearch;

import io.github.repir.tools.Lib.Log;

/**
 * Searches for an empty pattern, i.e. always matching the current position length 0
 *
 * @author Jeroen Vuurens
 */
public class ByteSearchEmpty extends ByteSearch {

   public static Log log = new Log(ByteSearchEmpty.class);
   
   protected ByteSearchEmpty() {
   }

   @Override
   public String toString() {
      return "ByteSearchEmpty()";
   }
   
   @Override
   public int find(byte haystack[], int start, int end) {
      return start;
   }

   @Override
   public int findEnd(byte haystack[], int start, int end) {
      return start;
   }

   @Override
   public boolean match(byte[] haystack, int position, int end) {
      return true;
   }

   @Override
   public int matchEnd(byte[] haystack, int position, int end) {
      return position;
   }

   @Override
   public ByteSearchPosition matchPos(byte[] haystack, int position, int end) {
      return new ByteSearchPosition(position, position);
   }

   @Override
   public ByteSearchPosition findPos(byte[] b, int start, int end) {
      return new ByteSearchPosition(start, start);
   }
}
