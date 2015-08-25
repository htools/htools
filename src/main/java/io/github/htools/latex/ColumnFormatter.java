package io.github.htools.latex;
import io.github.htools.latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public abstract class ColumnFormatter {
   Tabular tabular;
   int column;
   
   public ColumnFormatter( Tabular tabular, int column ) {
      this.tabular = tabular;
      this.column = column;
   }
   
   public abstract void format( Cell c, Object v );
   
   public abstract String getColumnSpec();
   
   public abstract int getColumnWidth(String value);
}
