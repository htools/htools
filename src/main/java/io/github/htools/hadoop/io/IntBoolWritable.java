package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;
import org.apache.hadoop.io.IntWritable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Pairs an int that indicates the partition number with a of long used for
 * secondary sorting.
 *
 * @author jeroen
 */
public class IntBoolWritable extends IntWritable {

    public static Log log = new Log(IntBoolWritable.class);
    private boolean value2;

    public IntBoolWritable() {
        super();
    }

    public IntBoolWritable(int value1, boolean value2) {
        super(value1);
        this.value2 = value2;
    }

    public void set(int value1, boolean value2) {
        super.set(value1);
        this.value2 = value2;
    }
    
    public boolean getValue2() {
        return value2;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        set(in.readInt());
        value2 = in.readBoolean();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(get());
        out.writeBoolean(value2);
    }
}
