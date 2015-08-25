package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;
import io.github.htools.hadoop.InputFormat;
import java.util.HashMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

/**
 *
 * @author jeroen
 */
public abstract class ReducerKeys<K> {

    public static Log log = new Log(ReducerKeys.class);
    protected final String REDUCERKEYS = "repirtools.reducerkeys";
    protected Configuration conf;
    protected final HashMap<K, Integer> keys;
    
    public ReducerKeys(Configuration conf) {
        this.conf = conf;
        keys = getKeys(conf);
    }

    public HashMap<K, Integer> getKeys() {
        return keys;
    }

    public int getNumReduceTasks() {
        return keys.size();
    }

    public int getPartitionNr(K o) {
        return keys.get(o);
    }

    public int getPartition(K key) {
        Integer partition = keys.get(key);
        if (partition == null) {
            log.info("getPartition(%s) null key=%s", key, keys);
        }
        return partition;
    }

    private HashMap<K, Integer> getKeys(Configuration conf) {
        HashMap<K, Integer> keys = getStoredKeys(conf.get(REDUCERKEYS));
        if (keys == null) {
            keys = new HashMap();
            Path[] inputPaths = InputFormat.getInputPaths(conf);
            for (Path p : inputPaths) {
                K key = getReducerKey(p);
                keys.put(key, keys.size());
            }
            if (keys.size() > 0)
                conf.set(REDUCERKEYS, storeKeys(keys));
        }
        return keys;
    }

    protected String storeKeys(HashMap<K, Integer> keys) {
        return "";
    }

    protected HashMap<K, Integer> getStoredKeys(String keys) {
        return null;
    }

    public abstract K getReducerKey(Path path);

}
