package io.github.htools.collection;

import io.github.htools.collection.ArrayMapDouble.Entry;
import io.github.htools.lib.Log;
import io.github.htools.lib.MapTools;
import io.github.htools.lib.RandomTools;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

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
public class ArrayMapDouble<V> implements Iterable<Double2ObjectMap.Entry<V>> {

    public static Log log = new Log(ArrayMapDouble.class);
    DoubleArrayList keys;
    ArrayList<V> list;
    Entry<V> entry = new Entry();

    public ArrayMapDouble() {
        list = new ArrayList();
        keys = new DoubleArrayList();
    }

    public ArrayMapDouble(Collection<? extends Map.Entry<Double, V>> c) {
        list = new ArrayList(c.size());
        keys = new DoubleArrayList(c.size());
        for (Map.Entry<Double, V> entry : c) {
            list.add(entry.getValue());
            keys.add(entry.getKey());
        }
    }

    public ArrayMapDouble(AbstractMap<Double, V> c) {
        this(c.entrySet());
    }

    public ArrayMapDouble(int initialsize) {
        list = new ArrayList(initialsize);
        keys = new DoubleArrayList(initialsize);
    }

//    public static <K, V> ArrayMapDouble<K, V> invert(Iterable<? extends Map.Entry<V, K>> c) {
//        ArrayMapDouble<K, V> map = new ArrayMapDouble();
//        for (Map.Entry<V, K> entry : c) {
//            map.add(entry.getValue(), entry.getKey());
//        }
//        return map;
//    }
    public int indexOfValue(V value) {
        for (int i = 0; i < size(); i++) {
            if (get(i).getValue().equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfKey(double key) {
        for (int i = 0; i < size(); i++) {
            if (getKey(i) == key) {
                return i;
            }
        }
        return -1;
    }

//    public static <K, V> ArrayMapDouble<K, V> invert(AbstractMap<V, K> c) {
//        return invert(c.entrySet());
//    }
//    /**
//     * @return a shallow copy, i.e. the elements are not cloned.
//     */
//    @Override
//    public ArrayMapDouble<K, V> clone() {
//        ArrayMapDouble<K, V> n = new ArrayMapDouble();
//        n.list = (ArrayList<Map.Entry<K, V>>) list.clone();
//        n.isSorted = isSorted;
//        return n;
//    }
    public void add(double key, V value) {
        keys.add(key);
        list.add(value);
    }

    public Entry<V> get(int i) {
        entry.key = getKey(i);
        entry.value = getValue(i);
        return entry;
    }

    public V getValue(int i) {
        return list.get(i);
    }

    public double getKey(int i) {
        return keys.get(i);
    }

    public void add(int i, double key, V value) {
        keys.add(i, key);
        list.add(i, value);
    }

    public boolean remove(int index) {
        if (index < size()) {
            keys.remove(index);
            list.remove(index);
            return true;
        }
        return false;
    }

    public int size() {
        return keys.size();
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public boolean contains(V o) {
        return list.contains(o);
    }

    public boolean contains(double o) {
        return keys.contains(o);
    }

    public DoubleListIterator keys() {
        return keys.iterator();
    }

    public Iterator<V> values() {
        return list.iterator();
    }

    public void clear() {
        list.clear();
        keys.clear();
    }

    public Iterator<Double2ObjectMap.Entry<V>> iterator() {
        return new EntryIterator();
    }

    class EntryIterator<V> implements Iterator<Entry<V>> {
        int pos = size();
        Entry entry = new Entry();

        @Override
        public boolean hasNext() {
            return pos > 0;
        }

        @Override
        public Entry next() {
            entry.key = getKey(--pos);
            entry.value = getValue(pos);
            return entry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    public class Entry<V> implements Double2ObjectMap.Entry<V> {

        public double key;
        public V value;

        @Override
        public double getDoubleKey() {
            return key;
        }

        @Override
        public Double getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldvalue = this.value;
            this.value = value;
            return oldvalue;
        }
    }
}
