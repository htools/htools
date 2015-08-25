package io.github.htools.hadoop.document;

import io.github.htools.lib.Log;
import io.github.htools.hadoop.InputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * @author jeroen
 */
public class DocumentInputFormat extends InputFormat<byte[]> {
    public final static String STARTLABEL = "documentinputformat.startlabel";
    public final static String ENDLABEL = "documentinputformat.endlabel";
    
    @Override
    public RecordReader<LongWritable, byte[]> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        return new DocumentReader();
    }
    
    public final static void setDocumentStart(Configuration conf, String label) {
        conf.set(STARTLABEL, label);
    }
    
    public final static void setDocumentEnd(Configuration conf, String label) {
        conf.set(ENDLABEL, label);
    }
}
