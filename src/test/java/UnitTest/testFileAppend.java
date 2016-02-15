package UnitTest;

import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class testFileAppend {
  public static Log log = new Log( testFileAppend.class ); 

   public static void main(String[] args) {
     s(0.05, 1);
     s(0.05, 2);
     s(0.05, 3);
     s(0.05, 4);
     s(0.054, 1);
     s(0.054, 2);
     s(0.054, 3);
     s(0.054, 4);
     s(0.055, 1);
     s(0.055, 2);
     s(0.055, 3);
     s(0.055, 4);
     s(0.094, 1);
     s(0.094, 2);
     s(0.094, 3);
     s(0.094, 4);
     s(0.095, 1);
     s(0.095, 2);
     s(0.095, 3);
     s(0.095, 4);
  }
  
  public static void s(double d, int prec) {
     log.info("%f %d %s", d, prec, MathTools.sigLatex(d, prec));
  }

  
  
}
