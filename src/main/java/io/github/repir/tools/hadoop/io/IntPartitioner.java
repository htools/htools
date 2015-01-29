package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.lib.MathTools;
import org.apache.hadoop.io.IntWritable;

/**
 *
 * @author jeroen
 */
public class IntPartitioner extends org.apache.hadoop.mapreduce.Partitioner<IntWritable, Object> {

    @Override
    public int getPartition(IntWritable key, Object value, int numPartitions) {
        return MathTools.mod(key.get(), numPartitions);
    }

}
