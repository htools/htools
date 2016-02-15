package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.lib.MapTools;

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
public class OrderedReverseQueueMap<K extends Comparable, V> extends OrderedQueueMap<K, V> {

    public static Log log = new Log(OrderedReverseQueueMap.class);

    public OrderedReverseQueueMap(int k, Collection<Map.Entry<K, V>> collection) {
        super(k, collection, new StdComparator<K, V>());
    }

    public OrderedReverseQueueMap(Collection<Map.Entry<K, V>> collection) {
        super(collection, new StdComparator<K, V>());
    }

    public OrderedReverseQueueMap(int k) {
        super(k, new StdComparator<K, V>());
    }

    public OrderedReverseQueueMap() {
        super(new StdComparator<K, V>());
    }

    @Override
    public String toString() {
        return MapTools.toString(this);
    }

    protected static class StdComparator<K extends Comparable, V> implements Comparator<Map.Entry<K, V>> {

        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return o2.getKey().compareTo(o1.getKey());
        }
    }
}
