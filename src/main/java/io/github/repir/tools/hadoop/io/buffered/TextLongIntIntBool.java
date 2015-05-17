package io.github.repir.tools.hadoop.io.buffered;

import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.MathTools;
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
public class TextLongIntIntBool extends Writable {

    public static Log log = new Log(TextLongIntIntBool.class);
    public String value;
    public Long value1;
    public int value2;
    public int value3;
    public boolean isCandidate;

    public String toString() {
        return value;
    }
    
    @Override
    public void write(BufferDelayedWriter writer) {
        writer.write0(toString());
        writer.write(value1);
        writer.write(value2);
        writer.write(value3);
        writer.write(isCandidate);
    }

    @Override
    public void readFields(BufferReaderWriter reader) {
        value = reader.readString0();
        value1 = reader.readLong();
        value2 = reader.readInt();
        value3 = reader.readInt();
        isCandidate = reader.readBoolean();
    }

    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<TextLongIntIntBool, Object> {
        @Override
        public int getPartition(TextLongIntIntBool key, Object value, int numPartitions) {
            return MathTools.mod(key.value.hashCode(), numPartitions);
        }
    }
}
