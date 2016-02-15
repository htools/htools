package io.github.htools.hadoop.io;

import io.github.htools.lib.MathTools;
import org.apache.hadoop.io.LongWritable;

/**
 *
 * @author jeroen
 */
public class LongPartitioner extends org.apache.hadoop.mapreduce.Partitioner<LongWritable, Object> {

    @Override
    public int getPartition(LongWritable key, Object value, int numPartitions) {
        return MathTools.mod(key.get(), numPartitions);
    }

}
