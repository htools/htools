package io.github.htools.hadoop.io;

import io.github.htools.hadoop.Job;
import io.github.htools.lib.Log;
import org.apache.hadoop.mapreduce.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A custom implementation of Hadoop's InputFormat, that holds the InputSplits
 * with defined values rather than files. This class should be used as static.
 * Internally, a separate InputSplit is created for each repository partition.
 * Whenever a Query request is added, it is added to each Split.
 * <p>
 * When cansplit==true, then the InputSplits are divided over 2 * nodes in
 * cluster (as defined in cluster.nodes), to divide the workload more evenly.
 *
 * @author jeroen
 * @param <KEY>
 * @param <VALUE>
 */
public abstract class ConstInputFormat<KEY, VALUE> extends InputFormat<KEY, VALUE> {

    public static Log log = new Log(ConstInputFormat.class);
    static boolean split = false;
    static HashMap<Object, MRInputSplit> map = new HashMap();

    public ConstInputFormat() {
    }

    @Override
    public RecordReader<KEY, VALUE> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        return new MRRecordReader<KEY, VALUE>();
    }

    public static ConstInputFormat getInputFormat(Job job) {
        return (ConstInputFormat) FileInputFormat.getInputFormat(job);
    }

    public static void setSplitable(boolean cansplit) {
        ConstInputFormat.split = cansplit;
    }

    public static MRInputSplit getSplit(Object key) {
        return map.get(key);
    }

    public static void putSplit(Object key, MRInputSplit list) {
        map.put(key, list);
    }

    public static int size() {
        return map.size();
    }

    protected abstract MRInputSplit<KEY, VALUE> createSplit(KEY key);

    public static void clear() {
        map = new HashMap();
    }

    public static void add(Job job, Object key, Object value) {
        MRInputSplit currentsplit = getSplit(key);
        if (currentsplit == null) {
            currentsplit = getInputFormat(job).createSplit(key);
            putSplit(key, currentsplit);
        }
        currentsplit.add(value);
    }

    /**
     * if there are less partitions than we have nodes, we can divide Splits
     * into smaller Splits to retrieve queries in parallel. This requires
     * cluster.nodes to be set in the configuration file.
     * <p>
     * @return @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        if (!split)
            return new ArrayList<InputSplit>(map.values());
        else {
            List<InputSplit> list = new ArrayList();
            for (Map.Entry<Object, MRInputSplit> entry : map.entrySet()) {
                for (Object value : entry.getValue().list) {
                   MRInputSplit<KEY, VALUE> split = createSplit((KEY)entry.getKey());
                   split.add((VALUE)value);
                   list.add(split);
                }
            }
            return list;
        }
    }
}
