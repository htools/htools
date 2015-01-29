package io.github.repir.tools.collection;

import io.github.repir.tools.type.Tuple2;
import io.github.repir.tools.lib.Log;
import java.util.HashMap;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class HashMap3<K, V1, V2> extends HashMap<K, Tuple2<V1, V2>> {

   public static Log log = new Log(HashMap3.class);
   
   public HashMap3( ) {
      super();
   }
   
   public void put(K k, V1 v1, V2 v2) {
       super.put( k, new Tuple2<V1, V2>(v1, v2));
   }
}
