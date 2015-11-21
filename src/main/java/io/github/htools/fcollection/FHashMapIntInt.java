package io.github.htools.fcollection;

import io.github.htools.collection.*;
import io.github.htools.lib.CollectionTools;
import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.Map;

/**
 * A HashMap with a Integer value, that supports adding values to existing keys
 *
 * @author jeroen
 */
public class FHashMapIntInt extends Int2IntOpenHashMap implements java.util.Map<Integer, Integer> {

    public static final Log log = new Log(FHashMapIntInt.class);

    public FHashMapIntInt() {
        super();
    }

    public FHashMapIntInt(int size) {
        super(size);
    }

    public FHashMapIntInt(Map<Integer, Integer> map) {
        super(map);
    }

    public FHashMapIntInt(Int2IntOpenHashMap map) {
        super(map);
    }

    @Override
    public FHashMapIntInt clone() {
        FHashMapIntInt clone = new FHashMapIntInt(size());
        for (Int2IntMap.Entry entry : this.int2IntEntrySet()) {
            add(entry.getIntKey(), entry.getIntValue());
        }
        return clone;
    }

    public Map<Integer, Integer> invert() {
        return new InvertedMap(this);
    }
    
    protected FHashMapIntInt create() {
        return new FHashMapIntInt();
    }

    public void add(int key, int value) {
        int newvalue = value + this.get(key);
        if (newvalue != 0) {
            put(key, newvalue);
        } else {
            super.remove(key);
        }
    }

    public FHashMapIntInt add(FHashMapIntInt map) {
        for (Int2IntMap.Entry entry : map.int2IntEntrySet()) {
            add(entry.getIntKey(), entry.getIntValue());
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
    public FHashMapIntInt subtract(FHashMapIntInt value) {
        FHashMapIntInt result = create();
        for (Int2IntMap.Entry entry : int2IntEntrySet()) {
            int v = entry.getIntValue() - value.get(entry.getIntKey());
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
    public void remove(FHashMapIntInt v) {
        for (Int2IntMap.Entry entry : v.int2IntEntrySet()) {
            add(entry.getIntKey(), -entry.getIntValue());
        }
    }

    public int max() {
        int maxvalue = Integer.MIN_VALUE;
        int max = Integer.MIN_VALUE;
        for (Int2IntMap.Entry entry : int2IntEntrySet()) {
            if (entry.getIntValue() > maxvalue) {
                maxvalue = entry.getIntValue();
                max = entry.getIntKey();
            }
        }
        return max;
    }

    public int maxValue() {
        int maxvalue = Integer.MIN_VALUE;
        for (int i : values()) {
            if (i > maxvalue) {
                maxvalue = i;
            }
        }
        return maxvalue;
    }

    public int sumValue() {
        int sum = 0;
        for (int i : values()) {
            sum += i;
        }
        return sum;
    }

    public int min() {
        int minvalue = Integer.MAX_VALUE;
        int min = Integer.MAX_VALUE;
        for (Int2IntMap.Entry entry : int2IntEntrySet()) {
            if (entry.getIntValue() < minvalue) {
                minvalue = entry.getIntValue();
                min = entry.getIntKey();
            }
        }
        return min;
    }

    public void addIfExists(int key, int value) {
        if (containsKey(key)) {
            put(key, get(key) + value);
        }
    }

    public <S extends FHashMapIntInt> S getTop(S result, int k) {
        TopKMap<Integer, Integer> topk = new TopKMap(k);
        CollectionTools.invert(this.entrySet(), topk);
        CollectionTools.invert(topk, result);
        return result;
    }

    public FHashMapIntInt getTop(int k) {
        return getTop(new FHashMapIntInt(), k);
    }

    public void cutBelow(int k) {
        IntIterator iter = values().iterator();
        while (iter.hasNext()) {
            if (iter.nextInt() < k) {
                iter.remove();
            }
        }
    }

    public void cutAbove(double k) {
        IntIterator iter = values().iterator();
        while (iter.hasNext()) {
            if (iter.nextInt() > k) {
                iter.remove();
            }
        }
    }
}
