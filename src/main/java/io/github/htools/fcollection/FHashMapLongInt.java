package io.github.htools.fcollection;

import io.github.htools.collection.InvertedMap;
import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.Map;

/**
 * A HashMap with a Integer value, that supports adding values to existing keys
 *
 * @author jeroen
 */
public class FHashMapLongInt extends Long2IntOpenHashMap implements java.util.Map<Long, Integer> {

    public static final Log log = new Log(FHashMapLongInt.class);

    public FHashMapLongInt() {
        super();
    }

    public FHashMapLongInt(int size) {
        super(size);
    }

    public FHashMapLongInt(Map<Long, Integer> map) {
        super(map);
    }

    @Override
    public FHashMapLongInt clone() {
        return new FHashMapLongInt(this);
    }
    
    public Map<Integer, Long> invert() {
        return new InvertedMap(this);
    }
}
