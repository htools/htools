package io.github.repir.tools.hadoop.io;
import java.io.IOException;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 *
 * @author Jeroen Vuurens
 */
public class MRRecordReader<KEY, VALUE> extends RecordReader<KEY, VALUE> {
  private KEY currentkey; 
   private VALUE currentvalue;
   private MRInputSplit<KEY, VALUE> is;
   private int pos = 0;

   @Override
  public void initialize(InputSplit is, TaskAttemptContext tac) throws IOException, InterruptedException {
      this.is = (MRInputSplit) is;
      currentkey = this.is.partition;
  }

   @Override
   public boolean nextKeyValue() throws IOException, InterruptedException {
      if (pos < is.size()) {
         currentvalue = is.get(pos++);
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
