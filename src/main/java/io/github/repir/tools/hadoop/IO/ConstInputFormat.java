package io.github.repir.tools.hadoop.IO;

import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
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
 * <p/>
 * When cansplit==true, then the InputSplits are divided over 2 * nodes in
 * cluster (as defined in cluster.nodes), to divide the workload more evenly.
 *
 * @author jeroen
 */
public abstract class ConstInputFormat<PWRITABLE extends Writable, P> extends InputFormat<IntWritable, PWRITABLE> {

   public static Log log = new Log(ConstInputFormat.class);
   static boolean cansplit = true;
   static HashMap<Integer, MRInputSplit> list = new HashMap<Integer, MRInputSplit>();

   public ConstInputFormat() {}
   
   @Override
   public RecordReader<IntWritable, PWRITABLE> createRecordReader(InputSplit is, TaskAttemptContext tac) {
      return new MRRecordReader<PWRITABLE, P>();
   }

   /**
    * Add a Query request to the MapReduce job. Note that this is used as a
    * static method (i.e. can only construct one job at the same startTime).
    * <p/>
    * @param repository Repository to retrieve the Query request from
    * @param queryrequest The Query request to retrieve
    */
   public void add(int partition, P value) {
         MRInputSplit<PWRITABLE, P> split = list.get(partition);
         if (split == null) {
            split = createIS(partition);
            list.put(partition, split);
         }
         split.add(value);
   }

   public void addSingle(P value) {
       add(0, value);
   }

   public void setSplitable(boolean cansplit) {
      ConstInputFormat.cansplit = cansplit;
   }

   public abstract MRInputSplit<PWRITABLE, P> createIS(int partition);

   /**
    * if there are less partitions than we have nodes, we can divide Splits into
    * smaller Splits to retrieve queries in parallel. This requires
    * cluster.nodes to be set in the configuration file.
    * <p/>
    * @return @throws java.io.IOException
    * @throws java.lang.InterruptedException
    */
   @Override
   public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
         return new ArrayList<InputSplit>(list.values());
   }
}
