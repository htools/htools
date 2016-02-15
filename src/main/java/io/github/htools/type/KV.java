package io.github.htools.type;

import io.github.htools.lib.MathTools;

import java.util.Map;

public class KV<K, V> implements Map.Entry<K, V>, Comparable<KV<K, V>> {

    public K key;
    public V value;

    public KV(K r, V s) {
        key = r;
        value = s;
    }

    @Override
    public int hashCode() {
        return MathTools.hashCode(key.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof KV && ((KV) o).key.equals(key));
    }

    @Override
    public String toString() {
        return new StringBuilder().append(key.toString()).append("=").append(value.toString()).toString();
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

    public K setKey(K key) {
        K old = this.key;
        this.key = key;
        return old;
    }

    @Override
    public int compareTo(KV o) {
        Comparable a = (Comparable) key;
        return a.compareTo(o.key);
    }  
}
