package io.github.repir.tools.Latex;
import java.text.DecimalFormat;
import io.github.repir.tools.Latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public class FixedDecimal3 extends RightAlign {
   public FixedDecimal3( Tabular tabular, int column) {
      super( tabular, column );
   }
   
   @Override
   public void format(Cell cell, Object v) {
      Double d = (Double)v;
      cell.value = new DecimalFormat("0.000").format(d);    
   }
}
