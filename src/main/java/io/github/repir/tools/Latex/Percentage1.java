package io.github.repir.tools.Latex;
import java.text.DecimalFormat;
import io.github.repir.tools.Latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public class Percentage1 implements ColumnFormatter {

   @Override
   public void format(Cell cell, Object v) {
      Double d = (Double)v;
      cell.value = ((d < 0)?'-':'+') + new DecimalFormat("#0.0").format(100 * d) + "\\%";    
   }

   @Override
   public String getColumnSpec() {
      return "r";
   }
}
