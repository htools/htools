package io.github.htools.hadoop.io;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.Text;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Pairs an int that indicates the partition number with a of long used for
 * secondary sorting.
 *
 * @author jeroen
 */
public class TextLongLong extends Text {

    public static Log log = new Log(TextLongLong.class);
    private long value1;
    private long value2;

    public TextLongLong() {
        super();
    }

    public TextLongLong(String key, long value1, long value2) {
        super(key);
        this.value1 = value1;
        this.value2 = value2;
    }

    public void set(String key, long value1, long value2) {
        super.set(key);
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readString0());
        value1 = reader.readLong();
        value2 = reader.readLong();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        writer.write0(toString());
        writer.write(value1);
        writer.write(value2);
        writer.writeBuffer(out);
    }
}
