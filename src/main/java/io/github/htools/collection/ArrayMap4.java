package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple3;

/**
 * An ArrayMap of a key and three values
 * @author jeroen
 */
public class ArrayMap4<K, V1, V2, V3> extends ArrayMap<K, Tuple3<V1, V2, V3>> {

   public static Log log = new Log(ArrayMap4.class);
   
   public ArrayMap4( ) {
      super();
   }
   
   public void add(K k, V1 v1, V2 v2, V3 v3) {
       super.add( k, new Tuple3<V1, V2, V3>(v1, v2, v3));
   }
   
   public void addSorted(K k, V1 v1, V2 v2, V3 v3) {
       super.addSorted( k, new Tuple3<V1, V2, V3>(v1, v2, v3));
   }
}
