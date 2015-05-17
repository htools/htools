package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Text;

/**
 * Pairs an int that indicates the partition number with a of long used for
 * secondary sorting.
 *
 * @author jeroen
 */
public class TextTextInt extends Text {

    public static Log log = new Log(TextTextInt.class);
    private String value2;
    private int value3;

    public TextTextInt() {
        super();
    }

    public TextTextInt(String key, String value2, int value3) {
        super(key);
        this.value2 = value2;
        this.value3 = value3;
    }

    public void set(String key, String value2, int value3) {
        super.set(key);
        this.value2 = value2;
        this.value3 = value3;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readString0());
        value2 = reader.readString0();
        value3 = reader.readInt();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        writer.write0(toString());
        writer.write0(value2);
        writer.write(value3);
        writer.writeBuffer(out);
    }
}
