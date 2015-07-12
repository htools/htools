package io.github.repir.tools.type;

import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.buffer.BufferSerializable;
import io.github.repir.tools.io.struct.StructureReader;
import io.github.repir.tools.io.struct.StructureWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import org.apache.hadoop.io.WritableComparable;

public class Long128Writable extends Long128 implements WritableComparable<Long128> {
    public Long128Writable() {}
    
    public Long128Writable(long high, long low) {
        super(high, low);
    }

    public Long128Writable(UUID uuid) {
        super(uuid);
    }

    public Long128Writable(String uuid) {
        super(uuid);
    }
    
    public Long128Writable(Long128 l) {
        super(l.uuid);
    }
    
    public Long128Writable clone() {
        return new Long128Writable(uuid);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        uuid = new UUID(in.readLong(), in.readLong());
    }
}
