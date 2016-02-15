package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.lib.MapTools;
import io.github.htools.lib.RandomTools;
import io.github.htools.type.KV;
import io.github.htools.type.Tuple2;

import java.util.*;

/**
 * An ArrayMap is similar to a Map in that it stores Map.Entry&lt;K,V&gt;,
 * however it is no a true Map in that it can contain duplicate Keys, and does
 * not implement the Map interface because it should not be used like a Map for
 * fast access of Keys. What it does do is provide a very fast mechanism to add
 * items, sort one time and provide the sorted list as a
 * Collection&lt;Map.Entry&lt;K,V&gt;&gt;. This allows fast and easy iteration
 * over the sorted list, however the order is disrupted when the list is
 * changed. However, returning as a Collection does allow easy transformation of
 * it's contents into a Collection of any other type.
 * <p>
 * Sorting and iteration are all shallow on the collection itself. To create a
 * sorted copy the collection must be cloned.
 * <p>
 * @author jeroen
 */
public class ArrayMap<K, V> implements Iterable<Map.Entry<K, V>>, Map<K, V> {

    public static Log log = new Log(ArrayMap.class);
    ArrayList<Map.Entry<K, V>> list;
    private Comparator<Map.Entry<K, V>> comparator = null;

    public ArrayMap() {
        list = new ArrayList();
    }

    public ArrayMap(Collection<? extends Map.Entry<K, V>> c) {
        list = new ArrayList(c);
    }

    public ArrayMap(Map<K, V> c) {
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

    public Map<V, K> invert() {
        return new InvertedMap(this);
    }
    
    public int indexOfValue(V value) {
        for (int i = 0; i < size(); i++) {
            if (ArrayMap.this.get(i).getValue().equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfKey(K key) {
        for (int i = 0; i < size(); i++) {
            if (ArrayMap.this.get(i).getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return a shallow copy, i.e. the elements are not cloned.
     */
    @Override
    public ArrayMap<K, V> clone() {
        ArrayMap<K, V> n = new ArrayMap();
        n.list = (ArrayList<Map.Entry<K, V>>) list.clone();
        return n;
    }

    public ArrayMap<V, K> cloneInverted() {
        ArrayMap<V, K> n = new ArrayMap(list.size());
        for (Map.Entry<K, V> entry : list)
            n.add(entry.getValue(), entry.getKey());
        return n;
    }

    public void add(K key, V value) {
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
    }

    public void add(int i, Map.Entry<K, V> entry) {
        if (entry instanceof KV) {
            list.add((KV) entry);
        } else {
            list.add(i, new KV(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * @param key
     * @return The first value for the given key or null if no such key exists,
     * note that an ArrayMap can contain non-unique keys
     */
    @Override
    public V get(Object key) {
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<K, V> entry = list.get(i);
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * @param key
     * @return removes all entries for the given key and returns the value for the
     * last entry or null if no entry with the given key was found
     */
    @Override
    public V remove(Object key) {
        V removedValue = null;
        Iterator<Map.Entry<K, V>> iter = list.iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            if (entry.getKey().equals(key)) {
                iter.remove();
                removedValue = entry.getValue();
            }
        }
        return null;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public ArrayMap<K, V> ascending() {
        comparator = new AscComparator();
        Collections.sort(list, comparator);
        return this;
    }

    public ArrayMap<K, V> descending() {
        comparator = new DescComparator();
        Collections.sort(list, comparator);
        return this;
    }

    public ArrayMap<K, V> sorted(Comparator<Map.Entry<K, V>> comparator) {
        this.comparator = comparator;
        Collections.sort(list, comparator);
        return this;
    }

    @Override
    public java.util.Iterator<Map.Entry<K, V>> iterator() {
        return list.iterator();
    }

    public MixedIterator<K> keys() {
        return new MapKeyIterator<K, V>(iterator());
    }

    @Override
    public Collection<V> values() {
        return new Values();
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

    public Map.Entry<K, V>[] toArray() {
        return (Map.Entry<K, V>[]) list.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public boolean add(Map.Entry<K, V> e) {
        return list.add(e);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public ArrayMap<K, V> addAllInverse(Collection<? extends Map.Entry<V, K>> c) {
        for (Map.Entry<V, K> entry : c) {
            list.add(new KV(entry.getValue(), entry.getKey()));
        }
        return this;
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
        return list.addAll(c);
    }

    @Override
    public boolean containsKey(Object key) {
       for (int i = size() -1; i >= 0; i++) {
           if (key.equals(get(i).getKey())) {
               return true;
           }
       }
       return false;
    }

    public boolean containsAllKeys(Collection<Object> key) {
       for (int i = size() -1; i >= 0; i++) {
           if (key.equals(get(i).getKey())) {
               return true;
           }
       }
       return false;
    }

    @Override
    public boolean containsValue(Object value) {
       for (int i = size() -1; i >= 0; i++) {
           if (value.equals(get(i).getKey())) {
               return true;
           }
       }
       return false;
    }

    @Override
    public V put(K key, V value) {
       add(key, value);
       return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            list.add(new Tuple2<K, V>(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
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

    private class KeySet extends AbstractSet<K> {
        @Override
        public int size() {
            HashMap a;
           return ArrayMap.this.size();
        }

        @Override
        public boolean isEmpty() {
           return ArrayMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return ArrayMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return ArrayMap.this.keys();
        }
    }
    
    private class Values extends AbstractCollection<V> {
        public int size() { return ArrayMap.this.size(); }
        public boolean isEmpty() { return ArrayMap.this.isEmpty(); }
        public Iterator<V> iterator() {  return new MapValueIterator<K, V>(ArrayMap.this.iterator()); }

        @Override
        public boolean contains(Object o) {
            for (int i = 0; i < size(); i++) {
                if (o.equals(get(i).getValue())) {
                    return true;
                }
            }
            return false;
        }
    }
    
    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return ArrayMap.this.size(); }
        public final void clear()               { ArrayMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() { return ArrayMap.this.iterator(); }
        public final boolean contains(Object o) { return ArrayMap.this.contains(o); }
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
