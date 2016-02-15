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
public class TextTextIntIntBool extends Text {

    public static Log log = new Log(TextTextIntIntBool.class);
    private String value1;
    private int value2;
    private int value3;
    public boolean isCandidate;

    public TextTextIntIntBool() {
        super();
    }

    public TextTextIntIntBool(String key, String value1, int value2, int value3, boolean iscandidate) {
        super(key);
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.isCandidate = iscandidate;
    }

    public void set(String key, String value1, int value2, int value3, boolean iscandidate) {
        super.set(key);
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.isCandidate = iscandidate;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readString0());
        value1 = reader.readString0();
        value2 = reader.readInt();
        value3 = reader.readInt();
        isCandidate = reader.readBoolean();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        writer.write0(toString());
        writer.write0(value1);
        writer.write(value2);
        writer.write(value3);
        writer.write(isCandidate);
        writer.writeBuffer(out);
    }    
}
