package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public class StoreValue implements ColumnFormatter {

   @Override
   public void format(Cell c, Object v) {
      c.value = v.toString();
   } 

   @Override
   public String getColumnSpec() {
      return "l";
   }
}
