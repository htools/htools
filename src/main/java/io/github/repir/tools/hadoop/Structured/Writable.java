package io.github.repir.tools.hadoop.Structured;

import io.github.repir.tools.Buffer.BufferDelayedWriter;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.hadoop.WritableDelayed;
import java.io.DataInput;
import java.io.IOException;

/**
 * Generic Writable interface for Structured TSV files. Subclasses must implement 
 * readFields and write for Hadoop serialization and read/write to a File. Although
 * the read/write code for Hadoop/File are usually redundant, thsi is faster than
 * generic serialization through Avro/Json/TSV.Writable.
 * @author jeroen
 * @param <F> 
 */
public abstract class Writable<F extends File> extends WritableDelayed implements io.github.repir.tools.hadoop.Writable<F> {

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
