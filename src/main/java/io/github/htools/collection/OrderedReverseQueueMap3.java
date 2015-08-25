package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple2;
import java.util.Collection;
import java.util.Map;

/**
 * This collection uses a PriorityQueue to retrieve the first ordered item.
 * Different from other Collections, on iteration the items are removed from the
 * set, if iteration is terminated the remainder of items remain unsorted, but
 * iteration can be continued on the remainder of the set.
 *
 * @author Jeroen Vuurens
 */
public class OrderedReverseQueueMap3<K extends Comparable, V, W> extends OrderedReverseQueueMap<K, Tuple2<V, W>> {

    public static Log log = new Log(OrderedReverseQueueMap3.class);

    public OrderedReverseQueueMap3(int k, Collection<Map.Entry<K, Tuple2<V, W>>> collection) {
        super(k, collection);
    }

    public OrderedReverseQueueMap3(Collection<Map.Entry<K, Tuple2<V, W>>> collection) {
        super(collection);
    }

    public OrderedReverseQueueMap3(int k) {
        super(k);
    }

    public OrderedReverseQueueMap3() {
        super();
    }
    
    public boolean add(K key, V value, W value2) {
        return super.add(key, new Tuple2<V, W>(value, value2));
    }
}
