package io.github.repir.tools.type;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.Log;

public class SparseDoubleMatrix {

   public static Log log = new Log(SparseDoubleMatrix.class);
   public SparseDoubleArray rows[];

   public SparseDoubleMatrix(SparseDoubleArray... value) {
      rows = new SparseDoubleArray[value.length];
      for (int i = 0; i < value.length; i++) {
         rows[i] = value[i].alignSpace(value);
      }
   }

   /**
    * @return a shallow copy of the SparseDoubleMatrix to a Matrix 
    */
   public Matrix toMatrix() {
      Matrix m = new Matrix( rows.length, rows[0].value.length );
      for (int r = 0; r < rows.length; r++)
         m.value[r] = rows[r].value;
      return m;
   }
   
   public double get( int row, int column ) {
      return rows[ row ].get(column);
   }
   
   public void set( int row, int column, double value ) {
      rows[ row ].set(column, value);
   }
}
