package io.github.htools.hadoop.io;

import io.github.htools.hadoop.Job;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * A custom implementation of Hadoop's InputFormat, that holds the InputSplits
 * with defined values rather than files. This class should be used as static.
 * Internally, a separate InputSplit
 * is created for each repository partition. Whenever a Query request is added,
 * it is added to each Split.
 * <p>
 * When cansplit==true, then the InputSplits are divided over 2 * nodes in
 * cluster (as defined in cluster.nodes), to divide the workload more evenly.
 *
 * @author jeroen
 */
public abstract class KVInputFormat<KEY, VALUE> extends InputFormat<KEY, VALUE> {

   public static Log log = new Log(KVInputFormat.class);
   static boolean cansplit = true;
   static HashMap<Object, KVInputSplit> map = new HashMap();

   public KVInputFormat() {}
   
   @Override
   public RecordReader<KEY, VALUE> createRecordReader(InputSplit is, TaskAttemptContext tac) {
      return new KVRecordReader<KEY, VALUE>();
   }
   
   public static KVInputFormat getInputFormat(Job job) {
       return (KVInputFormat)FileInputFormat.getInputFormat(job);
   }
   
   public static void setSplitable(boolean cansplit) {
      KVInputFormat.cansplit = cansplit;
   }

   public static KVInputSplit getSplit(Object key) {
       return map.get(key);
   }
   
   public static void putSplit(Object key, KVInputSplit list) {
       map.put(key, list);
   }
   
   public static int size() {
       return map.size();
   }
   
   protected abstract KVInputSplit<KEY, VALUE> createSplit();
   
    public static void add(Job job, Object splitid, Object key, Object value) {
        KVInputSplit currentsplit = getSplit(splitid);
        if (currentsplit == null) {
            currentsplit = getInputFormat(job).createSplit();
            putSplit(splitid, currentsplit);
        }
        currentsplit.add(key, value);
    }
   /**
    * if there are less partitions than we have nodes, we can divide Splits into
    * smaller Splits to retrieve queries in parallel. This requires
    * cluster.nodes to be set in the configuration file.
    * <p>
    * @return @throws java.io.IOException
    * @throws java.lang.InterruptedException
    */
   @Override
   public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
         return new ArrayList<InputSplit>(map.values());
   }
}
