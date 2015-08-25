package io.github.htools.type;

import io.github.htools.lib.MathTools;

public class Tuple3<K, V, V2> extends KV3<K, V, V2> {

   public Tuple3(K key, V value, V2 value2) {
       super(key, value, value2);
   }

    @Override
    public int hashCode() {
        return MathTools.hashCode(key.hashCode(), value.hashCode(), value2.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof KV3) {
            Tuple3<K, V, V2> oo = (Tuple3)o;
            return oo.key.equals(key) && oo.value.equals(value) && oo.value2.equals(value2);
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(key.toString())
                .append(",").append(value.toString())
                .append(",").append(value2.toString()).toString();
    }
}
