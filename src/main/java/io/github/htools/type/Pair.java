package io.github.htools.type;

import io.github.htools.lib.Log;

/**
 *
 * @author Jeroen
 */
public interface Pair<K, V> {
    K getKey();
    V getValue();
}
