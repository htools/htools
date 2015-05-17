package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jeroen
 */
public class HashMapDouble<K> extends HashMap<K, Double> {

    public static final Log log = new Log(HashMapDouble.class);

    public HashMapDouble() {
        super();
    }

    public void add(K key, double value) {
        Double oldvalue = get(key);
        put(key, oldvalue == null ? value : oldvalue + value);
    }
}
