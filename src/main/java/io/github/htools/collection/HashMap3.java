package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.KV;

import java.util.HashMap;

/**
 * A HashMap that contains a key and two values
 * @author jeroen
 */
public class HashMap3<K, V1, V2> extends HashMap<K, KV<V1, V2>> {

   public static Log log = new Log(HashMap3.class);
   
   public HashMap3( ) {
      super();
   }
   
   public KV<V1, V2> put(K k, V1 v1, V2 v2) {
       KV<V1, V2> value = new KV<V1, V2>(v1, v2);
       super.put( k, value);
       return value;
   }
}
