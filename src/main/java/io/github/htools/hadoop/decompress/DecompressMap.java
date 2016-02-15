package io.github.htools.hadoop.decompress;

import io.github.htools.lib.Log;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Filter clicked urls using a list of domains
 */
public class DecompressMap extends Mapper<LongWritable, Text, NullWritable, Text> {

    public static Log log = new Log(DecompressMap.class);
    
    @Override
    public void map(LongWritable key, Text text, Context context) throws IOException, InterruptedException {
        context.write(NullWritable.get(), text);
    }
}
