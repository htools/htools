package io.github.htools.fcollection;

import io.github.htools.collection.*;
import io.github.htools.lib.CollectionTools;
import io.github.htools.lib.Log;
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
public class FHashMapInt<K> extends Object2IntOpenHashMap<K> implements java.util.Map<K, Integer> {

    public static final Log log = new Log(FHashMapInt.class);

    public FHashMapInt() {
        super();
    }

    public FHashMapInt(int size) {
        super(size);
    }

    public FHashMapInt(Map<K, Integer> map) {
        super(map);
    }

    @Override
    public FHashMapInt clone() {
        FHashMapInt clone = new FHashMapInt(size());
        for (Object2IntMap.Entry<K> entry : object2IntEntrySet()) {
            add(entry.getKey(), entry.getIntValue());
        }
        return clone;
    }

    protected FHashMapInt create() {
        return new FHashMapInt();
    }

    public void add(K key, int value) {
        int newvalue = value + this.getInt(key);
        if (newvalue != 0) {
            put(key, newvalue);
        } else {
            remove(key);
        }
    }

    public FHashMapInt add(FHashMapInt<K> map) {
        for (Object2IntMap.Entry<K> entry : map.object2IntEntrySet()) {
            add(entry.getKey(), entry.getIntValue());
        }
        return this;
    }

    public void add(K key) {
        add(key, 1);
    }

    public void addAll(Collection<K> keys) {
        for (K key : keys) {
            add(key, 1);
        }
    }

    /**
     * creates a new Map containing the tallies of this map minus the tallies in
     * map v.
     *
     * @param value
     */
    public FHashMapInt<K> subtract(FHashMapInt<K> value) {
        FHashMapInt<K> result = create();
        for (Object2IntMap.Entry<K> entry : object2IntEntrySet()) {
            int v = entry.getIntValue() - value.getInt(entry.getKey());
            if (v > 0) {
                result.add(entry.getKey(), v);
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
    public void remove(FHashMapInt<K> v) {
        for (Object2IntMap.Entry<K> entry : v.object2IntEntrySet()) {
            add(entry.getKey(), -entry.getIntValue());
        }
    }

    public K max() {
        int maxvalue = Integer.MIN_VALUE;
        K max = null;
        for (Object2IntMap.Entry<K> entry : this.object2IntEntrySet()) {
            if (entry.getIntValue() > maxvalue) {
                maxvalue = entry.getIntValue();
                max = entry.getKey();
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

    public <S extends FHashMapDouble<K>> S divide(S result, double div) {
        for (Object2IntMap.Entry<K> entry : this.object2IntEntrySet()) {
            result.put(entry.getKey(), entry.getIntValue() / div);
        }
        return result;
    }

    public K min() {
        int minvalue = Integer.MAX_VALUE;
        K min = null;
        for (Object2IntMap.Entry<K> entry : this.object2IntEntrySet()) {
            if (entry.getIntValue() < minvalue) {
                minvalue = entry.getIntValue();
                min = entry.getKey();
            }
        }
        return min;
    }

    public void addIfExists(K key, int value) {
        Integer oldvalue = get(key);
        if (oldvalue != null) {
            put(key, oldvalue + value);
        }
    }

    public <S extends FHashMapInt<K>> S getTop(S result, int k) {
        TopKMap<Integer, K> topk = new TopKMap(k);
        CollectionTools.invert(this.entrySet(), topk);
        CollectionTools.invert(topk, result);
        return result;
    }

    public FHashMapInt getTop(int k) {
        return getTop(new FHashMapInt(), k);
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
