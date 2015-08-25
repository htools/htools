package io.github.htools.hadoop.io;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p>
 * @author jeroen
 */
public class IntStringInputSplit extends MRInputSplit<Integer, String> {

   public static Log log = new Log(IntStringInputSplit.class); 

   public IntStringInputSplit() {
      super();
   }

   public IntStringInputSplit(Object text) {
      super( (Integer)text );
   }

   @Override
   public void writeValue(BufferDelayedWriter out, String value) {
      out.write(value);
   }

   @Override
   public String readValue(BufferReaderWriter reader) {
      return reader.readString();
   }

    @Override
    public void writeKey(BufferDelayedWriter out, Integer key) {
        out.write(key);
    }

    @Override
    public Integer readKey(BufferReaderWriter reader) {
        return reader.readInt();
    }
}
