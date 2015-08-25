package io.github.htools.latex;
import io.github.htools.latex.Tabular.Cell;
import io.github.htools.latex.Tabular.Row;
import io.github.htools.lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class TableDefault extends TableTemplate {
   public TableDefault( Tabular tabular ) {
      super( tabular );
   }

   @Override
   public String header(Tabular t) {
      StringBuilder sb = new StringBuilder();
      sb.append("\\begin{table}\n");
      sb.append("\\scriptsize\n");
      sb.append("\\tabcolsep=0.02cm\n");
      sb.append("\\begin{tabular}{" + t.getColumnSpecs() + "}\n");
      return sb.toString();
   }
      

   @Override
   public String footer(Tabular t) {
      StringBuilder sb = new StringBuilder();
      sb.append("\\end{tabular}\n");
      if (t.caption != null) {
         sb.append("\\caption{").append(t.caption).append("}\n");
      }
      sb.append("\\end{table}\n");
      return sb.toString();
   }

   @Override
   public ColumnFormatter columDefault(int column) {
      return new StoreValue(tabular, column);
   }

}
