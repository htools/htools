package io.github.repir.tools.hadoop.io;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 *
 * @author Jeroen Vuurens
 */
public class KVRecordReader<KEY, VALUE> extends RecordReader<KEY, VALUE> {
    public static Log log = new Log(KVRecordReader.class);
   private KEY currentkey; 
   private VALUE currentvalue;
   private KVInputSplit<KEY, VALUE> is;
   private int pos = 0;

   @Override
  public void initialize(InputSplit is, TaskAttemptContext tac) throws IOException, InterruptedException {
      this.is = (KVInputSplit) is;
      log.info("MMRecordReader %d %d", this.is.size(), pos);
  }

   @Override
   public boolean nextKeyValue() throws IOException, InterruptedException {
       log.info("nextKeyValue %d %d", pos, is.size());
      if (pos < is.size()) {
         currentkey = is.get(pos).getKey();
         currentvalue = is.get(pos++).getValue();
         return true;
      }
      return false;
   }

   @Override
   public KEY getCurrentKey() throws IOException, InterruptedException {
      return currentkey;
   }

   @Override
   public VALUE getCurrentValue() throws IOException, InterruptedException {
      return currentvalue;
   }

   @Override
   public float getProgress() throws IOException, InterruptedException {
      return (pos) / (float) (is.list.size());
   }

   @Override
   public void close() throws IOException {
   }

}
