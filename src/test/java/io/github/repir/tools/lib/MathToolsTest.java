/*
 */
package io.github.repir.tools.lib;

import io.github.repir.tools.lib.ArrayTools;
import io.github.repir.tools.lib.DoubleTools;
import io.github.repir.tools.lib.MathTools;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jeroen
 */
public class MathToolsTest {
   
   public MathToolsTest() {
   }

   @Test
   public void testStdNormalP() {
   }

   @Test
   public void testStdNormalZ() {
   }

   @Test
   public void testRoundPrecisionStr() {
   }

   @Test
   public void testRoundPrecision() {
   }

   @Test
   public void testMin_doubleArr() {
   }

   @Test
   public void testMin3() {
   }

   @Test
   public void testMin_intArr() {
   }

   @Test
   public void testMin_Set() {
   }

   @Test
   public void testMin_doubleArrArr() {
   }

   @Test
   public void testMax_doubleArrArr() {
   }

   @Test
   public void testMax_doubleArr() {
   }

   @Test
   public void testSum() {
   }

   @Test
   public void testNormalize() {
   }

   @Test
   public void testAvg() {
   }

   @Test
   public void testTodouble() {
      Double a[] = {1.0, 2.0};
      double b[] = DoubleTools.toPrimitive(a);
      for (int i = 0; i < a.length; i++)
         assertEquals("", a[i], b[i], 0);
   }

   @Test
   public void testStandardDeviation() {
   }

   @Test
   public void testSDdistance() {
   }

   @Test
   public void testSDdistance2() {
   }

   @Test
   public void testCramersV() {
   }

   @Test
   public void testMod() {
   }

   @Test
   public void testLog2() {
   }

   @Test
   public void testFac() {
   }

   @Test
   public void testBinomialCoefficient() {
   }

   @Test
   public void testMain() {
   }

   @Test
   public void testNumberOfSetBits() {
      for (int i = 0; i < 32; i++)
         assertEquals("single bit failed " + i, MathTools.numberOfSetBits(1 << i), 1);
      for (int i = 0; i < 31; i++)
         assertEquals("double bit failed " + i, MathTools.numberOfSetBits(3 << i), 2);
      assertEquals("all bits failed", MathTools.numberOfSetBits( 0xFFFFFFFF ), 32);
      assertEquals("no bits failed", MathTools.numberOfSetBits(0), 0);
      for (int i = 0; i < 64; i++)
         assertEquals("single bit failed " + i, MathTools.numberOfSetBits(1l << i), 1);
      for (int i = 0; i < 63; i++)
         assertEquals("double bit failed " + i, MathTools.numberOfSetBits(3l << i), 2);
      assertEquals("all bits failed", MathTools.numberOfSetBits( 0xFFFFFFFFl ), 32);
      assertEquals("all bits failed", MathTools.numberOfSetBits( 0xFFFFFFFFFFFFFFFFl ), 64);
      assertEquals("no bits failed", MathTools.numberOfSetBits(0l), 0);
   }

   @Test
   public void numberCoverBits() {
      for (int p = 0; p < 20; p++) {
         assertEquals("numberCoverBit failed " + (1<<p), MathTools.numberCoverBits(1 << p), 1);
         assertEquals("numberCoverBit failed " + (3<<p), MathTools.numberCoverBits(3 << p), 2);
         assertEquals("numberCoverBit failed " + (13<<p), MathTools.numberCoverBits(13 << p), 4);
         assertEquals("numberCoverBit failed " + (129<<p), MathTools.numberCoverBits(129 << p), 8);
      }
   }
   
   @Test
   public void testNumberLeadingZeroBits() {
   }

   @Test
   public void testNextHighestPower2() {
   }

   @Test
   public void testGetWeibull() {
   }
   
   @Test
   public void testWelchDegreesOfFreedom() {
       double welchDegreesOfFreedom = MathTools.welchDegreesOfFreedom(100, 100, 11, 11);
       assertEquals(welchDegreesOfFreedom, 22.0, 0.01);
   }
   @Test
   public void testWelch() {
       double a[] = {175, 168, 168, 190, 156, 181, 182, 175, 174, 179};
       double b[] = {120, 180, 125, 188, 130, 190, 110, 185, 112, 188};
       double p = DoubleTools.welchTTestOneSided(ArrayTools.toList(a), ArrayTools.toList(b));
       assertEquals(p, 0.04424, 0.01);
   }
}
