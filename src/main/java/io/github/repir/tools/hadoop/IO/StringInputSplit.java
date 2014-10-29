package io.github.repir.tools.hadoop.IO;

import io.github.repir.tools.Buffer.BufferDelayedWriter;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.io.Text;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public class StringInputSplit extends MRInputSplit<Text, String> {

   public static Log log = new Log(StringInputSplit.class); 

   public StringInputSplit() {
      super();
   }

   public StringInputSplit(int partition) {
      super( partition );
   }

   @Override
   public Text convert(String p) {
      return new Text(p);
   }

   @Override
   public void writeKey(BufferDelayedWriter out, String key) {
      out.write(key);
   }

   @Override
   public String readKey(BufferReaderWriter reader) {
      return reader.readString();
   }
}
