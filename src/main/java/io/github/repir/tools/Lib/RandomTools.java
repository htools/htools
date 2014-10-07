package io.github.repir.tools.Lib;

import static io.github.repir.tools.Lib.MathTools.*;
import static io.github.repir.tools.Lib.PrintTools.*;
import java.util.UUID;

/**
 *
 * @author jeroen
 */
public enum RandomTools {;

   private static java.util.Random rand = new java.util.Random(1);
   private static MT mt = new MT();
   private static boolean haveNextNextGaussian = false;
   private static double nextNextGaussian;

   /**
    *
    * @return
    */
   public static double getStdNormal() {
      if (haveNextNextGaussian) {
         haveNextNextGaussian = false;
         return nextNextGaussian;
      } else {
         double v1, v2, s;
         do {
            v1 = 2 * getDouble() - 1; // between -1 and 1
            v2 = 2 * getDouble() - 1; // between -1 and 1
            s = v1 * v1 + v2 * v2;
         } while (s >= 1 || s == 0);
         double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
         nextNextGaussian = v2 * multiplier;
         haveNextNextGaussian = true;
         return v1 * multiplier;
      }
   }

   /**
    *
    * @return
    */
   public static int getSign() {
      return (getBoolean() ? 1 : -1);
   }

   /**
    *
    * @return
    */
   public static boolean getBoolean() {
      return getInt() > 0;
   }

   /**
    *
    * @param bias
    * @return
    */
   public static int getBiasRandomSign(double bias) {
      int sign = (int) java.lang.Math.signum(bias);
      bias *= sign;
      if (getDouble() <= bias) {
         return sign;
      } else {
         return getSign();
      }
   }

   /**
    *
    * @return
    */
   public static double getDouble() { // double between 0.0 and 1.0
      return (((long) (getBits(26)) << 27) + getBits(27))
              / (double) (1L << 53);
   }

   /**
    *
    * @param UpperLimit
    * @return
    */
   public static int getInt(int UpperLimit) {
      return java.lang.Math.abs(mt.random() % (UpperLimit));
   }

   /**
    *
    * @return
    */
   public static int getInt() {
      return mt.random();
   }

   public static int[] getRandomList( int upperlimit ) {
      int result[] = new int[ upperlimit ];
      for (int i = 0; i < upperlimit; i++)
         result[i] = i;
      for (int i = 0; i < upperlimit; i++) {
         int pos = getInt(upperlimit);
         int a = result[i];
         result[i] = result[pos];
         result[pos] = a;
      }
      return result;
   }
   
   /**
    *
    * @param bits
    * @return
    */
   public static int getBits(int bits) {
      return (int) (getInt() >>> (32 - bits));
   }

   /**
    *
    * @param classes
    * @return
    */
   public static int chooseClass(double[] classes) {
      double[] norm = normalizeP(classes);
      double random = getDouble();
      int c;
      for (c = 0; c < norm.length - 1 && random >= norm[c]; c++) {
         random -= norm[c];
      }
      return c;
   }

   /**
    *
    * @param p
    * @return
    */
   public static int StdNormalOneTailed(double p) {
      double z = MathTools.stdNormalZ(0.5 + p / 2);
      double r = getStdNormal();
      r = (r < 0) ? -r : r;
      int dev = (int) java.lang.Math.floor(r / z);
      return dev;
   }
   static double StdNormalOneTailedCompCorrection = 0.79 / (0.282 / (0.282 + 0.115));

   /**
    *
    * @param p
    * @return
    */
   public static int StdNormalOneTailedComp(double p) {
      double z = MathTools.stdNormalZ(0.5 + p / 2);
      double r = getStdNormal();
      r = (r < 0) ? -r : r;
      double v = r / z;
      int dev = (int) java.lang.Math.floor(v);
      if (dev > 0) {
         dev = (int) java.lang.Math.floor(v * StdNormalOneTailedCompCorrection);
      }
      return dev;
   }

   public static String uuid() {
      return UUID.randomUUID().toString();
   }
}

class MT {

   private int mt_index;
   private int[] mt_buffer = new int[624];

   public MT() {
      java.util.Random r = new java.util.Random();
      for (int i = 0; i < 624; i++) {
         mt_buffer[i] = r.nextInt();
      }
      mt_index = 0;
   }

   public int random() {
      if (mt_index == 624) {
         mt_index = 0;
         int i = 0;
         int s;
         for (; i < 624 - 397; i++) {
            s = (mt_buffer[i] & 0x80000000) | (mt_buffer[i + 1] & 0x7FFFFFFF);
            mt_buffer[i] = mt_buffer[i + 397] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
         }
         for (; i < 623; i++) {
            s = (mt_buffer[i] & 0x80000000) | (mt_buffer[i + 1] & 0x7FFFFFFF);
            mt_buffer[i] = mt_buffer[i - (624 - 397)] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
         }

         s = (mt_buffer[623] & 0x80000000) | (mt_buffer[0] & 0x7FFFFFFF);
         mt_buffer[623] = mt_buffer[396] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
      }
      return mt_buffer[mt_index++];
   }
}
