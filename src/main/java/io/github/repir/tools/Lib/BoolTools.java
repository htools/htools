package io.github.repir.tools.Lib;

public class BoolTools {

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

   public static boolean[] createASCIIAccept(char ... c) {
      boolean a[] = new boolean[128];
      for (int i : c) {
         a[i] = true;
      }
      return a;
   }

   public static boolean[] createASCIIAcceptRange(char c, char d) {
      boolean a[] = new boolean[128];
      for (int i = 0; i < 128; i++) {
         a[i] = (i >= c && i <= d);
      }
      return a;
   }

   public static boolean[] createASCIIRejectRange(char c, char d) {
      boolean a[] = new boolean[128];
      for (int i = 0; i < 128; i++) {
         a[i] = !(i >= c && i <= d);
      }
      return a;
   }
}
