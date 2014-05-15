package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;

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
