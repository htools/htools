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
public class TextText extends Text {

    public static Log log = new Log(TextText.class);
    private String value2;

    public TextText() {
        super();
    }

    public TextText(String key, String value2) {
        super(key);
        this.value2 = value2;
    }

    public void set(String key, String value2) {
        super.set(key);
        this.value2 = value2;
    }

    public String getValue2() {
        return value2;
    }
    
    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readString0());
        value2 = reader.readString0();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        writer.write0(toString());
        writer.write0(value2);
        writer.writeBuffer(out);
    }
}
