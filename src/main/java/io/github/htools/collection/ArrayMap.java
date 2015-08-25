package io.github.htools.collection;

import io.github.htools.collection.MapKeyIterator;
import io.github.htools.lib.Log;
import io.github.htools.lib.MapTools;
import io.github.htools.lib.RandomTools;
import io.github.htools.type.KV;
import io.github.htools.type.Tuple2;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An ArrayMap is similar to a Map in that it stores Map.Entry&lt;K,V&gt;, however it
 * is no a true Map in that it can contain duplicate Keys, and does not
 * implement the Map interface because it should not be used like a Map for fast
 * access of Keys. What it does do is provide a very fast mechanism to add
 * items, sort one time and provide the sorted list as a
 * Collection&lt;Map.Entry&lt;K,V&gt;&gt;. This allows fast and easy iteration over the
 * sorted list, however the order is disrupted when the list is changed.
 * However, returning as a Collection does allow easy transformation of it's
 * contents into a Collection of any other type.
 * <p>
 * Sorting and iteration are all shallow on the collection itself. To create a
 * sorted copy the collection must be cloned.
 * <p>
 * @author jeroen
 */
public class ArrayMap<K, V> implements Iterable<Map.Entry<K, V>>, Collection<Map.Entry<K, V>> {
 
    public static Log log = new Log(ArrayMap.class);
    ArrayList<Map.Entry<K, V>> list;
    private boolean isSorted = false;
    private Comparator<Map.Entry<K, V>> comparator = null;

    public ArrayMap() {
        list = new ArrayList();
    }

    public ArrayMap(Collection<? extends Map.Entry<K, V>> c) {
        list = new ArrayList(c);
    }

    public ArrayMap(AbstractMap<K, V> c) {
        this();
        for (Map.Entry<K, V> entry : c.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public ArrayMap(int initialsize) {
        list = new ArrayList(initialsize);
    }

    public static <K, V> ArrayMap<K, V> invert(Iterable<? extends Map.Entry<V, K>> c) {
        ArrayMap<K, V> map = new ArrayMap();
        for (Map.Entry<V, K> entry : c) {
            map.add(entry.getValue(), entry.getKey());
        }
        return map;
    }

    public int indexOfValue(V value) {
        for (int i = 0; i < size(); i++)
            if (get(i).getValue().equals(value))
                return i;
        return -1;
    }
    
    public int indexOfKey(K key) {
        for (int i = 0; i < size(); i++)
            if (get(i).getKey().equals(key))
                return i;
        return -1;
    }
    
    public static <K, V> ArrayMap<K, V> invert(AbstractMap<V, K> c) {
        return invert(c.entrySet());
    }

    /**
     * @return a shallow copy, i.e. the elements are not cloned.
     */
    @Override
    public ArrayMap<K, V> clone() {
        ArrayMap<K, V> n = new ArrayMap();
        n.list = (ArrayList<Map.Entry<K, V>>) list.clone();
        n.isSorted = isSorted;
        return n;
    }

    public void add(K key, V value) {
        list.add(new Tuple2(key, value));
        isSorted = false;
    }

    public void addSorted(K key, V value) {
        if (isSorted) {
            int binarySearch = Collections.binarySearch(list, new KV<K, V>(key, null), comparator);
            if (binarySearch >= 0 && binarySearch < list.size()) {
                this.add(binarySearch, key, value);
            } else if (binarySearch < 0) {
                this.add(-binarySearch + 1, key, value);
            }
        } else
            list.add(new Tuple2(key, value));
    }

    public static <K, V> Map.Entry<K, V> createEntry(K key, V value) {
        return new KV(key, value);
    }

    public Map.Entry<K, V> get(int i) {
        return list.get(i);
    }

    public V getValue(int i) {
        return list.get(i).getValue();
    }

    public K getKey(int i) {
        return list.get(i).getKey();
    }

    public Map.Entry<K, V> remove(int i) {
        return list.remove(i);
    }

    public void removeValues(V value) {
        Iterator<? extends Map.Entry<K, V>> iter = iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> next = iter.next();
            if (next.getValue().equals(value)) {
                iter.remove();
            }
        }
    }

    public void add(int i, K key, V value) {
        list.add(i, new KV(key, value));
        isSorted = false;
    }

    public void add(int i, Map.Entry<K, V> entry) {
        if (entry instanceof KV)
            list.add((KV)entry);
        else
            list.add(i, new KV(entry.getKey(), entry.getValue()));
        isSorted = false;
    }

    public V getValue(K key) {
        if (isSorted) {
            int binarySearch = Collections.binarySearch(list, new KV<K, V>(key, null), comparator);
            if (binarySearch >= 0 && binarySearch < list.size()) {
                Map.Entry<K, V> entry = list.get(binarySearch);
                if (entry.getKey().equals(key)) {
                    return entry.getValue();
                }
            }
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<K, V> entry = list.get(i);
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean remove(Object key) {
        if (isSorted) {
            int binarySearch = Collections.binarySearch(list, new KV<K, V>((K) key, null), comparator);
            if (binarySearch >= 0 && binarySearch < list.size()) {
                Map.Entry<K, V> entry = list.get(binarySearch);
                if (entry.getKey().equals(key)) {
                    list.remove(binarySearch);
                    return true;
                }
            }
            return false;
        }
        Iterator<Map.Entry<K, V>> iter = list.iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            if (entry.getKey().equals(key)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    public ArrayMap<K, V> ascending() {
        comparator = new AscComparator();
        Collections.sort(list, comparator);
        isSorted = true;
        return this;
    }

    public ArrayMap<K, V> descending() {
        comparator = new DescComparator();
        Collections.sort(list, comparator);
        isSorted = true;
        return this;
    }

    public ArrayMap<K, V> sorted(Comparator<Map.Entry<K, V>> comparator) {
        this.comparator = comparator;
        Collections.sort(list, comparator);
        isSorted = true;
        return this;
    }

    @Override
    public java.util.Iterator<Map.Entry<K, V>> iterator() {
        return list.iterator();
    }

    public MixedIterator<K> keys() {
        return new MapKeyIterator<K, V>(iterator());
    }

    public MixedIterator<V> values() {
        return new MapValueIterator<K, V>(iterator());
    }

    public PeekIterator<K> peekKeys() {
        return new PeekKeyIterator<K, V>(iterator());
    }

    public PeekIterator<V> peekValues() {
        return new PeekValueIterator<K, V>(iterator());
    }

    public Iterator<Map.Entry<K, V>> iteratorAsc() {
        return ascending().iterator();
    }

    public Iterator<Map.Entry<K, V>> iteratorDesc() {
        return descending().iterator();
    }

    public Iterator<Map.Entry<K, V>> iterator(Comparator<Map.Entry<K, V>> comparator) {
        return sorted(comparator).iterator();
    }

    @Override
    public Map.Entry<K, V>[] toArray() {
        return (Map.Entry<K, V>[]) list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public boolean add(Map.Entry<K, V> e) {
        return list.add(e);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public ArrayMap<K, V> addAllInverse(Collection<? extends Map.Entry<V, K>> c) {
        for (Map.Entry<V, K> entry : c) {
            list.add(new KV(entry.getValue(), entry.getKey()));
        }
        isSorted = false;
        return this;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
        isSorted = false;
        return list.addAll(c);
    }

    private class AscComparator implements Comparator<Map.Entry<K, V>> {

        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return ((Comparable) o1.getKey()).compareTo(o2.getKey());
        }
    }

    protected class DescComparator implements Comparator<Map.Entry<K, V>> {

        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return ((Comparable) o2.getKey()).compareTo(o1.getKey());
        }
    }

    public void shuffle() {
        isSorted = false;
        for (int i = 0; i < size() - 1; i++) {
            int winner = i + RandomTools.getInt(size() - i);
            if (winner != i) {
                list.add(i, list.remove(winner));
            }
        }
    }

    @Override
    public String toString() {
        return MapTools.toString(this);
    }

    public static void main(String[] args) {
        ArrayMap<Integer, Integer> map = new ArrayMap();
        for (int i = 0; i < 100; i++) {
            map.add(RandomTools.getInt(100), 1);
        }
        for (Map.Entry<Integer, Integer> entry : map) {
            log.printf("%d %d", entry.getKey(), entry.getValue());
        }
    }
}
