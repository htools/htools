package io.github.repir.tools.type;

import io.github.repir.tools.collection.HashMapDouble;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author jeroen
 */
public class TermVectorDouble extends HashMapDouble<String> {

    Double total = null;
    Double magnitude = null;

    public TermVectorDouble() {
    }

    public TermVectorDouble(HashMapDouble<String> map) {
        super(map);
    }

    public TermVectorDouble(Collection<String> terms) {
        add(terms);
    }

    public void add(Collection<String> terms) {
        for (String t : terms) {
            add(t, 1);
        }
        magnitude = null;
    }

    public void add(TermVectorDouble v) {
        for (Map.Entry<String, Double> entry : v.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        magnitude = null;
    }

    public void add(TermVectorInt v) {
        for (Map.Entry<String, Integer> entry : v.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        magnitude = null;
    }

    public void remove(TermVectorDouble v) {
        for (Map.Entry<String, Double> entry : v.entrySet()) {
            add(entry.getKey(), -entry.getValue());
        }
        magnitude = null;
    }

    public double magnitude() {
        if (magnitude == null) {
            total = null;
            double sum = 0;
            for (Double freq : values()) {
                sum += freq * freq;
            }
            magnitude = Math.sqrt(sum);
        }
        return magnitude;
    }

    public double cossim(TermVectorDouble v) {
        double dotproduct = 0;
        for (Map.Entry<String, Double> entry : entrySet()) {
            Double freq = v.get(entry.getKey());
            if (freq != null) {
                dotproduct += freq * entry.getValue();
            }
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public double cossim(TermVectorInt v) {
        double dotproduct = 0;
        for (Map.Entry<String, Double> entry : entrySet()) {
            Integer freq = v.get(entry.getKey());
            if (freq != null) {
                dotproduct += freq * entry.getValue();
            }
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public TermVectorDouble multiply(TermVectorDouble v) {
        TermVectorDouble result = new TermVectorDouble();
        for (Map.Entry<String, Double> entry : entrySet()) {
            Double d = v.get(entry.getKey());
            if (d != null)
                result.put(entry.getKey(), entry.getValue() * d);
        }
        return result;
    }

    public TermVectorDouble multiply(TermVectorInt v) {
        TermVectorDouble result = new TermVectorDouble();
        for (Map.Entry<String, Double> entry : entrySet()) {
            Integer d = v.get(entry.getKey());
            if (d != null)
                result.put(entry.getKey(), entry.getValue() * d);
        }
        return result;
    }

    public TermVectorDouble divide(double div) {
        return (TermVectorDouble)super.divide(new TermVectorDouble(), div);
    }

    public String getMax() {
        double max = Double.MIN_VALUE;
        String maxterm = null;
        for (Map.Entry<String, Double> entry : entrySet()) {
            if (max < entry.getValue()) {
                max = entry.getValue();
                maxterm = entry.getKey();
            }
        }
        return maxterm;
    }

    public TermVectorDouble getTop(int k) {
        return super.getTop(new TermVectorDouble(), k);
    }

    public Double total() {
        if (magnitude == null) {
            magnitude();
            total = 0.0;
            for (Double i : values()) {
                total += i;
            }
        }
        return total;
    }

    public TermVectorDouble normalize() {
        double total = total();
        if (total != 1) {
            for (Map.Entry<String, Double> entry : entrySet()) {
                entry.setValue(entry.getValue() / total);
            }
        }
        magnitude = null;
        total = 1;
        return this;
    }
}
