package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.lib.MapTools;
import io.github.htools.type.KV4;
import io.github.htools.type.Tuple3;

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
public class OrderedQueueMap4<K extends Comparable, V, W, X> extends OrderedQueueMap<K, Tuple3<V, W, X>> {

    public static Log log = new Log(OrderedQueueMap4.class);

    public OrderedQueueMap4(Comparator<? super Map.Entry<K, Tuple3<V, W, X>>> comparator) {
        super(comparator);
    }

    public OrderedQueueMap4(int k, Collection<Map.Entry<K, Tuple3<V, W, X>>> collection, Comparator<? super Map.Entry<K, Tuple3<V, W, X>>> comparator) {
        super(k, collection, comparator);
    }

    public OrderedQueueMap4(int k, Comparator<? super Map.Entry<K, Tuple3<V, W, X>>> comparator) {
        super(k, comparator);
    }

    public OrderedQueueMap4(Collection<Map.Entry<K, Tuple3<V, W, X>>> collection, Comparator<? super Map.Entry<K, Tuple3<V, W, X>>> comparator) {
        super(collection, comparator);
    }

    public OrderedQueueMap4(int k, Collection<Map.Entry<K, Tuple3<V, W, X>>> collection) {
        super(k, collection, new StdComparator<K, Tuple3<V, W, X>>());
    }

    public OrderedQueueMap4(Collection<Map.Entry<K, Tuple3<V, W, X>>> collection) {
        super(collection, new StdComparator<K, Tuple3<V, W, X>>());
    }

    public OrderedQueueMap4(int k) {
        super(k, new StdComparator<K, Tuple3<V, W, X>>());
    }

    public OrderedQueueMap4() {
        super(new StdComparator<K, Tuple3<V, W, X>>());
    }

    public boolean add(K key, V value, W value2, X value3) {
        return super.add(new KV4<K, V, W, X>(key, value, value2, value3));
    }

    @Override
    public String toString() {
        return MapTools.toString(this);
    }
}
