package io.github.repir.tools.type;

import java.util.Iterator;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.Log;

/**
 * Represents a Matrix for which the row and column dimensions are switched. 
 * This fast implementation reduces costs by not actually having to physically 
 * transpose the data, but providing functions that switch row and column. Internally
 * the data is represented the same, therefore fast external routines that attempt to access
 * the data values directly should check if the Matrix is an instance of MatrixTransposed
 * and then access value[column].value[row] instead of value[row].value[column].
 * @author jeroen
 */
public class MatrixTransposed extends Matrix {

   public static Log log = new Log(MatrixTransposed.class);

   protected MatrixTransposed( Matrix m ) {
      super( m.value );
      transposed = m;
   }

   public Matrix copy() {
      Matrix result = new Matrix(getRows(), getColumns());
      copy( result );
      return result;
   }
   
   public void copy(Matrix target) {
      int rows = getRows();
      int columns = getColumns();
      for (int row = 0; row < rows; row++) {
         for (int column = 0; column < columns; column++) {
            target.value[row][column] = value[column][row];
         }
      }
   }

   /**
    * @return shallow copy of Matrix that is transposed. In the case of transposing
    * a MatrixTransposed, a Matrix is returned with the same data.
    */
   public Matrix transpose() {
      return transposed;
   }

   public Matrix dot(Matrix m) {
      Matrix result = new Matrix( getRows(), m.getColumns() );
      dot(m, result);
      return result;
   }
   
   public void dot(Matrix matrix, Matrix result) {
      int factors = getColumns();
      int rows = getRows();
      int columns = matrix.getColumns();
      if (matrix instanceof MatrixTransposed) {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               double sum = 0;
               for (int factor = 0; factor < factors; factor++) {
                  sum += value[factor][row] * matrix.value[column][factor];
               }
               result.value[row][column] = sum;
            }
         }
      } else {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               double sum = 0;
               for (int factor = 0; factor < factors; factor++) {
                  sum += value[factor][row] * matrix.value[factor][column];
               }
               result.value[row][column] = sum;
            }
         }
      }
   }

   public double[] dotR( double d[] ) {
      double r[] = new double[ getColumns() ];
      dotR( d, r );
      return r;
   }
   
   public void dotR(double d[], double result[]) {
      int factors = getRows();
      int columns = getColumns();
      for (int column = 0; column < columns; column++) {
         double sum = 0;
         for (int factor = 0; factor < factors; factor++) {
            sum += d[factor] * value[column][factor];
         }
         result[column] = sum;
      }
   }

   public double[] dotR( int d[] ) {
      double r[] = new double[ getColumns() ];
      dotR( d, r );
      return r;
   }
   
   public void dotR(int d[], double result[]) {
      int factors = getRows();
      int columns = getColumns();
      for (int column = 0; column < columns; column++) {
         double sum = 0;
         for (int factor = 0; factor < factors; factor++) {
            sum += d[factor] * value[column][factor];
         }
         result[column] = sum;
      }
   }

   public Matrix mult( double d) {
      Matrix r = new Matrix(getRows(), getColumns());
      mult( d, r );
      return r;
   }
   
   public void mult(double d, Matrix result ) {
      int rows = getRows();
      int columns = getColumns();
      for (int row = 0; row < rows; row++) {
         for (int column = 0; column < columns; column++) {
            result.value[row][column] = value[column][row] * d;
         }
      }
   }

   public Matrix plus(Matrix a) {
      Matrix r = new Matrix( getRows(), getColumns());
      plus(a, r);
      return r;
   }
   
   public void plus(Matrix a, Matrix r) {
      int rows = getRows();
      int columns = getColumns();
      if (a instanceof MatrixTransposed) {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               r.value[row][column] = value[column][row] + a.value[column][row];
            }
         }
      } else {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               r.value[row][column] = value[column][row] + a.value[row][column];
            }
         }
      }
   }

   public boolean equals(Matrix a) {
      int rows = getRows();
      int columns = a.getColumns();
      if (a instanceof MatrixTransposed) {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               if (value[column][row] != a.value[column][row])
                  return false;
            }
         }
      } else {
         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
              if (value[column][row] != a.value[row][column])
                 return false;
            }
         }
      } 
      return true;
   }

   public int getRows() {
      return value[0].length;
   }

   public int getColumns() {
      return value.length;
   }

   public double[] getRow( int row ) {
      log.fatal("cannot call getRow() on a TransposedMatrix");
      return null;
   }
   
   public double get(int row, int column) {
      return value[column][row]; 
   }
   
   public void set(int row, int column, double v) {
      value[column][row] = v;
   }
}
