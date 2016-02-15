package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple2;

import java.util.HashSet;

/**
 * A HashSet containing a key combined of two types
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
