package io.github.repir.tools.hadoop.hashjoin;

import io.github.repir.tools.hadoop.io.MultiWritable;
import org.apache.hadoop.io.LongWritable;

public abstract class Mapper<K, V> extends org.apache.hadoop.mapreduce.Mapper<LongWritable, MultiWritable, K, V> {

}
