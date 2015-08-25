package io.github.htools.hadoop.xml;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.hadoop.io.buffered.DelayedWritable;
import java.io.DataInput;
import java.io.IOException;

/**
 * Generic FileWritable interface for Structured TSV files. Subclasses must implement 
 readFields and write for Hadoop serialization and read/write to a File. Although
 the read/write code for Hadoop/File are usually redundant, thsi is faster than
 generic serialization through Avro/Json/TSV.FileWritable.
 * @author jeroen
 * @param <F> 
 */
public abstract class Writable<F extends File> 
        extends io.github.htools.hadoop.io.buffered.Writable 
        implements io.github.htools.hadoop.io.FileWritable<F> {

    @Override
    public abstract void write(BufferDelayedWriter writer);
    
    @Override
    public void readFields(DataInput di) throws IOException {
        super.readFields(di);
        readFields(reader);
    }
    
    public <K extends Writable> K toWritable(K w) {
        w.readFields(reader);
        return w;
    }
    
    public abstract void readFields(BufferReaderWriter reader);
}
