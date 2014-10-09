package io.github.repir.tools.Lib;

import io.github.repir.tools.ByteSearch.ByteSearch;
import java.util.Arrays;

/**
 * Family of operations on 256 byte boolean decision arrays, used for fast processing
 * on byte arrays.
 * @author jeroen
 */
public enum BoolTools {;

   public static Log log = new Log(BoolTools.class);

   public static void setBooleanArray(boolean[] a, char from, char to, boolean s) {
      for (; from <= to; from++) {
         a[from] = s;
      }
   }

   public static void setBooleanArray(boolean a[], boolean s, char... c) {
      for (char b : c) {
         a[b] = s;
      }
   }

   public static boolean[] createASCIIAccept(char... c) {
      boolean a[] = new boolean[256];
      for (int i : c) {
         a[i] = true;
      }
      return a;
   }

   public static boolean[] createASCIIAcceptRange(char c, char d) {
      boolean a[] = new boolean[256];
      for (int i = 0; i < 256; i++) {
         a[i] = (i >= c && i <= d);
      }
      return a;
   }

   public static boolean[] combineRanges(boolean[] ... range) {
      boolean a[] = new boolean[256];
      for (boolean r[] : range) {
         for (int i = 0; i < 256; i++) {
            a[i] |= r[i];
         }
      }
      return a;
   }

   public static boolean[] createASCIIRejectRange(char c, char d) {
      boolean a[] = new boolean[256];
      for (int i = 0; i < 256; i++) {
         a[i] = !(i >= c && i <= d);
      }
      return a;
   }
   
   public static boolean[] allTrue() {
      boolean a[] = new boolean[256];
      Arrays.fill(a, true);
      return a;
   }
   
   public static boolean[] whitespace() {
      return createASCIIAccept( '\n' , ' ', '\t' , '\r' );
   }

   public static boolean[] invert(boolean array[]) {
       boolean result[] = new boolean[256];
       for (int i = 0; i < result.length; i++)
           result[i] = !array[i];
       return result;
   }
   
   public static boolean[] firstAcceptedCharFromRegex(String regex) {
       ByteSearch s = ByteSearch.create(regex);
       return s.firstAcceptedChar();
   }
   
   public static boolean[] word() {
      return combineRanges(
              createASCIIAcceptRange('A', 'Z'), 
              createASCIIAcceptRange('a', 'z'), 
              createASCIIAcceptRange('0', '9'));
   }
   
   public static boolean[] label() {
      return combineRanges(
              createASCIIAcceptRange('A', 'Z'), 
              createASCIIAcceptRange('a', 'z'), 
              createASCIIAcceptRange('0', '9'), 
              createASCIIAccept('_'));
   }
   
   public static boolean[] word0() {
      return combineRanges(word(),
              createASCIIAccept((char)0));
   }
   
   public static boolean[] letter() {
      return combineRanges(
              createASCIIAcceptRange('A', 'Z'), 
              createASCIIAcceptRange('a', 'z'));
   }
   
   public static boolean[] digit() {
      return createASCIIAcceptRange('0', '9');
   }
   
   public static boolean[] namedot() {
      return combineRanges(word(), 
              createASCIIAccept('.'));
   }
}
