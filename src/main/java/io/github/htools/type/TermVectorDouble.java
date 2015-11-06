package io.github.htools.type;

import io.github.htools.collection.HashMapDouble;
import io.github.htools.fcollection.FHashMapObjectDouble;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author jeroen
 */
public class TermVectorDouble extends FHashMapObjectDouble<String> implements TermVector {

    Double total = null;
    Double magnitude = null;

    public TermVectorDouble() {
    }

    public TermVectorDouble(int size) {
        super(size);
    }

    public TermVectorDouble(int size, float loadfactor) {
        super(size, loadfactor);
    }

    public TermVectorDouble(Map<String, Double> map) {
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
        for (Object2DoubleMap.Entry<String> entry : v.object2DoubleEntrySet()) {
            add(entry.getKey(), entry.getDoubleValue());
        }
        magnitude = null;
    }

    public void add(TermVectorInt v) {
        for (Object2IntMap.Entry<String> entry : v.object2IntEntrySet()) {
            add(entry.getKey(), entry.getIntValue());
        }
        magnitude = null;
    }

    public void remove(TermVectorDouble v) {
        for (Object2DoubleMap.Entry<String> entry : v.object2DoubleEntrySet()) {
            add(entry.getKey(), -entry.getDoubleValue());
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
        for (Object2DoubleMap.Entry<String> entry : object2DoubleEntrySet()) {
            dotproduct += v.getDouble(entry.getKey()) * entry.getValue();
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public double cossim(TermVectorInt v) {
        double dotproduct = 0;
        for (Object2DoubleMap.Entry<String> entry : object2DoubleEntrySet()) {
            dotproduct += v.getInt(entry.getKey()) * entry.getValue();
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public TermVectorDouble multiply(Map<String, Double> v) {
        if (size() > v.size() && v instanceof TermVector)
            return ((TermVector)v).multiply(this);
        TermVectorDouble result = new TermVectorDouble();
        for (Object2DoubleMap.Entry<String> entry : object2DoubleEntrySet()) {
            Double d = v.get(entry.getKey());
            if (d != 0) {
                result.put(entry.getKey(), entry.getValue() * d);
            }
        }
        return result;
    }

    public TermVectorDouble multiply(TermVectorInt v) {
        if (size() > v.size())
            return v.multiply(this);
        TermVectorDouble result = new TermVectorDouble();
        for (Object2DoubleMap.Entry<String> entry : object2DoubleEntrySet()) {
            int d = v.getInt(entry.getKey());
            if (d != 0) {
                result.put(entry.getKey(), entry.getValue() * d);
            }
        }
        return result;
    }

    public TermVectorDouble divide(double div) {
        return (TermVectorDouble) super.divide(new TermVectorDouble(), div);
    }

    public String getMax() {
        double max = Double.MIN_VALUE;
        String maxterm = null;
        for (Object2DoubleMap.Entry<String> entry : object2DoubleEntrySet()) {
            if (max < entry.getDoubleValue()) {
                max = entry.getDoubleValue();
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
            for (Object2DoubleMap.Entry<String> entry : object2DoubleEntrySet()) {
                entry.setValue(entry.getDoubleValue() / total);
            }
        }
        magnitude = null;
        total = 1;
        return this;
    }

    @Override
    public double cossim(TermVector v) {
        if (v instanceof TermVectorDouble)
            return cossim((TermVectorDouble)v);
        else if (v instanceof TermVectorInt)
            return cossim((TermVectorInt)v);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
