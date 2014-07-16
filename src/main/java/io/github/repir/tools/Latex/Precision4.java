package io.github.repir.tools.Latex;
import java.text.DecimalFormat;
import io.github.repir.tools.Latex.Tabular.Cell;
import static io.github.repir.tools.Lib.PrintTools.sprintf;
import io.github.repir.tools.Lib.StrTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class Precision4 extends Decimal {
   public Precision4( Tabular tabular, int column) {
      super( tabular, column, 4 );
   }

   @Override
   public void format(Cell cell, Object v) {
      boolean inp = false;
      int precision = 4;
      Double d = (Double)v;
      double ep = Math.log10(d);
      if (ep > -precision) {
         cell.value = new DecimalFormat("0." + StrTools.concat("0", precision)).format(d);
         return;
      }
      int p = (int)Math.ceil(-ep);
      String s = new DecimalFormat("0." + StrTools.concat("0", p)).format(d);
      char c = s.charAt(s.length()-1);
      char c1 = s.charAt(s.length()-2);
      if (c1 > '0' && c1 <= '9')
         cell.value = sprintf("$%s \\cdot 10^{-%d}$", c1, p-1);
      else
         cell.value = sprintf("$%s \\cdot 10^{-%d}$", c, p);
   }
}
