package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;
import org.apache.hadoop.io.LongWritable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Pairs an long that indicates an id with a of long used for
 * secondary sorting.
 *
 * @author jeroen
 */
public class LongBoolWritable extends LongWritable {

    public static Log log = new Log(LongBoolWritable.class);
    private boolean value2;

    public LongBoolWritable() {
        super();
    }

    public LongBoolWritable(long value1, boolean value2) {
        super(value1);
        this.value2 = value2;
    }

    public void set(long value1, boolean value2) {
        super.set(value1);
        this.value2 = value2;
    }
    
    public boolean getValue2() {
        return value2;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        set(in.readLong());
        value2 = in.readBoolean();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(get());
        out.writeBoolean(value2);
    }
}
