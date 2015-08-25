package io.github.htools.latex;
import java.text.DecimalFormat;
import io.github.htools.latex.Tabular.Cell;
import static io.github.htools.lib.PrintTools.sprintf;
import io.github.htools.lib.StrTools;

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
