package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
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
       log.info("join %s %s", k, l);
       HashSet<K> setk = this.get(k);
       if (setk == null) {
           setk = getSet(l);
           setk.add(k);
           put(k, setk);
           return;
       }
       HashSet<K> setl = this.get(l);
       log.info("setk %s", setk);
       log.info("setl %s", setl);
       if (setl == null) {
           setk.add(l);
           put(l, setk);
           return;
       }
       if (setk.size() < setl.size()) {
           setl.addAll(setk);
           for (K n : setk)
               put(n, setl);
           if (setl.contains(415561154734l) && setl != get(415561154734l)) {
              log.info("newsetl %s %s", setl, get(415561154734l));
           }
       } else {
           setk.addAll(setl);
           for (K n : setl)
               put(n, setk);
           if (setk.contains(415561154734l) && setk != get(415561154734l)) {
              log.info("newsetk %s %s", setk, get(415561154734l));
           }
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
       if (newset.contains(415561154734l) && newset != get(415561154734l)) {
           log.info("newset %s", newset);
       }
   }
   
   public HashSet<HashSet<K>> getClusters() {
       return new HashSet(values());
   }
}
