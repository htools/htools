package io.github.htools.latex;
import io.github.htools.latex.Tabular.Cell;
import io.github.htools.latex.Tabular.Row;
import io.github.htools.lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class Underline implements RowModifier {
   
   @Override
   public void modify(Row c, StringBuilder sb) {
      sb.append("\\hline\n");
   }

   @Override
   public void modify(Cell c) { }
   
}
