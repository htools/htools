package io.github.repir.tools.Content;
import java.io.UnsupportedEncodingException;
import io.github.repir.tools.Lib.ByteTools;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class t {
  public static Log log = new Log( t.class ); 

   public static void main(String[] args) throws UnsupportedEncodingException {
      System.setProperty("file.encoding", "UTF-8");
      byte b[] = new byte[3];
      b[0] = 99;
      b[1] = (byte) (233 & 0xFF);
      b[2] = 99;
      log.info("%d %s ", b[1] &0xFF, ByteTools.toString(b));
      log.info("%s ", new String(b));
   }

}
