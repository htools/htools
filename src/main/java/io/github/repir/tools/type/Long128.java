package io.github.repir.tools.type;

import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.buffer.BufferSerializable;
import io.github.repir.tools.io.struct.StructureReader;
import io.github.repir.tools.io.struct.StructureWriter;
import java.util.UUID;

public class Long128 implements Comparable<Long128>, BufferSerializable {
    UUID uuid;
   
    public Long128() {}
    
    public Long128(long high, long low) {
        uuid = new UUID(high, low);
    }

    public Long128(UUID uuid) {
        this.uuid = uuid;
    }

    public Long128(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }
    
    public UUID getUUID() {
        return uuid;
    }
    
    public Long128 clone() {
        return new Long128(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Long128) && uuid.equals(((Long128)o).uuid);
    }

    @Override
    public String toString() {
        return uuid.toString();
    }


    @Override
    public int compareTo(Long128 o) {
        return uuid.compareTo(o.uuid);
    }  

    @Override
    public void read(StructureReader reader) throws EOCException {
        uuid = new UUID(reader.readLong(), reader.readLong());
    }

    @Override
    public void write(StructureWriter writer) {
        writer.write(uuid.getMostSignificantBits());
        writer.write(uuid.getLeastSignificantBits());
    }
}
