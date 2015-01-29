package io.github.repir.tools.Latex;
import io.github.repir.tools.Latex.Tabular.Cell;
import io.github.repir.tools.Latex.Tabular.Row;
import io.github.repir.tools.lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public abstract class TableTemplate {
   Tabular tabular;
   
   public TableTemplate( Tabular tabular ) {
      this.tabular = tabular;
   }
   
   public abstract String header( Tabular t );
   
   public abstract String footer( Tabular t );
   
   public abstract ColumnFormatter columDefault(int column);
   
}
