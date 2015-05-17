package io.github.repir.tools.type;

import io.github.repir.tools.lib.MathTools;

public class Tuple2<K, V> extends KV<K, V> {

    public Tuple2(K key, V value) {
        super(key, value);
    }

    @Override
    public int hashCode() {
        return MathTools.hashCode(key.hashCode(), value.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Tuple2 && ((Tuple2) o).key.equals(key) && ((Tuple2) o).value.equals(value));
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append(key.toString()).append(",").append(value.toString()).toString();
    }    
}
