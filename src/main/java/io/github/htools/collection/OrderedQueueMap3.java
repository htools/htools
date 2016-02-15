package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.lib.MapTools;
import io.github.htools.type.KV3;
import io.github.htools.type.Tuple2;

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
public class OrderedQueueMap3<K extends Comparable, V, W> extends OrderedQueueMap<K, Tuple2<V, W>> {

    public static Log log = new Log(OrderedQueueMap3.class);

    public OrderedQueueMap3(Comparator<? super Map.Entry<K, Tuple2<V, W>>> comparator) {
        super(comparator);
    }

    public OrderedQueueMap3(int k, Collection<Map.Entry<K, Tuple2<V, W>>> collection, Comparator<? super Map.Entry<K, Tuple2<V, W>>> comparator) {
        super(k, collection, comparator);
    }

    public OrderedQueueMap3(int k, Comparator<? super Map.Entry<K, Tuple2<V, W>>> comparator) {
        super(k, comparator);
    }

    public OrderedQueueMap3(Collection<Map.Entry<K, Tuple2<V, W>>> collection, Comparator<? super Map.Entry<K, Tuple2<V, W>>> comparator) {
        super(collection, comparator);
    }

    public OrderedQueueMap3(int k, Collection<Map.Entry<K, Tuple2<V, W>>> collection) {
        super(k, collection, new StdComparator<K, Tuple2<V, W>>());
    }

    public OrderedQueueMap3(Collection<Map.Entry<K, Tuple2<V, W>>> collection) {
        super(collection, new StdComparator<K, Tuple2<V, W>>());
    }

    public OrderedQueueMap3(int k) {
        super(k, new StdComparator<K, Tuple2<V, W>>());
    }

    public OrderedQueueMap3() {
        super(new StdComparator<K, Tuple2<V, W>>());
    }

    public boolean add(K key, V value, W value2) {
        return super.add(new KV3<K, V, W>(key, value, value2));
    }

    @Override
    public String toString() {
        return MapTools.toString(this);
    }
}
