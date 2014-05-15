package io.github.repir.tools.Latex;
import java.text.DecimalFormat;
import io.github.repir.tools.Latex.Tabular.Cell;
import io.github.repir.tools.Latex.Tabular.Row;

/**
 *
 * @author Jeroen Vuurens
 */
public class Decimal2 extends Decimal {
   
   
   public Decimal2( Tabular tabular, int column) {
      super( tabular, column, 2 );
   }
   
   @Override
   public void format(Cell cell, Object v) {
      Double d = (Double)v;
      cell.value = new DecimalFormat("0.00").format(d);    
   }
}
