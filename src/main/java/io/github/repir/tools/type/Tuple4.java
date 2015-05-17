package io.github.repir.tools.type;

import io.github.repir.tools.lib.MathTools;

public class Tuple4<K, V, V2, V3> extends KV4<K, V, V2, V3> {

   public Tuple4(K key, V value, V2 value2, V3 value3) {
       super(key, value, value2, value3);
   }

    @Override
    public int hashCode() {
        return MathTools.hashCode(key.hashCode(), value.hashCode(), value2.hashCode(), value3.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Tuple4) {
            Tuple4<K, V, V2, V3> oo = (Tuple4)o;
            return oo.key.equals(key) && oo.value.equals(value) && oo.value2.equals(value2) && oo.value3.equals(value3);
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(key.toString())
                .append(",").append(value.toString())
                .append(",").append(value2.toString())
                .append(",").append(value3.toString()).toString();
    }
}
