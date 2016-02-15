package io.github.htools.hadoop.document;

import io.github.htools.hadoop.InputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * @author jeroen
 */
public class WikipediaInputFormat extends InputFormat<byte[]> {
    
    @Override
    public RecordReader<LongWritable, byte[]> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        return new ReaderWikipedia();
    }
}
