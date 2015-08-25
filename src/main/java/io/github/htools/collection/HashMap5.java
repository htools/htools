package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.KV4;
import java.util.HashMap;

/**
 * A HashMap that contains a key and four values
 * <p>
 * @author jeroen
 */
public class HashMap5<K, V1, V2, V3, V4> extends HashMap<K, KV4<V1, V2, V3, V4>> {

   public static Log log = new Log(HashMap5.class);
   
   public HashMap5( ) {
      super();
   }
   
   public void put(K k, V1 v1, V2 v2, V3 v3, V4 v4) {
       super.put( k, new KV4<V1, V2, V3, V4>(v1, v2, v3, v4));
   }
}
