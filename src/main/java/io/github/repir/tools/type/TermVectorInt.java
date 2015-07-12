package io.github.repir.tools.type;

import io.github.repir.tools.collection.ArrayMap;
import io.github.repir.tools.collection.HashMapInt;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.buffer.BufferSerializable;
import io.github.repir.tools.io.struct.StructureReader;
import io.github.repir.tools.io.struct.StructureWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author jeroen
 */
public class TermVectorInt extends HashMapInt<String> implements BufferSerializable {

    protected Integer total = null;
    protected Double magnitude = null;

    public TermVectorInt() {
    }

    public TermVectorInt(int size) {
        super(size);
    }

    public TermVectorInt(HashMapInt<String> map) {
        super(map);
    }

    public TermVectorInt(Collection<String> terms) {
        add(terms);
    }

    protected TermVectorInt create() {
        return new TermVectorInt();
    }

    @Override
    public TermVectorInt clone() {
        return (TermVectorInt) super.clone();
    }

    public TermVectorInt toBinary() {
        TermVectorInt clone = (TermVectorInt) super.clone();
        for (Map.Entry<String, Integer> entry : clone.entrySet()) {
            entry.setValue(entry.getValue() >= 1 ? 1 : 0);
        }
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

    @Override
    public void remove(HashMapInt<String> v) {
        super.remove(v);
        magnitude = null;
        total = null;
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

    public double magnitudeOmit(TermVectorInt v) {
        double magnitude = 0;
        int sum = 0;
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Integer omitf = v.get(entry.getKey());
            int freq = omitf == null ? entry.getValue() : entry.getValue() - omitf;
            sum += freq * freq;
        }
        magnitude = Math.sqrt(sum);
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

    public double cossimOmit(TermVectorInt v) {
        double dotproduct = 0;
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Integer freq = v.get(entry.getKey());
            if (freq != null) {
                dotproduct += freq * (entry.getValue() - freq);
            }
        }
        double magnitude = magnitudeOmit(v) * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public static double dice(TermVectorInt... v) {
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
            if (d != null) {
                result.put(entry.getKey(), entry.getValue() * d);
            }
        }
        return result;
    }

    public TermVectorDouble divide(double div) {
        return super.divide(new TermVectorDouble(), div);
    }

    public TermVectorInt multiply(TermVectorInt v) {
        TermVectorInt result = new TermVectorInt();
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Integer d = v.get(entry.getKey());
            if (d != null) {
                result.put(entry.getKey(), entry.getValue() * d);
            }
        }
        return result;
    }

    public TermVectorInt getTop(int k) {
        return super.getTop(new TermVectorInt(), k);
    }

    public Integer total() {
        if (total == null) {
            total = 0;
            for (Integer i : values()) {
                total += i;
            }
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

    @Override
    public void read(StructureReader reader) throws EOCException {
        int size = reader.readInt();
        HashMap<String, Integer> map = new HashMap(size);
        for (int i = 0; i < size; i++) {
            map.put(reader.readString(), reader.readInt());
        }
        this.putAll(map);
    }

    @Override
    public void write(StructureWriter writer) {
        writer.write(size());
        for (Map.Entry<String, Integer> entry : entrySet()) {
            writer.write(entry.getKey());
            writer.write(entry.getValue());
        }
    }
}
