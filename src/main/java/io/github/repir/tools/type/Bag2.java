package io.github.repir.tools.type;

import io.github.repir.tools.lib.MathTools;

public class Bag2<K extends Comparable<K>> {
    K value1;
    K value2;
    
    public Bag2(K value1, K value2) {
        int comp = value1.compareTo(value2);
        if (comp < 0) {
            this.value1 = value1;
            this.value2 = value2;   
        } else {
            this.value1 = value2;
            this.value2 = value1;
        }
    }

    @Override
    public int hashCode() {
        return MathTools.hashCode(value1.hashCode(), value2.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Bag2 && ((Bag2) o).value1.equals(value1) && ((Bag2) o).value2.equals(value2));
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append(value1.toString()).append(",").append(value2.toString()).toString();
    }    
}
