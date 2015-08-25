package io.github.htools.latex;
import java.text.DecimalFormat;
import io.github.htools.latex.Tabular.Cell;
import static io.github.htools.lib.PrintTools.sprintf;
import io.github.htools.lib.StrTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class Precision2 extends RightAlign {
   public Precision2( Tabular tabular, int column) {
      super( tabular, column);
   }

   @Override
   public void format(Cell cell, Object v) {
      boolean inp = false;
      int precision = 2;
      Double d = (Double)v;
      cell.value = new DecimalFormat("0." + StrTools.concat("0", precision)).format(d);
      if (cell.value.equals(new DecimalFormat("0." + StrTools.concat("0", precision)).format(0))) {
         cell.value = "\\small{\\textless} 0." + StrTools.concat("0", precision-1) + "1";
      }
   }
}
