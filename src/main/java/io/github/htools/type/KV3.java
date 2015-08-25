package io.github.htools.type;

import io.github.htools.lib.MathTools;
import java.util.Map;

public class KV3<K, V, V2> implements Map.Entry<K, Tuple2<V, V2>>, Comparable<KV3<K, V, V2>> {

    public K key;
    public V value;
    public V2 value2;

    public KV3(K key, V value, V2 value2) {
        this.key = key;
        this.value = value;
        this.value2 = value2;
    }

    @Override
    public int hashCode() {
        return MathTools.hashCode(key.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof KV3 && ((KV3) o).key.equals(key));
    }

    @Override
    public String toString() {
        return new StringBuilder().append(key.toString()).append("=").append(value.toString()).append(",").append(value2.toString()).toString();
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public Tuple2<V, V2> getValue() {
        return new Tuple2(value, value2);
    }

    @Override
    public Tuple2<V, V2> setValue(Tuple2<V, V2> value) {
        Tuple2<V, V2> old = getValue();
        this.value = value.key;
        this.value2 = value.value;
        return old;
    }

    public void setValue(V value, V2 value2) {
        this.value = value;
        this.value2 = value2;
    }

    public K setKey(K key) {
        K old = this.key;
        this.key = key;
        return old;
    }

    @Override
    public int compareTo(KV3 o) {
        Comparable a = (Comparable) key;
        return a.compareTo(o.key);
    }  
}
