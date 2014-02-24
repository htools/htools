package UnitTest;
import io.github.repir.tools.Lib.Log; 
import io.github.repir.tools.Lib.MathTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class testSigLatex {
  public static Log log = new Log( testSigLatex.class ); 

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
