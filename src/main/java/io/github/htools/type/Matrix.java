package io.github.htools.type;

import io.github.htools.lib.Log;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import io.github.htools.lib.PrintTools;
import io.github.htools.lib.RandomTools;
import io.github.htools.lib.RandomTools;

/**
 * Wrapper around a 2-dimensional double array
 */
public class Matrix {

   public static Log log = new Log(Matrix.class);
   public double value[][];
   Matrix transposed = null; 

   public Matrix(double[][] values) {
      value = values;
   }

   public Matrix(double[] values) {
      value = new double[1][];
      value[0] = values;
   }

   public Matrix(int rows, int columns) {
      value = new double[rows][columns];
   }

   /**
    * Fill the array with random numbers that follow a Gaussian distribution
    * @param standarddeviation
    */
   public void fillRandom(double standarddeviation) {
      for (int row = 0; row < value.length; row++) {
         for (int column = 0; column < value[row].length; column++) {
            value[row][column] = RandomTools.getStdNormal() * standarddeviation;
         }
      }
   }

   public Matrix truncMax(double max) {
      for (int row = 0; row < value.length; row++) {
         for (int column = 0; column < value[row].length; column++) {
            if (value[row][column] > max)
               value[row][column] = max;
         }
      }
      return this;
   }

   public Matrix truncMin(double min) {
      for (int row = 0; row < value.length; row++) {
         for (int column = 0; column < value[row].length; column++) {
            if (value[row][column] < min)
               value[row][column] = min;
         }
      }
      return this;
   }

   /**
    * @return a deep copy of the Matrix
    */
   public Matrix copy() {
      Matrix target = new Matrix(getRows(), getColumns());
      copy( target );
      return target;
   }
   
   /**
    * Creates a deep copy of the Matrix and store it in target
    * @param target 
    */
   public void copy( Matrix target ) {
      int rows = getRows();
      int columns = getColumns();
      for (int row = 0; row < rows; row++) {
         System.arraycopy(value[row], 0, target.value[row], 0, columns);
      }
   }
   
   public void copyRowFrom( int row, double[] source ) {
      System.arraycopy(source, 0, value[row], 0, source.length);
   }

   /**
    * @return shallow copy of Matrix that is transposed
    */
   public Matrix transpose() {
      if (transposed == null)
         transposed = new MatrixTransposed(this);
      return transposed;
   }

   /**
    * Multiplies two matrices. Matrix b must have the same number of rows
    * as this matrix has column, this function does not check.
    * @param b
    * @return dot product of (this * b) 
    */
   public Matrix dot(Matrix b) {
      Matrix r = new Matrix(getRows(), b.getColumns());
      dot( b, r );
      return r;
   }
   
   /**
    * Dot product with Matrix b. Matrix b must have the same number of rows
    * as this matrix has column, and the Matrix result must have a number of rows
    * equal to this Matrix's number of rows and a number of columns equal to 
    * Matrix b's number of columns. This function does not check.
    * @param b
    * @param result Matrix to store the result in
    */
   public void dot(Matrix b, Matrix result) {
      int factors = getColumns();
      int rows = getRows();
      int columns = b.getColumns();
      if (b instanceof MatrixTransposed) {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               double sum = 0;
               for (int factor = 0; factor < factors; factor++) {
                  sum += value[row][factor] * b.value[column][factor];
               }
               result.value[row][column] = sum;
            }
         }
      } else {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               double sum = 0;
               for (int factor = 0; factor < factors; factor++) {
                  sum += value[row][factor] * b.value[factor][column];
               }
               result.value[row][column] = sum;
            }
         }
      }
   }
   
   public Matrix isDot(Matrix a, Matrix b) {
      a.dot(b, this);
      return this;
   }

   public Matrix isPlus(Matrix a, Matrix b) {
      a.plus(b, this);
      return this;
   }

   public Matrix isMinus(Matrix a, Matrix b) {
      a.minus(b, this);
      return this;
   }

   public Matrix isMult(double b) {
      this.mult(b, this);
      return this;
   }

   /**
    * Warning does not check dimensions
    * @param d
    * @return reverse dot product: d * this
    */
   public double[] dotR(double d[]) {
      double result[] = new double[ getColumns() ];
      dotR( d, result);
      return result;
   }

   /**
    * Warning, does not check dimensions
    * @param d
    * @param result store the reverse dot product d * this in result
    */
   public void dotR(double d[], double result[] ) {
      int factors = getRows();
      int columns = getColumns();
      for (int column = 0; column < columns; column++) {
         double sum = 0;
         for (int factor = 0; factor < factors; factor++) {
            sum += d[factor] * value[factor][column];
         }
         result[column] = sum;
      }
   }

   public double[] dotR(int d[]) {
      double result[] = new double[ getColumns() ];
      dotR( d, result);
      return result;
   }

   public void dotR(int d[], double result[] ) {
      int factors = getRows();
      int columns = getColumns();
      for (int column = 0; column < columns; column++) {
         double sum = 0;
         for (int factor = 0; factor < factors; factor++) {
            sum += d[factor] * value[factor][column];
         }
         result[column] = sum;
      }
   }

   /**
    * @param d 
    * @param result = scalar multiplication d * this 
    */
   public void mult(double d, Matrix result) {
      int rows = getRows();
      int columns = getColumns();
      for (int row = 0; row < rows; row++) {
         for (int column = 0; column < columns; column++) {
            result.value[row][column] = value[row][column] * d;
         }
      }
   }
   
   /**
    * @param d
    * @return Matrix that is the scalar multiplication d * this
    */
   public Matrix mult(double d) {
      Matrix result = new Matrix(getRows(), getColumns());
      mult( d, result );
      return result;
   }

   /**
    * @param a
    * @param result = this + a
    */
   public void plus(Matrix a, Matrix result) {
      int rows = getRows();
      int columns = a.getColumns();
      if (a instanceof MatrixTransposed) {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               result.value[row][column] = value[row][column] + a.value[column][row];
            }
         }
      } else {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               result.value[row][column] = value[row][column] + a.value[row][column];
            }
         }
      }
   }
   
   /**
    * @param a
    * @return this + a
    */
   public Matrix plus(Matrix a) {
      Matrix result = new Matrix(getRows(), getColumns());
      plus( a, result );
      return result;
   }

   /**
    * @param a
    * @param result = this + a
    */
   public void minus(Matrix a, Matrix result) {
      int rows = getRows();
      int columns = a.getColumns();
      if (a instanceof MatrixTransposed) {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               result.value[row][column] = value[row][column] - a.value[column][row];
            }
         }
      } else {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               result.value[row][column] = value[row][column] - a.value[row][column];
            }
         }
      }
   }
   
   /**
    * @param a
    * @return this + a
    */
   public Matrix minus(Matrix a) {
      Matrix result = new Matrix(getRows(), getColumns());
      minus( a, result );
      return result;
   }

   /**
    * @param a
    * @return true if all cells[row,column] contain the same values
    */
   public boolean equals(Matrix a) {
      int rows = getRows();
      int columns = a.getColumns();
      if (a instanceof MatrixTransposed) {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               if (value[row][column] != a.value[column][row]) {
                  return false;
               }
            }
         }
      } else {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               if (value[row][column] != a.value[row][column]) {
                  return false;
               }
            }
         }
      }
      return true;
   }

   public int getRows() {
      return value.length;
   }
   
   /**
    * Warning, this is a shallow reference, which doe snot work on transposed matrices
    * @param row
    * @return reference to the values in a row
    */
   public double[] getRow( int row ) {
      return value[row];
   }

   public int getColumns() {
      return value[0].length;
   }

   public double get(int row, int column) {
      return value[row][column];
   }

   public void set(int row, int column, double v) {
      value[row][column] = v;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      int rows = getRows();
      int columns = getColumns();
      sb.append("Matrix( ").append(rows).append(", ").append(columns).append(")\n");
      for (int row = 0; row < rows; row++) {
         for (int column = 0; column < columns; column++) {
            if (column > 0) {
               sb.append(", ");
            }
            sb.append(PrintTools.sprintf("%6f", value[this instanceof MatrixTransposed ? column : row][this instanceof MatrixTransposed ? row : column]));
         }
         sb.append("\n");
      }
      return sb.toString();
   }

   public void print() {
      log.printf("%s", this);
   }
}
