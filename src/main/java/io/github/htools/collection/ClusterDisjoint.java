package io.github.htools.collection;

import io.github.htools.lib.Log;

import java.util.Collection;
import java.util.HashSet;

/**
 * Maintains clusters of type K, of are merged when two elements are said to be
 * in the same cluster.
 * @author jeroen
 */
public class ClusterDisjoint<K> extends HashMapSet<K, K> {

   public static Log log = new Log(ClusterDisjoint.class);
   
   public ClusterDisjoint( ) {
      super();
   }
   
   public void add(K k) {
       add(k, k);
   }
   
   public void join(K k, K l) {
       HashSet<K> setk = this.get(k);
       if (setk == null) {
           setk = getSet(l);
           setk.add(k);
           put(k, setk);
           return;
       }
       HashSet<K> setl = this.get(l);
       if (setl == null) {
           setk.add(l);
           put(l, setk);
           return;
       }
       if (setk.size() < setl.size()) {
           setl.addAll(setk);
           for (K n : setk)
               put(n, setl);
       } else {
           setk.addAll(setl);
           for (K n : setl)
               put(n, setk);
       }
   }
   
   public void join(Collection<K> keys) {
       HashSet<K> newset = new HashSet(keys);
       for (K key : keys) {
           HashSet<K> setkey = get(key);
           if (setkey != null) {
              newset.addAll(setkey);
           }
       }
       for (K key : newset) {
           put(key, newset);
       }
   }
   
   public HashSet<HashSet<K>> getClusters() {
       return new HashSet(values());
   }
}
