package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.io.struct.StructuredFileRecord;
import io.github.repir.tools.hadoop.tsv.File;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class WritableComparable<W extends WritableComparable, F extends File> 
       implements org.apache.hadoop.io.WritableComparable<W>, StructuredFileRecord<F>, Writable<F> {
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
        readFields(reader);
    }
}