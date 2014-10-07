package io.github.repir.tools.Collection;
import io.github.repir.tools.Collection.ArrayMap.Entry;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MapTools;
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

  public TopKMap(int k, Collection<Map.Entry<K, V>> collection, Comparator<? super Map.Entry<K,V>> comparator) {
     this(k, comparator);
     addAll(collection);
  }

  public TopKMap(int k, Collection<Map.Entry<K, V>> collection) {
     this(k);
     addAll(collection);
  }

  public TopKMap(int k) {
     this(k, new StdComparator<K,V>());
  }

  public boolean add(K key, V value) {
     return super.add(new Entry<K,V>(key, value));
  }
   
  @Override
  public String toString() {
      return MapTools.toString(this);
  }
  
  private static class StdComparator<K extends Comparable,V> implements Comparator<Map.Entry<K,V>> {
        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
  }
}
