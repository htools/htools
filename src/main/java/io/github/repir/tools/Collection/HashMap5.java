package io.github.repir.tools.Collection;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Type.Tuple4;
import java.util.HashMap;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class HashMap5<K extends Comparable, V1, V2, V3, V4> extends HashMap<K, Tuple4<V1, V2, V3, V4>> {

   public static Log log = new Log(HashMap5.class);
   
   public HashMap5( ) {
      super();
   }
   
   public void put(K k, V1 v1, V2 v2, V3 v3, V4 v4) {
       super.put( k, new Tuple4<V1, V2, V3, V4>(v1, v2, v3, v4));
   }
}
