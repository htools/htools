package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.type.Tuple3;
import java.util.HashMap;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class HashMap4<K, V1, V2, V3> extends HashMap<K, Tuple3<V1, V2, V3>> {

   public static Log log = new Log(HashMap4.class);
   
   public HashMap4( ) {
      super();
   }
   
   public void put(K k, V1 v1, V2 v2, V3 v3) {
       super.put( k, new Tuple3<V1, V2, V3>(v1, v2, v3));
   }
}
