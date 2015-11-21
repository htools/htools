package io.github.htools.collection;

import io.github.htools.lib.CollectionTools;
import io.github.htools.lib.Log;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A HashMap with a Double value, that supports adding values to existing keys
 * @author jeroen
 */
public class HashMapDouble<K> extends HashMap<K, Double> {

    public static final Log log = new Log(HashMapDouble.class);

    public HashMapDouble() {
        super();
    }

    public HashMapDouble(Map<K, Double> map) {
        super(map);
    }
    
    public Map<Double, K> invert() {
        return new InvertedMap(this);
    }
    
    public void add(K key, double value) {
        Double oldvalue = get(key);
        put(key, oldvalue == null ? value : oldvalue + value);
    }

    public void addAll(Collection<K> keys) {
        for (K key: keys)
            add(key, 1);
    }

    public <S extends HashMapDouble<K>> S divide(S result, double div) {
        for (Map.Entry<K, Double> entry : entrySet()) {
            result.put(entry.getKey(), entry.getValue() / div);
        }
        return result;
    }
    
   public <S extends HashMapDouble<K>> S getTop(S result, int k) {
       TopKMap<Double, K> topk = new TopKMap(k);
       CollectionTools.invert(this, topk);
       CollectionTools.invert(topk, result);
       return result;
   }
   
   public HashMapDouble getTop(int k) {
       return getTop(new HashMapDouble(), k);
   }
    
   public void cutBelow(double k) {
       Iterator<Double> iter = values().iterator();
       while (iter.hasNext()) {
           Double d = iter.next();
           if (d < k)
               iter.remove();
       }
   }
   
   public void cutAbove(double k) {
       Iterator<Double> iter = values().iterator();
       while (iter.hasNext()) {
           Double d = iter.next();
           if (d > k)
               iter.remove();
       }
   }
}
