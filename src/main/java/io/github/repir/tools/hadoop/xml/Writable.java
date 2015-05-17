package io.github.repir.tools.hadoop.xml;

import io.github.repir.tools.hadoop.tsv.*;
import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.hadoop.io.buffered.DelayedWritable;
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
        extends io.github.repir.tools.hadoop.io.buffered.Writable 
        implements io.github.repir.tools.hadoop.io.FileWritable<F> {

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
