package io.github.repir.tools.collection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.MapTools;
import io.github.repir.tools.type.KV;
import io.github.repir.tools.type.Tuple2;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * This collection retains the TopK Key Value entries that are added. By default
 * the keys are compared to decide the TopK.
 * @author Jeroen Vuurens
 */
public class TopKMap<K extends Comparable, V> extends TopK<Map.Entry<K,V>> {
  public static Log log = new Log( TopKMap.class );

  public TopKMap(int k, Comparator<? super Map.Entry<K,V>> comparator) {
     super(k, comparator);
  }

  public TopKMap(int k, Iterable<Map.Entry<K, V>> collection, Comparator<? super Map.Entry<K,V>> comparator) {
     this(k, comparator);
     addAll(collection);
  }

  public TopKMap(int k, Iterable<Map.Entry<K, V>> collection) {
     this(k);
     addAll(collection);
  }
  
  public void addAll(Iterable<Map.Entry<K, V>> collection) {
      for (Map.Entry<K, V> entry : collection)
          add(entry);
  }

  public TopKMap(int k) {
     this(Math.abs(k), (k>0)?new StdComparator<K,V>():new DescComparator<K,V>());
  }

  public TopKMap(int k, AbstractMap<K, V> map) {
     this(k, map.entrySet(), new StdComparator<K,V>());
  }

  public boolean add(K key, V value) {
     return super.add(new KV<K,V>(key, value));
  }
   
  public Collection<V> values() {
      ArrayList<V> values = new ArrayList();
      for (Map.Entry<K, V> entry : this) {
          values.add(entry.getValue());
      }
      return values;
  }
  
  @Override
  public String toString() {
      return MapTools.toString(this);
  }
  
    public static <K, V> ArrayMap<K, V> invert(Iterable<? extends Map.Entry> c) {
        ArrayMap<K, V> map = new ArrayMap();
        for (Map.Entry<V, K> entry : c) {
            map.add(entry.getValue(), entry.getKey());
        }
        return map;
    }
  
  private static class StdComparator<K extends Comparable,V> implements Comparator<Map.Entry<K,V>> {
        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
  }
  
  private static class DescComparator<K extends Comparable,V> implements Comparator<Map.Entry<K,V>> {
        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return o2.getKey().compareTo(o1.getKey());
        }
  }
}
