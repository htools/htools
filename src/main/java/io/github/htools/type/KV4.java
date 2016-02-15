package io.github.htools.type;

import io.github.htools.lib.MathTools;

import java.util.Map;

public class KV4<K, V, V2, V3> implements Map.Entry<K, Tuple3<V, V2, V3>>, Comparable<KV4<K, V, V2, V3>> {

    public K key;
    public V value;
    public V2 value2;
    public V3 value3;

    public KV4(K key, V value, V2 value2, V3 value3) {
        this.key = key;
        this.value = value;
        this.value2 = value2;
        this.value3 = value3;
    }

    @Override
    public int hashCode() {
        return MathTools.hashCode(key.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof KV4 && ((KV4) o).key.equals(key));
    }

    @Override
    public String toString() {
        return new StringBuilder().append(key.toString())
                  .append("=").append(value.toString())
                  .append(",").append(value2.toString())
                  .append(",").append(value3.toString())
                  .toString();
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public Tuple3<V, V2, V3> getValue() {
        return new Tuple3(value, value2, value3);
    }

    @Override
    public Tuple3<V, V2, V3> setValue(Tuple3<V, V2, V3> value) {
        Tuple3<V, V2, V3> old = getValue();
        this.value = value.key;
        this.value2 = value.value;
        this.value3 = value.value2;
        return old;
    }

    public void setValue(V value, V2 value2, V3 value3) {
        this.value = value;
        this.value2 = value2;
        this.value3 = value3;
    }

    public K setKey(K key) {
        K old = this.key;
        this.key = key;
        return old;
    }

    @Override
    public int compareTo(KV4 o) {
        Comparable a = (Comparable) key;
        return a.compareTo(o.key);
    }  
}
