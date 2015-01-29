package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;
import io.github.repir.tools.Latex.Tabular.Row;
import io.github.repir.tools.lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public interface ColumnModifier {
   public void modify( Cell c );
}
