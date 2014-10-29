package io.github.repir.tools.hadoop;

import io.github.repir.tools.Buffer.BufferDelayedWriter;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.hadoop.Structured.File;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Generic Writable interface for Structured TSV files. Subclasses must implement 
 * readFields and write for Hadoop serialization and read/write to a File. Although
 * the read/write code for Hadoop/File are usually redundant, thsi is faster than
 * generic serialization through Avro/Json/TSV.Writable.
 * @author jeroen
 * @param <F> 
 */
public class WritableDelayed<F extends File> implements org.apache.hadoop.io.Writable {
    protected BufferReaderWriter reader;

    @Override
    public void write(DataOutput d) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        write(writer);
        writer.writeBuffer(d);
    }
    
    public void write(BufferDelayedWriter writer) {}

    public void readFields(BufferReaderWriter reader) {}
    
    @Override
    public void readFields(DataInput di) throws IOException {
        reader = new BufferReaderWriter(di);
    }
}
