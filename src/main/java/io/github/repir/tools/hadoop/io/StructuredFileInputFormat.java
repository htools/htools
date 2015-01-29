package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.struct.StructuredRecordFile;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * @author jeroen
 */
public abstract class StructuredFileInputFormat<F extends StructuredRecordFile, W extends org.apache.hadoop.io.Writable>
        extends FileInputFormat<LongWritable, W> {

    public StructuredFileInputFormat(Class fileclass) {
        super(fileclass);
    }
    
    @Override
    public RecordReader<LongWritable, W> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        return new MyRecordReader(fileclass);
    }

    class MyRecordReader extends StructuredRecordReader<F, LongWritable, W> {

        MyRecordReader(Class fileclass) {
            super(fileclass);
        }
        
        @Override
        protected LongWritable nextKey() {
            return new LongWritable(this.structuredRecordFile.getOffset());
        }
    }    
}
