package io.github.repir.tools.hadoop.hashjoin;

import io.github.repir.tools.hadoop.io.buffered.DelayedWritable;
import org.apache.hadoop.io.LongWritable;

public abstract class Mapper<K, V> extends org.apache.hadoop.mapreduce.Mapper<LongWritable, DelayedWritable, K, V> {

}
