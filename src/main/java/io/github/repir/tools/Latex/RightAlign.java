package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public abstract class RightAlign extends ColumnFormatter {
   public RightAlign( Tabular tabular, int column ) {
      super(tabular, column);
   }
   @Override
   public String getColumnSpec() {
      return "r";
   }

   @Override
   public int getColumnWidth(String value) {
      return 1;
   }
}
