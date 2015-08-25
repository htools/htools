package io.github.htools.hadoop.tsv;

import io.github.htools.hadoop.io.MRInputSplit;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p>
 * @author jeroen
 */
public abstract class StringFileInputSplit<W extends Writable> extends MRInputSplit<String, W> {

   public static Log log = new Log(StringFileInputSplit.class); 

   public StringFileInputSplit() {
      super();
   }

   public StringFileInputSplit(String text) {
      super( text );
   }

   @Override
   public void writeValue(BufferDelayedWriter out, W value) {
       value.write(out);
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
