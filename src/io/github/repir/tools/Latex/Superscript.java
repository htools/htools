package io.github.repir.tools.Latex;
import java.text.DecimalFormat;
import io.github.repir.tools.Latex.Tabular.Cell;
import static io.github.repir.tools.Lib.PrintTools.sprintf;
import io.github.repir.tools.Lib.StrTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class Superscript extends LeftAlign {
   public Superscript( Tabular tabular, int column) {
      super( tabular, column );
   }

   @Override
   public void format(Cell cell, Object v) {
      String s = v.toString();
      cell.value = (s.length() > 0)?("$^{" + s + "}$"):"";
   }
}
