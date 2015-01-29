package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;
import io.github.repir.tools.Latex.Tabular.Row;
import io.github.repir.tools.lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class Upperline implements RowModifier {
   
   @Override
   public void modify(Row c, StringBuilder sb) {
      sb.insert(0, "\\hline\n");
   }

   @Override
   public void modify(Cell c) { }
   
}
