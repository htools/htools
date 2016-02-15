package io.github.htools.fcollection;

import io.github.htools.collection.InvertedMap;
import io.github.htools.collection.TopKMap;
import io.github.htools.lib.CollectionTools;
import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Collection;
import java.util.Map;

/**
 * A HashMap with a Integer value, that supports adding values to existing keys
 *
 * @author jeroen
 */
public class FHashMapIntDouble extends Int2DoubleOpenHashMap implements Map<Integer, Double> {

    public static final Log log = new Log(FHashMapIntDouble.class);

    public FHashMapIntDouble() {
        super();
    }

    public FHashMapIntDouble(int size) {
        super(size);
    }

    public FHashMapIntDouble(Map<Integer, Double> map) {
        super(map);
    }

    public FHashMapIntDouble(Int2DoubleOpenHashMap map) {
        super(map);
    }

    @Override
    public FHashMapIntDouble clone() {
        FHashMapIntDouble clone = new FHashMapIntDouble(size());
        for (Int2DoubleMap.Entry entry : this.int2DoubleEntrySet()) {
            put(entry.getIntKey(), entry.getDoubleValue());
        }
        return clone;
    }

    public Map<Integer, Integer> invert() {
        return new InvertedMap(this);
    }

    protected FHashMapIntDouble create() {
        return new FHashMapIntDouble();
    }

    public void add(int key, double value) {
        double newvalue = value + this.get(key);
        if (newvalue != 0) {
            put(key, newvalue);
        } else {
            super.remove(key);
        }
    }

    public FHashMapIntDouble add(FHashMapIntDouble map) {
        for (Int2DoubleMap.Entry entry : map.int2DoubleEntrySet()) {
            add(entry.getIntKey(), entry.getDoubleValue());
        }
        return this;
    }

    public void add(int key) {
        add(key, 1);
    }

    public void addAll(Collection<Integer> keys) {
        for (Integer key : keys) {
            add(key, 1);
        }
    }

    public void addAll(IntArrayList keys) {
        for (int key : keys) {
            add(key, 1);
        }
    }

    /**
     * creates a new Map containing the tallies of this map minus the tallies in
     * map v.
     *
     * @param value
     */
    public FHashMapIntDouble subtract(FHashMapIntDouble value) {
        FHashMapIntDouble result = create();
        for (Int2DoubleMap.Entry entry : int2DoubleEntrySet()) {
            double v = entry.getDoubleValue() - value.get(entry.getIntKey());
            if (v > 0) {
                result.add(entry.getIntKey(), v);
            }
        }
        return result;
    }

    /**
     * subtracts the values from given keys, will on work when the keys with
     * given values exists.
     *
     * @param v
     */
    public void remove(FHashMapIntDouble v) {
        for (Int2DoubleMap.Entry entry : v.int2DoubleEntrySet()) {
            add(entry.getIntKey(), -entry.getDoubleValue());
        }
    }

    public double max() {
        double maxvalue = Double.MIN_VALUE;
        double max = Double.MIN_VALUE;
        for (Int2DoubleMap.Entry entry : int2DoubleEntrySet()) {
            if (entry.getDoubleValue() > maxvalue) {
                maxvalue = entry.getDoubleValue();
                max = entry.getIntKey();
            }
        }
        return max;
    }

    public double maxValue() {
        double maxvalue = Double.MIN_VALUE;
        for (double i : values()) {
            if (i > maxvalue) {
                maxvalue = i;
            }
        }
        return maxvalue;
    }

    public double sumValue() {
        double sum = 0;
        for (double i : values()) {
            sum += i;
        }
        return sum;
    }

    public double min() {
        double minvalue = Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (Int2DoubleMap.Entry entry : int2DoubleEntrySet()) {
            if (entry.getDoubleValue() < minvalue) {
                minvalue = entry.getDoubleValue();
                min = entry.getIntKey();
            }
        }
        return min;
    }

    public void addIfExists(int key, double value) {
        if (containsKey(key)) {
            put(key, get(key) + value);
        }
    }

    public <S extends FHashMapIntDouble> S getTop(S result, int k) {
        TopKMap<Double, Integer> topk = new TopKMap(k);
        CollectionTools.invert(this.entrySet(), topk);
        CollectionTools.invert(topk, result);
        return result;
    }

    public FHashMapIntDouble getTop(int k) {
        return getTop(new FHashMapIntDouble(), k);
    }

    public void cutBelow(double k) {
        DoubleIterator iter = values().iterator();
        while (iter.hasNext()) {
            if (iter.nextDouble() < k) {
                iter.remove();
            }
        }
    }

    public void cutAbove(double k) {
        DoubleIterator iter = values().iterator();
        while (iter.hasNext()) {
            if (iter.nextDouble() > k) {
                iter.remove();
            }
        }
    }
}
