package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple2;

import java.util.Collection;
import java.util.Map; 

/**
 * A ArrayMap of a key and two values
 * @author jeroen
 */
public class ArrayMap3<K, V1, V2> extends ArrayMap<K, Tuple2<V1, V2>> {

   public static Log log = new Log(ArrayMap3.class);
   
   public ArrayMap3( ) {
      super();
   }
   
   public void add(K k, V1 v1, V2 v2) {
       super.add( k, new Tuple2<V1, V2>(v1, v2));
   }
   
   public static <K,V,W> ArrayMap3<K,V,W> invert2(Collection<Map.Entry<V, Tuple2<K, W>>> c) {
       ArrayMap3<K, V, W> map = new ArrayMap3();
       for (Map.Entry<V, Tuple2<K, W>> entry : c) {
           map.add(entry.getValue().key, new Tuple2<V, W>(entry.getKey(), entry.getValue().value));
       }
       return map;
   } 
   
   public static <K,V,W> ArrayMap3<K,V,W> invert3(Collection<? extends Map.Entry<V, Tuple2<W, K>>> c) {
       ArrayMap3<K, V, W> map = new ArrayMap3();
       for (Map.Entry<V, Tuple2<W, K>> entry : c) {
           map.add(entry.getValue().value, new Tuple2<V, W>(entry.getKey(), entry.getValue().key));
       }
       return map;
   } 
   

}
