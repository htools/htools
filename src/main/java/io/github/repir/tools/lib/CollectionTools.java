package io.github.repir.tools.lib;

import io.github.repir.tools.type.KV3;
import io.github.repir.tools.type.Tuple2;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author jeroen
 */
public enum CollectionTools {;

   public static Log log = new Log(CollectionTools.class);

   /**
    * Clusters collections in the list that have shared members
    * @param list 
    */
   public static void cluster(ArrayList<? extends Collection> list) {
       for (int i = 1; i < list.size(); i++) {
           Collection c = list.get(i);
           Collection match = null;
           for (int j = 0; j < i; j++) {
               Collection s = list.get(j);
               for (Object o : c) {
                   if (s.contains(o)) {
                      if (match == null) {
                          match = s;
                      } else {
                          match.addAll(s);
                          list.remove(j);
                          i--;
                          j--;
                      }
                      break;
                   }
               }
           }
           if (match != null) {
               match.addAll(c);
               list.remove(i--);
           }
       }
   }
   
   public static <K> HashSet<K> createSet(K ... values) {
       HashSet<K> set = new HashSet(values.length);
       for (K value : values) {
           set.add(value);
       }
       return set;
   }
   
   public static <K> ArrayList<K> createList(K ... values) {
       ArrayList<K> list = new ArrayList(values.length);
       for (K value : values) {
           list.add(value);
       }
       return list;
   }
   
    public static <K, V> Map<K, V> invert(Collection<? extends Map.Entry<V, K>> c, Map<K, V> dest) {
        for (Map.Entry<V, K> entry : c) {
            dest.put(entry.getValue(), entry.getKey());
        }
        return dest;
    }

    public static <K, V> Collection<Map.Entry<K, V>> invert(Collection<? extends Map.Entry<V, K>> c, Collection<Map.Entry<K, V>> dest) {
        for (Map.Entry<V, K> entry : c) {
            dest.add(new Tuple2<K, V>(entry.getValue(), entry.getKey()));
        }
        return dest;
    }

    public static <K, V, W> Collection<KV3<K, V, W>> invert3(
            Map<V, ? extends Map.Entry<W, K>> c, 
            Collection<KV3<K, V, W>> dest) {
        for (Map.Entry<V, ? extends Map.Entry<W, K>> entry : c.entrySet()) {
            dest.add(new KV3<K, V, W>(entry.getValue().getValue(), entry.getKey(), entry.getValue().getKey()));
        }
        return dest;
    }

    public static <K, V> Map<K, V> invert(Map<V, K> c, Map<K, V> dest) {
        return invert(c.entrySet(), dest);
    }
    
    public static <K, V> Map<K, V> invert(AbstractMap<V, K> c, AbstractMap<K, V> dest) {
        return invert(c.entrySet(), dest);
    }
    
    public static <K, V> Collection<Map.Entry<K, V>> invert(AbstractMap<V, K> c, Collection<Map.Entry<K, V>> dest) {
        return invert(c.entrySet(), dest);
    }
    
    public static boolean containsNone(Collection a, Collection b) {
        if (b.size() > a.size()) {
            return containsNone(b, a);
        } else if (b.size() < 3) {
            for (Object o : b) {
                for (Object oo : a) {
                    if (oo.equals(o))
                        return false;
                }
            }
            return true;
        } else {
            return containsNone(new HashSet(a), b);
        }
    }
    
    public static boolean containsNone(HashSet a, Collection b) {
        for (Object o : b)
            if (a.contains(o))
                return false;
        return true;
    }
    
    public static boolean containsNone(HashSet a, HashSet b) {
        if (b.size() > a.size())
            return containsNone(b, a);
        for (Object o : b)
            if (a.contains(o))
                return false;
        return true;
    }
    
    public static <T> ArrayList<T> intersection(Collection<T> a, Collection<T> b) {
        if (b.size() > a.size()) {
            return intersection(b, a);
        } else {
            if (b.size() > 20 && !(a instanceof HashSet)) {
                a = new HashSet(a);
            }
            ArrayList<T> result = new ArrayList();
            for (T objb : b) {
                if (a.contains(objb)) {
                    result.add(objb);
                }
            }
            return result;
        }
    }
    
}
