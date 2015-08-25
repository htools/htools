package io.github.htools.hadoop.hashjoin;

import io.github.htools.hadoop.io.buffered.DelayedWritable;
import org.apache.hadoop.io.LongWritable;

public abstract class Mapper<K, V> extends org.apache.hadoop.mapreduce.Mapper<LongWritable, DelayedWritable, K, V> {

}
