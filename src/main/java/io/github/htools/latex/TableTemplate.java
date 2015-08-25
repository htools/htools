package io.github.htools.latex;
import io.github.htools.latex.Tabular.Cell;
import io.github.htools.latex.Tabular.Row;
import io.github.htools.lib.Log; 

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
