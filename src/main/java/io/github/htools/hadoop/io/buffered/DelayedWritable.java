package io.github.htools.hadoop.io.buffered;

import io.github.htools.hadoop.io.buffered.Writable;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.hadoop.tsv.File;
import java.io.DataInput;
import java.io.IOException;

/**
 * Generic Writable that can transport multiple in/output types. Typically this
 * class is used by HashJoin to mask multiple input formats. When read in the mapper
 * it can be inspected with instanceof to check the incoming type. When read in the
 * Reducer, InputFormat can supply an Iterator for MultiWritable when supplied with
 * a key that implements HashJoinType (thus allowing to figure out the true type)
 * and 
 * @author jeroen
 * @param <F> 
 */
public class DelayedWritable<F extends File> extends Writable {
    
    @Override
    public void write(BufferDelayedWriter writer) {}

    @Override
    public void readFields(BufferReaderWriter reader) {}
    
    @Override
    public void readFields(DataInput di) throws IOException {
        if (reader == null)
            reader = new BufferReaderWriter(di);
        else
            reader.readBuffer(di);
    }
}
