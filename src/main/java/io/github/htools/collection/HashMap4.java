package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.KV3;

import java.util.HashMap;

/**
 * A HashMap that contains a key and three values
 * @author jeroen
 */
public class HashMap4<K, V1, V2, V3> extends HashMap<K, KV3<V1, V2, V3>> {

   public static Log log = new Log(HashMap4.class);
   
   public HashMap4( ) {
      super();
   }
   
   public void put(K k, V1 v1, V2 v2, V3 v3) {
       super.put( k, new KV3<V1, V2, V3>(v1, v2, v3));
   }
}
