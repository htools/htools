package io.github.repir.tools.Latex;
import java.text.DecimalFormat;
import io.github.repir.tools.Latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public class Decimal2 implements ColumnFormatter {

   @Override
   public void format(Cell cell, Object v) {
      Double d = (Double)v;
      cell.value = new DecimalFormat("0.00").format(d);    
   }

   @Override
   public String getColumnSpec() {
      return "r";
   }
}
