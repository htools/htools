package io.github.repir.tools.Latex;
import java.text.DecimalFormat;
import io.github.repir.tools.Latex.Tabular.Cell;
import io.github.repir.tools.lib.PrintTools;

/**
 *
 * @author Jeroen Vuurens
 */
public abstract class Decimal extends ColumnFormatter {
   public int precision;
   
   public Decimal( Tabular tabular, int column, int precision ) {
      super(tabular, column);
      this.precision = precision;
   }
   
   @Override
   public String getColumnSpec() {
      return PrintTools.sprintf("S[table-format=%d.%d] ", getMaxWidth(), getMaxPrec());
   }
   
   @Override
   public int getColumnWidth(String value) {
      return 1;
   }
   
   public int getMaxWidth() {
      int max = 0;
      for (Tabular.Row r : tabular.rows) {
         if (r.cells[column] != null)
            max = Math.max(max, r.cells[ column ].value.indexOf('.'));
      }
      return max;
   }

   public int getMaxPrec() {
      return precision;
   }
}
