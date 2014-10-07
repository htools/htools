package io.github.repir.tools.Collection;

import io.github.repir.tools.Collection.ArrayMap.Entry;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MapTools;
import io.github.repir.tools.Lib.RandomTools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An ArrayMap is similar to a Map in that it stores Map.Entry<K,V>, however it is
 * no a true Map in that it can contain duplicate Keys, and does not implement
 * the Map interface because it should not be used like a Map for fast access of
 * Keys. What it does do is provide a very fast mechanism to add items, sort one time
 * and provide the sorted list as a Collection<Map.Entry<K,V>>. This allows fast 
 * and easy iteration over the sorted list, however the order is disrupted when the
 * list is changed. However, returning as a Collection does allow easy transformation
 * of it's contents into a Collection of any other type.
 * <p/>
 * Sorting and iteration are all shallow on the collection itself. To create a sorted
 * copy the collection must be cloned.
 * <p/>
 * @author jeroen
 */
public class ArrayMap<K extends Comparable,V> implements Iterable<Map.Entry<K,V>>, Collection<Map.Entry<K,V>> {

   public static Log log = new Log(ArrayMap.class);
    ArrayList<Map.Entry<K,V>> list;
   
   public ArrayMap() {
      list = new ArrayList();
   }
   
   public ArrayMap(Collection<? extends Map.Entry<K, V>> c) {
      list = new ArrayList(c);
   }
   
   public ArrayMap(int initialsize) {
      list = new ArrayList(initialsize);
   }
   
   /**
    * @return a shallow copy, i.e. the elements are not cloned.
    */
   @Override
   public ArrayMap<K,V> clone() {
       ArrayMap<K,V> n = new ArrayMap();
       n.list = (ArrayList<Map.Entry<K,V>>)list.clone();
       return n;
   }
   
   public void add(K key, V value) {
       list.add(new Entry(key, value));
   }
   
    public V getValue(Object key) {
        Iterator<Map.Entry<K, V>> iter = list.iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

   @Override
    public boolean remove(Object key) {
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

    public ArrayMap<K,V> ascending() {
        Collections.sort(list, new AscComparator());
        return this;
    }

    public ArrayMap<K,V> descending() {
        Collections.sort(list, new DescComparator());
        return this;
    }

    public ArrayMap<K,V> sorted(Comparator<Map.Entry<K,V>> comparator) {
        Collections.sort(list, comparator);
        return this;
    }

    @Override
    public Iterator<Map.Entry<K,V>> iterator() {
        return list.iterator();
    }

    public Iterator<Map.Entry<K,V>> iteratorAsc() {
        Collections.sort(list, new AscComparator());
        return list.iterator();
    }

    public Iterator<Map.Entry<K,V>> iteratorDesc() {
        Collections.sort(list, new DescComparator());
        return list.iterator();
    }

    public Iterator<Map.Entry<K,V>> iterator(Comparator<Map.Entry<K,V>> comparator) {
        Collections.sort(list, comparator);
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    public Object[] toArrayAsc() {
        Collections.sort(list, new AscComparator());
        return list.toArray();
    }

    public Object[] toArrayDesc() {
        Collections.sort(list, new DescComparator());
        return list.toArray();
    }

    public Object[] toArray(Comparator<Map.Entry<K,V>> comparator) {
        Collections.sort(list, comparator);
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public <T> T[] toArrayAsc(T[] a) {
        Collections.sort(list, new AscComparator());
        return list.toArray(a);
    }

    public <T> T[] toArrayDesc(T[] a) {
        Collections.sort(list, new DescComparator());
        return list.toArray(a);
    }

    public <T> T[] toArray(T[] a, Comparator<Map.Entry<K,V>> comparator) {
        Collections.sort(list, comparator);
        return list.toArray(a);
    }

    @Override
    public boolean add(Map.Entry<K, V> e) {
        return list.add(e);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
        return list.addAll(c);
    }

    public ArrayMap<K,V> addAllInverse(Collection<? extends Map.Entry<V, K>> c) {
        for (Map.Entry<V, K> entry : c)
            list.add(new Entry(entry.getValue(), entry.getKey()));
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
   
    private class AscComparator implements Comparator<Map.Entry<K,V>> {
        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return o1.getKey().compareTo(o2.getKey());
        } 
    }
    
    protected class DescComparator implements Comparator<Map.Entry<K,V>> {
        @Override
        public int compare(Map.Entry<K,V> o1, Map.Entry<K,V> o2) {
            return o2.getKey().compareTo(o1.getKey());
        }
    }
    
   @Override
    public String toString() {
        return MapTools.toString(this);
    }
    
   public static class Entry<K, V> implements Comparable<Entry<K,V>>, Map.Entry<K,V> { 
       final K key;
       V value;
       
       public Entry(K key, V value) {
           this.key = key;
           this.value = value;
       }

       public K getKey() {
           return key;
       }
       
       public V getValue() {
           return value;
       }
       
       @Override
       public String toString() {
           return new StringBuilder().append(key.toString()).append("=").append(value.toString()).toString();
       }
       
       @Override
       public boolean equals(Object o) {
           if (o instanceof Entry) {
               Entry e = (Entry)o;
               return e.key.equals(key) && e.value.equals(value);
           }
           return false;
       }
       
       @Override
       public int hashCode() {
           return key.hashCode();
       }
       
        @Override
        public int compareTo(Entry o) {
            Comparable a = (Comparable)key;
            return a.compareTo(o.key);
        }     

        public V setValue(V value) {
            V oldvalue = this.value;
            this.value = value;
            return oldvalue;
        }
   }
   
    public static void main(String[] args) {
        ArrayMap<Integer, Integer> map = new ArrayMap();
        for (int i = 0; i < 100; i++)
            map.add(RandomTools.getInt(100), 1);
        for (Map.Entry<Integer, Integer> entry : map)
            log.printf("%d %d", entry.getKey(), entry.getValue());
    }
}
