package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public class StringPairInputSplit extends KVInputSplit<String, String> {

   public static Log log = new Log(StringPairInputSplit.class); 

   public StringPairInputSplit() {
      super();
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
    public void writeKey(BufferDelayedWriter out, String key) {
        out.write(key.toString());
    }

    @Override
    public String readKey(BufferReaderWriter reader) {
        return reader.readString();
    }
}
