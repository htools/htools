package io.github.repir.tools.Collection;

import io.github.repir.tools.Collection.ArrayMap.Entry;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MapTools;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * This collection uses a PriorityQueue to retrieve the first ordered item.
 * Different from other Collections, on iteration the items are removed from the
 * set, if iteration is terminated the remainder of items remain unsorted, but
 * iteration can be continued on the remainder of the set.
 *
 * @author Jeroen Vuurens
 */
public class OrderedQueueMap<K extends Comparable, V> extends OrderedQueueSet<Map.Entry<K, V>> {

    public static Log log = new Log(OrderedQueueMap.class);

    public OrderedQueueMap(Comparator<? super Map.Entry<K, V>> comparator) {
        super(comparator);
    }

    public OrderedQueueMap(int k, Collection<Map.Entry<K, V>> collection, Comparator<? super Map.Entry<K, V>> comparator) {
        super(k, collection, comparator);
    }

    public OrderedQueueMap(int k, Comparator<? super Map.Entry<K, V>> comparator) {
        super(k, comparator);
    }

    public OrderedQueueMap(Collection<Map.Entry<K, V>> collection, Comparator<? super Map.Entry<K, V>> comparator) {
        super(collection, comparator);
    }

    public OrderedQueueMap(int k, Collection<Map.Entry<K, V>> collection) {
        super(k, collection, new StdComparator<K, V>());
    }

    public OrderedQueueMap(Collection<Map.Entry<K, V>> collection) {
        super(collection, new StdComparator<K, V>());
    }

    public OrderedQueueMap(int k) {
        super(k, new StdComparator<K, V>());
    }

    public OrderedQueueMap() {
        super(new StdComparator<K, V>());
    }

    public boolean add(K key, V value) {
        return super.add(new Entry<K, V>(key, value));
    }

    @Override
    public String toString() {
        return MapTools.toString(this);
    }

    private static class StdComparator<K extends Comparable, V> implements Comparator<Map.Entry<K, V>> {

        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }
}
