/*
 * Copyright 2013 jeroen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.repir.tools.Lib;

import io.github.repir.tools.Type.Matrix;
import io.github.repir.tools.Type.SparseDoubleArray;
import io.github.repir.tools.Type.SparseDoubleMatrix;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jeroen
 */
public class MatrixTest {
   
   @Test
   public void testConstruct() {
      Matrix b = new Matrix( new double[][]{{ 1,2,3 }, {0,1,2}, {1, 2, 0}, {10, 0, 1}});
      Matrix a = new SparseDoubleMatrix(new SparseDoubleArray(new double[]{ 0, 0, 1, 2, 3 }),
                                        new SparseDoubleArray(new double[]{ 0, 0, 0, 1, 2 }),
                                        new SparseDoubleArray(new double[]{ 0, 0, 1, 2 }),
                                        new SparseDoubleArray(new double[]{ 0, 0, 0, 0, 1 })).toMatrix();
      Matrix c = a.transpose();
      c.set(0, 3, 10);
      Matrix d = c.transpose();
      Assert.assertEquals("", 4, a.getRows());
      Assert.assertEquals("", 3, a.getColumns());
      Assert.assertEquals("", 3, a.transpose().getRows());
      Assert.assertEquals("", 4, a.transpose().getColumns());
      Assert.assertTrue("Matrices are not the same", d.equals(b));
   }

   @Test
   public void testProd() {
      Matrix a = new Matrix( new double[][]{{ 1,2,3 }, {4,5,6}});
      Matrix b = new SparseDoubleMatrix(new SparseDoubleArray(new double[]{ 0, 0, 7, 9, 11}),
                                        new SparseDoubleArray(new double[]{ 0, 0, 8, 10, 12 })).toMatrix();
      Matrix c = b.transpose();
      Matrix d = a.dot(c);
      Matrix e = new Matrix( new double[][]{{ 58, 64}, {139, 154}});
      Assert.assertTrue("Matrices " + d + " and " + e + " are not the same", d.equals(e));
   }

   @Test
   public void testMult() {
      Matrix a = new Matrix( new double[][]{{ 1,2,3 }, {4,5,6}});
      Matrix b = a.transpose().mult(2).transpose();
      Matrix c = new Matrix( new double[][]{{ 2,4,6 }, {8,10,12}});
      Assert.assertTrue("Matrices " + b + " and " + c + " are not the same", b.equals(c));
   }

   @Test
   public void testAdd() {
      Matrix a = new Matrix( new double[][]{{ 1,2,3 }, {4,5,6}});
      Matrix b = a.transpose().plus(a.transpose().mult(2)).transpose();
      Matrix c = new Matrix( new double[][]{{ 3,6,9 }, {12,15,18}});
      Assert.assertTrue("Matrices " + b + " and " + c + " are not the same", b.equals(c));
   }

   @Test
   public void testDot1() {
      Matrix m = new Matrix( new double[][]{{ 1,2,3 }, {4,5,6}});
      double p1[] = new double[]{ 1, 2 };
      double p2[] = new double[]{ 1, 2, 3 };
      double r1[] = m.dotR(p1);
      double r2[] = m.transpose().dotR(p2);
      double e1[] = new double[]{ 9, 12, 15 };
      double e2[] = new double[]{ 14, 32 };
      Assert.assertTrue("Matrices " + ArrayTools.concat(r1) + " and " + ArrayTools.concat(e1) + " are not the same", ArrayTools.equals(r1,e1) );
      Assert.assertTrue("Matrices " + ArrayTools.concat(r2) + " and " + ArrayTools.concat(e2) + " are not the same", ArrayTools.equals(r2,e2) );
   }

}
