package io.github.repir.tools.collection;

import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.buffer.BufferSerializable;
import io.github.repir.tools.io.struct.StructureReader;
import io.github.repir.tools.io.struct.StructureWriter;
import io.github.repir.tools.lib.CollectionTools;
import io.github.repir.tools.lib.Log;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 *
 * @author jeroen
 */
public class HashMapInt<K> extends HashMap<K, Integer> {
   public static final Log log = new Log( HashMapInt.class );

   public HashMapInt() {
       super();
   }
   
   public HashMapInt(int size) {
       super(size);
   }
   
    public HashMapInt(Map<K, Integer> map) {
        super(map);
    }
    
    protected HashMapInt create() {
        return new HashMapInt();
    }
    
   public void add(K key, int value) {
       Integer oldvalue = get(key);
       if (oldvalue == null)
           put(key, value);
       else {
           int newvalue = oldvalue + value;
           if (newvalue == 0)
               remove(key);
           else
               put(key, newvalue);
       }
   }
   
   public HashMapInt add(HashMapInt<K> map) {
       for (Map.Entry<K, Integer> entry : map.entrySet())
           add(entry.getKey(), entry.getValue());
       return this;
   }
   
   public void add(K key) {
       add(key, 1);
   }
   
   public void addAll(Collection<K> keys) {
       for (K key : keys)
          add(key, 1);
   }
   
   /**
     * creates a new Map containing the tallies of this map minus the tallies in
     * map v.
     * @param v 
     */
    public HashMapInt<K> subtract(HashMapInt<K> v) {
        HashMapInt<K> result = create(); 
        for (Map.Entry<K, Integer> entry : entrySet()) {
            Integer freq = entry.getValue();
            Integer freq2 = v.get(entry.getKey());
            if (freq - freq2 > 0)
               result.add(entry.getKey(), freq - freq2);
        }
        return result;
    }

    /**
     * subtracts the values from given keys, will on work when the keys with given values exists.
     * @param v 
     */
    public void remove(HashMapInt<K> v) {
        for (Map.Entry<K, Integer> entry : v.entrySet()) {
            add(entry.getKey(), -entry.getValue());
        }
    }
    
   @Override
   public Integer get(Object key) {
       Integer oldvalue = super.get(key);
       return (oldvalue==null)?0:oldvalue;
   }
   
   public K max() {
       Integer maxvalue = Integer.MIN_VALUE;
       K max = null;
       for (Map.Entry<K, Integer> entry : entrySet()) {
           if (entry.getValue() > maxvalue) {
               maxvalue = entry.getValue();
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
   
   public <S extends HashMapDouble<K>> S divide(S result, double div) {
       for (Map.Entry<K, Integer> entry : entrySet()) {
           result.put(entry.getKey(), entry.getValue() / div);
       }
       return result;
   }
   
   public K min() {
       Integer minvalue = Integer.MAX_VALUE;
       K min = null;
       for (Map.Entry<K, Integer> entry : entrySet()) {
           if (entry.getValue() < minvalue) {
               minvalue = entry.getValue();
               min = entry.getKey();
           }
       }
       return min;
   }
   
   public void addIfExists(K key, int value) {
       Integer oldvalue = get(key);
       if (oldvalue != null)
          put(key, oldvalue+value);
   }
   
   public <S extends HashMapInt<K>> S getTop(S result, int k) {
       TopKMap<Integer, K> topk = new TopKMap(k);
       CollectionTools.invert(this, topk);
       CollectionTools.invert(topk, result);
       return result;
   }
   
   public HashMapInt getTop(int k) {
       return getTop(new HashMapInt(), k);
   }
   
   public void cutBelow(int k) {
       Iterator<Integer> iter = values().iterator();
       while (iter.hasNext()) {
           Integer d = iter.next();
           if (d < k)
               iter.remove();
       }
   }
   
   public void cutAbove(double k) {
       Iterator<Integer> iter = values().iterator();
       while (iter.hasNext()) {
           Integer d = iter.next();
           if (d > k)
               iter.remove();
       }
   }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
