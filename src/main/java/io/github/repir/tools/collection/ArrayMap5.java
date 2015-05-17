package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.type.Tuple3;
import io.github.repir.tools.type.Tuple4;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class ArrayMap5<K, V1, V2, V3, V4> extends ArrayMap<K, Tuple4<V1, V2, V3, V4>> {

   public static Log log = new Log(ArrayMap5.class);
   
   public ArrayMap5( ) {
      super();
   }
   
   public void add(K k, V1 v1, V2 v2, V3 v3, V4 v4) {
       super.add( k, new Tuple4<V1, V2, V3, V4>(v1, v2, v3, v4));
   }
   
   public void addSorted(K k, V1 v1, V2 v2, V3 v3, V4 v4) {
       super.addSorted( k, new Tuple4<V1, V2, V3, V4>(v1, v2, v3, v4));
   }
}
