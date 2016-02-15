package io.github.htools.hadoop.io;

import io.github.htools.hadoop.tsv.File;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.struct.StructuredFileRecord;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class FileWritableComparable<W extends FileWritableComparable, F extends File> 
       implements org.apache.hadoop.io.WritableComparable<W>, StructuredFileRecord<F>, FileWritable<F> {
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