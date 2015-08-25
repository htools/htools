package io.github.htools.collection;

import io.github.htools.type.Tuple2;
import java.util.HashMap;

/**
 *
 * @author jeroen
 */
public class UndirectedMap<K> extends HashMap<K, Tuple2<K, K>> {
    public UndirectedMap() {
        super();
    }
    
    
}
