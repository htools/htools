package io.github.repir.tools.collection;

import io.github.repir.tools.type.Tuple2;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.type.KV;
import java.util.HashMap;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class HashPairMap<K, V1, V2> extends HashMap<Tuple2<K, V1>, V2> {

   public static Log log = new Log(HashPairMap.class);
   
   public HashPairMap( ) {
      super();
   }
   
   public void put(K k, V1 v1, V2 v2) {
       super.put( new Tuple2<K, V1>(k, v1), v2);
   }
   
   public V2 get(K k, V1 v1) {
       return super.get( new Tuple2<K, V1>(k, v1));
   }
   
   public boolean containsKey(K k, V1 v1) {
       return super.containsKey(new Tuple2<K, V1>(k, v1));
   }
   
   public V2 getOrDefault(K k, V1 v1, V2 v2) {
       return super.getOrDefault(new Tuple2<K, V1>(k, v1), v2);
   }
   
   public V2 putIfAbsent(K k, V1 v1, V2 v2) {
       return super.putIfAbsent(new Tuple2<K, V1>(k, v1), v2);
   }
}
