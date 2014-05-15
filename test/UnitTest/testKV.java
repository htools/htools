package UnitTest;
import io.github.repir.tools.DataTypes.HashMap;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class testKV {
  public static Log log = new Log( testKV.class ); 

   public static void main(String[] args) {
      HashMap<String, Integer> kv = new HashMap<String, Integer>();
      kv.put("aap", 1);
      kv.put("noot", 2);
   }

}
