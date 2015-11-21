package io.github.htools.fcollection;

import io.github.htools.collection.*;
import io.github.htools.lib.CollectionTools;
import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A HashMap with a Double value, that supports adding values to existing keys
 * @author jeroen
 */
public class FHashMapObjectDouble<K> extends Object2DoubleOpenHashMap<K> implements java.util.Map<K, Double> {

    public static final Log log = new Log(FHashMapObjectDouble.class);

    public FHashMapObjectDouble() {
        super();
    }

    public FHashMapObjectDouble(int size) {
        super(size);
    }

    public FHashMapObjectDouble(int size, float loadfactor) {
        super(size, loadfactor);
    }

    public FHashMapObjectDouble(java.util.Map<K, Double> map) {
        super(map);
    }
    
   @Override
    public FHashMapObjectDouble clone() {
        FHashMapObjectDouble clone = new FHashMapObjectDouble(size());
        for (Object2DoubleMap.Entry<K> entry : object2DoubleEntrySet()) {
            clone.put(entry.getKey(), entry.getDoubleValue());
        }
        return clone;
    }
    
    public Map<Double, Object> invert() {
        return new InvertedMap(this);
    }
    
    public void add(K key, double value) {
        put(key, this.getDouble(key) + value);
    }

    public <S extends FHashMapObjectDouble<K>> S divide(S result, double div) {
        for (Object2DoubleMap.Entry<K> entry : object2DoubleEntrySet()) {
            result.put(entry.getKey(), entry.getDoubleValue() / div);
        }
        return result;
    }
    
   public <S extends Map<K, Double>> S getTop(S result, int k) {
       TopKMap<Double, K> topk = new TopKMap(k);
       CollectionTools.invert(this.entrySet(), topk);
       CollectionTools.invert(topk, result);
       return result;
   }
   
   public FHashMapObjectDouble getTop(int k) {
       return getTop(new FHashMapObjectDouble(), k);
   }
    
   public void cutBelow(double k) {
       DoubleIterator iter = values().iterator();
       while (iter.hasNext()) {
           if (iter.nextDouble() < k)
               iter.remove();
       }
   }
   
   public void cutAbove(double k) {
       DoubleIterator iter = values().iterator();
       while (iter.hasNext()) {
           if (iter.nextDouble() > k)
               iter.remove();
       }
   }
}
