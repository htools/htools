package io.github.htools.hadoop.io;

import io.github.htools.hadoop.io.buffered.Writable;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 *
 * @author jeroen
 */
public class DelayedWritable implements org.apache.hadoop.io.Writable {

    public static final Log log = new Log(DelayedWritable.class);
    BufferDelayedWriter writer;
    BufferReaderWriter reader;
    public Writable record;
    public HashMap<String, Writable> types;

    @Override
    public void write(DataOutput out) throws IOException {
        if (writer == null) {
            writer = new BufferDelayedWriter();
        }
        writer.write(record.getClass().getCanonicalName());
        record.write(writer);
        writer.writeBuffer(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        reader = new BufferReaderWriter(in);
    }

    public Writable read() {
       getWritable();
       record.readFields(reader);
       return record;
    }

    public Writable getWritable() {
        if (types == null) {
            types = new HashMap();
        }
        String type = reader.readString();
        if (record == null || !record.getClass().getCanonicalName().equals(type)) {
            if (types == null) {
                types = new HashMap();
            }
            record = types.get(type);
            if (record == null) {
                Class<? extends Writable> clazz = ClassTools.toClass(type);
                Constructor<? extends Writable> cons = ClassTools.tryGetAssignableConstructor(clazz, Writable.class);
                record = ClassTools.construct(cons);
                types.put(type, record);
            }
        }
        return record;
    }
}
