package io.github.htools.latex;
import java.text.DecimalFormat;
import io.github.htools.latex.Tabular.Cell;

/**
 *
 * @author Jeroen Vuurens
 */
public class Decimal4 extends Decimal {
   public Decimal4( Tabular tabular, int column) {
      super( tabular, column, 4 );
   }
   
   @Override
   public void format(Cell cell, Object v) {
      Double d = (Double)v;
      cell.value = new DecimalFormat("0.0000").format(d);    
   }
}
