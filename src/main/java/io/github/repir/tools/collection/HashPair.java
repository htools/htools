package io.github.repir.tools.collection;

import io.github.repir.tools.type.Tuple2;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.type.KV;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class HashPair<K, V> extends HashSet<Tuple2<K, V>> {

   public static Log log = new Log(HashPair.class);
   
   public HashPair( ) {
      super();
   }
   
   public void add(K k, V v) {
       super.add( new Tuple2<K, V>(k, v));
   }
   
   public boolean contains(K k, V v) {
       return super.contains( new Tuple2<K, V>(k, v));
   }
   
   public boolean remove(K k, V v) {
       return super.remove( new Tuple2<K, V>(k, v));
   }
}
