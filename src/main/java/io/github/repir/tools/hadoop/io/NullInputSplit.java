package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;
import org.apache.hadoop.io.NullWritable;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public class NullInputSplit extends MRInputSplit<NullWritable, NullWritable> {

   public static Log log = new Log(NullInputSplit.class);

   public NullInputSplit() {
   }

   public NullInputSplit(NullWritable key) {
      super(NullWritable.get());
   }

   @Override
   public void writeValue(BufferDelayedWriter out, NullWritable key) {
   }

   @Override
   public NullWritable readValue(BufferReaderWriter reader) {
      return NullWritable.get();
   }

    @Override
    public void writeKey(BufferDelayedWriter out, NullWritable key) {
    }

    @Override
    public NullWritable readKey(BufferReaderWriter reader) {
        return NullWritable.get();
    }
}
