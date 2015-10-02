package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;

/**
 * A HashMap with a Integer value, that supports adding values to existing keys
 *
 * @author jeroen
 */
public class FHashMapIntObject<K> extends Int2ObjectOpenHashMap<K> implements java.util.Map<Integer, K> {

    public static final Log log = new Log(FHashMapIntObject.class);

    public FHashMapIntObject() {
        super();
    }

    public FHashMapIntObject(int size) {
        super(size);
    }

    public FHashMapIntObject(Map<Integer, K> map) {
        super(map);
    }

    @Override
    public FHashMapIntObject clone() {
        return new FHashMapIntObject(this);
    }
}
