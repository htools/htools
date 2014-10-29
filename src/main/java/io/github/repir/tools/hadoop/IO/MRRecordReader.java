package io.github.repir.tools.hadoop.IO;
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 *
 * @author Jeroen Vuurens
 */
public class MRRecordReader<PWRITABLE extends Writable, P> extends RecordReader<IntWritable, PWRITABLE> {
  private IntWritable currentkey; 
   private PWRITABLE currentvalue;
   private MRInputSplit<PWRITABLE, P> is;
   private int pos = 0;

   @Override
  public void initialize(InputSplit is, TaskAttemptContext tac) throws IOException, InterruptedException {
      this.is = (MRInputSplit) is;
      currentkey = new IntWritable(this.is.partition);
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
   public IntWritable getCurrentKey() throws IOException, InterruptedException {
      return currentkey;
   }

   @Override
   public PWRITABLE getCurrentValue() throws IOException, InterruptedException {
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
