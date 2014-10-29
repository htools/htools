package io.github.repir.tools.hadoop.HashJoin;

import io.github.repir.tools.Buffer.BufferDelayedWriter;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.hadoop.WritableDelayed;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;
/**
 *
 * @author jeroen
 */
public class BytesWritable implements Writable {
   public static final Log log = new Log( BytesWritable.class );
   WritableDelayed record;
   BufferDelayedWriter writer;
   BufferReaderWriter reader;
   
   public void set(WritableDelayed w) {
       record = w;    
   }

   public void get(WritableDelayed w) {
       w.readFields(reader);
   }
   
    @Override
    public void write(DataOutput out) throws IOException {
       if (writer == null)
           writer = new BufferDelayedWriter();
       record.write(writer);
       writer.writeBuffer(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        reader = new BufferReaderWriter(in);
    }
}
