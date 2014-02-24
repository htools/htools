package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;
import io.github.repir.tools.Latex.Tabular.Row;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public interface TableTemplate {
   public String header( Tabular t );
   
   public String footer( Tabular t );
   
   public ColumnFormatter columDefault();
   
}
