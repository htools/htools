package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public interface CellModifier {
   public void modify( Cell c );
}
