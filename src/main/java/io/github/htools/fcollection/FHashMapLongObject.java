package io.github.htools.fcollection;

import io.github.htools.collection.InvertedMap;
import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;

/**
 * A HashMap with a Integer value, that supports adding values to existing keys
 *
 * @author jeroen
 */
public class FHashMapLongObject<K> extends Long2ObjectOpenHashMap<K> implements java.util.Map<Long, K> {

    public static final Log log = new Log(FHashMapLongObject.class);

    public FHashMapLongObject() {
        super();
    }

    public FHashMapLongObject(int size) {
        super(size);
    }

    public FHashMapLongObject(Map<Long, K> map) {
        super(map);
    }

    @Override
    public FHashMapLongObject clone() {
        return new FHashMapLongObject(this);
    }
    
    public Map<K, Long> invert() {
        return new InvertedMap(this);
    }
}
