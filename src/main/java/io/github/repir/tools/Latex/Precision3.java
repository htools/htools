package io.github.repir.tools.Latex;
import java.text.DecimalFormat;
import io.github.repir.tools.Latex.Tabular.Cell;
import static io.github.repir.tools.lib.PrintTools.sprintf;
import io.github.repir.tools.lib.StrTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class Precision3 extends RightAlign {
   public Precision3( Tabular tabular, int column) {
      super( tabular, column);
   }

   @Override
   public void format(Cell cell, Object v) {
      boolean inp = false;
      int precision = 3;
      Double d = (Double)v;
      cell.value = new DecimalFormat("0." + StrTools.concat("0", precision)).format(d);
      if (cell.value.equals(new DecimalFormat("0." + StrTools.concat("0", precision)).format(0))) {
         cell.value = "0." + StrTools.concat("0", precision-1) + "0";
      }
   }
}
