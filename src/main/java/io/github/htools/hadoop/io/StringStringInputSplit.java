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
public class StringStringInputSplit extends MRInputSplit<String, String> {

   public static Log log = new Log(StringStringInputSplit.class); 

   public StringStringInputSplit() {
      super();
   }

   public StringStringInputSplit(String text) {
      super( text );
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
