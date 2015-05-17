package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.lib.MathTools;
import org.apache.hadoop.io.Text;

/**
 *
 * @author jeroen
 */
public class TextPartitioner extends org.apache.hadoop.mapreduce.Partitioner<Text, Object> {

    @Override
    public int getPartition(Text key, Object value, int numPartitions) {
        return MathTools.mod(key.toString().hashCode(), numPartitions);
    }

}
