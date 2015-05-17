package io.github.repir.tools.type;

import io.github.repir.tools.collection.HashMapInt;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author jeroen
 */
public class TermVectorInt extends HashMapInt<String> {

    protected Integer total = null;
    protected Double magnitude = null;

    public TermVectorInt() {
    }

    public TermVectorInt(Collection<String> terms) {
        add(terms);
    }

    @Override
    public TermVectorInt clone() {
        return (TermVectorInt)super.clone();
    }
    
    public TermVectorInt toBinary() {
        TermVectorInt clone = (TermVectorInt)super.clone();
        for (Map.Entry<String, Integer> entry : clone.entrySet())
            entry.setValue(entry.getValue() >= 1?1:0);
        return clone;
    }
    
    public void add(Collection<String> terms) {
        for (String t : terms) {
            super.add(t, 1);
        }
        magnitude = null;
        total = null;
    }

    public TermVectorInt add(HashMapInt<String> v) {
        for (Map.Entry<String, Integer> entry : v.entrySet()) {
            super.add(entry.getKey(), entry.getValue());
        }
        magnitude = null;
        total = null;
        return this;
    }

    public TermVectorInt remove(HashMapInt<String> v) {
        for (Map.Entry<String, Integer> entry : v.entrySet()) {
            add(entry.getKey(), -entry.getValue());
        }
        magnitude = null;
        total = null;
        return this;
    }

    public double magnitude() {
        if (magnitude == null) {
            int sum = 0;
            for (Integer freq : values()) {
                sum += freq * freq;
            }
            magnitude = Math.sqrt(sum);
        }
        return magnitude;
    }

    public double cossim(TermVectorInt v) {
        double dotproduct = 0;
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Integer freq = v.get(entry.getKey());
            if (freq != null) {
                dotproduct += freq * entry.getValue();
            }
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public static double dice(TermVectorInt ... v) {
        HashSet<String> intersect = new HashSet(v[0].keySet());
        int count = v[0].size();
        LOOP:
        for (int i = 1; i < v.length; i++) {
            intersect.retainAll(v[i].keySet());
            count += v[i].size();
        }
        return v.length * intersect.size() / (double) count;
    }

    public double dice(TermVectorInt v) {
        HashSet<String> intersect = new HashSet(v.keySet());
        return intersect.size() / (double) (size() + v.size());
    }

    public double cossim(TermVectorDouble v) {
        double dotproduct = 0;
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Double freq = v.get(entry.getKey());
            if (freq != null) {
                dotproduct += freq * entry.getValue();
            }
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public TermVectorDouble multiply(TermVectorDouble v) {
        TermVectorDouble result = new TermVectorDouble();
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Double d = v.get(entry.getKey());
            if (d != null)
                result.put(entry.getKey(), entry.getValue() * d);
        }
        return result;
    }

    public TermVectorInt multiply(TermVectorInt v) {
        TermVectorInt result = new TermVectorInt();
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Integer d = v.get(entry.getKey());
            if (d != null)
                result.put(entry.getKey(), entry.getValue() * d);
        }
        return result;
    }

    public Integer total() {
       if (total == null) {
           total = 0;
           for (Integer i : values())
               total += i;
       }
       return total;
    }
    
    public TermVectorDouble normalize() {
        TermVectorDouble result = new TermVectorDouble();
        double total = total();
        for (Map.Entry<String, Integer> entry : entrySet()) {
            result.put(entry.getKey(), entry.getValue() / total);
        }
        return result;
    }
}
