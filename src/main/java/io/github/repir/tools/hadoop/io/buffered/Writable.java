package io.github.repir.tools.hadoop.io.buffered;

import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.hadoop.tsv.File;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparator;

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
public abstract class Writable implements org.apache.hadoop.io.Writable {
    protected BufferReaderWriter reader;

    @Override
    public void write(DataOutput d) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        write(writer);
        writer.writeBuffer(d);
    }
    
    public abstract void write(BufferDelayedWriter writer);

    public abstract void readFields(BufferReaderWriter reader);
    
    @Override
    public void readFields(DataInput di) throws IOException {
        if (reader == null)
            reader = new BufferReaderWriter(di);
        else
            reader.readBuffer(di);
        readFields(reader);
    }
}
