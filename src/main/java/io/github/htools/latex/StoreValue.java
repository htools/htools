package io.github.htools.latex;
import io.github.htools.latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public class StoreValue extends LeftAlign {
   public StoreValue( Tabular tabular, int column) {
      super( tabular, column );
   }

   @Override
   public void format(Cell c, Object v) {
      c.value = v.toString();
   } 
}
