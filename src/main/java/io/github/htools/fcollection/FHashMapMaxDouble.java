package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple2;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A HashMap with a Double value, that supports adding values to existing keys
 * and only retaining the highest value.
 * @author jeroen
 */
public class FHashMapMaxDouble<K, V> extends Object2ObjectOpenHashMap<K, Tuple2<Double, V>> {

   public static Log log = new Log(FHashMapMaxDouble.class);
   
   public FHashMapMaxDouble( ) {
      super();
   }
   
   public void add(K k, Double d, V v) {
       Tuple2<Double, V> entry = get(k);
       if (entry == null || entry.key < d)
           put(k, new Tuple2<Double, V>(d, v));
   }
   
   public V getValue(K k) {
       Tuple2<Double, V> list = super.get(k);
       if (list != null) {
           return list.value;
       }
       return null;
   }
}
