package io.github.repir.tools.Lib;

import io.github.repir.tools.Lib.Log;

/**
 *
 * @author jbpvuurens
 */
public enum CompressTools {;

   public static Log log = new Log(CompressTools.class);

   public static byte[] int128(int i) {
      int length = 0;
      for (int t = i; (t >> 7) > 0; length++);
      byte b[] = new byte[length + 1];
      for (int t = 0; t <= length; t++) {
         if (t > 0) {
            b[length - t] = (byte) ((i & 0x7F) + 0x80);
         } else {
            b[length - t] = (byte) (i & 0x7F);
         }
      }
      return b;
   }

   public static int int128(byte[] b, int pos) {
      int r = b[pos] & 0x7F;
      for (; (b[pos++] & 0x80) == 1;) {
         r = (r << 7) + b[pos] & 0x7F;
      }
      return r;
   }
}
