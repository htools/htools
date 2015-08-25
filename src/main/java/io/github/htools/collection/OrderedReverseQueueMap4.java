package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple3;
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
public class OrderedReverseQueueMap4<K extends Comparable, V, W, X> extends OrderedReverseQueueMap<K, Tuple3<V, W, X>> {

    public static Log log = new Log(OrderedReverseQueueMap4.class);

    public OrderedReverseQueueMap4(int k, Collection<Map.Entry<K, Tuple3<V, W, X>>> collection) {
        super(k, collection);
    }

    public OrderedReverseQueueMap4(Collection<Map.Entry<K, Tuple3<V, W, X>>> collection) {
        super(collection);
    }

    public OrderedReverseQueueMap4(int k) {
        super(k);
    }

    public OrderedReverseQueueMap4() {
        super();
    }
    
    public boolean add(K key, V value, W value2, X value3) {
        return super.add(key, new Tuple3<V, W, X>(value, value2, value3));
    }
}
