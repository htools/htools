package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public interface ColumnFormatter {
   public void format( Cell c, Object v );
   
   public String getColumnSpec();
}
