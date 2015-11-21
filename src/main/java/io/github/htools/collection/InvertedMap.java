package io.github.htools.collection;

import io.github.htools.type.KV;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jeroen
 */
public class InvertedMap<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>> {
    Map<V, K> map;

    public InvertedMap(Map<V, K> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsValue(key);
    }

    @Override
    public boolean containsValue(Object value) {
       return map.containsKey(value);
    }

    @Override
    public V get(Object key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
       for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
           map.put(entry.getValue(), entry.getKey());
       }
    }

    @Override
    public void clear() {
        map.clear();;
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
       return new Values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new EntryIterator();
    }
    
    private class EntryIterator implements Iterator<Entry<K, V>> {
        Iterator<Map.Entry<V, K>> iter = map.entrySet().iterator();
        public boolean hasNext() { return iter.hasNext(); }
        public void remove() {  iter.remove(); }

        @Override
        public Entry<K, V> next() {
            Entry<V, K> next = iter.next();
            return new KV<K, V>(next.getValue(), next.getKey());
        } 
    }
    
    private class KeySet extends AbstractSet<K> {
        public int size() { return map.size(); }
        public boolean isEmpty() {  return InvertedMap.this.isEmpty(); }
        public boolean contains(Object o) { return map.containsValue(o); }
        public Iterator<K> iterator() { return map.values().iterator(); }
    }
    
    private class Values extends AbstractCollection<V> {
        public int size() { return map.size(); }
        public boolean isEmpty() { return map.isEmpty(); }
        public Iterator<V> iterator() {  return map.keySet().iterator(); }

        @Override
        public boolean contains(Object o) {
            return map.keySet().contains(o);
        }
    }
    
    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return map.size(); }
        public final void clear()               { map.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() { return new EntryIterator(); }
        public final boolean contains(Object o) { return map.containsKey(o); }
    }        
}
