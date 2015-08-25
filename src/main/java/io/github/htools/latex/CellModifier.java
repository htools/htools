package io.github.htools.latex;
import io.github.htools.latex.Tabular.Cell;
import io.github.htools.lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public interface CellModifier {
   public void modify( Cell c );
}
