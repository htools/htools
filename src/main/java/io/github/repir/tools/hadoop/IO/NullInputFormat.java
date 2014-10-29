package io.github.repir.tools.hadoop.IO;

import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.io.NullWritable;

/**
 * A custom implementation of Hadoop's InputFormat, that holds the InputSplits
 * that are to be retrieved. This class should be used as static, using
 * {@link #setRepository(Repository.Repository)} to initialize and 
 * {@link #add(Repository.Repository, IndexReader.Query) }
 * to add Query requests to the MapReduce job. Internally, a separate InputSplit
 * is created for each repository partition. Whenever a Query request is added,
 * it is added to each Split.
 * <p/>
 * When cansplit==true, then the InputSplits are divided over 2 * nodes in cluster
 * (as defined in cluster.nodes), to divide the workload more evenly.
 * 
 * @author jeroen
 */
public class NullInputFormat extends ConstInputFormat<NullWritable, Integer> {

   public static Log log = new Log(NullInputFormat.class);

   public NullInputFormat() {}
   
   @Override
   public MRInputSplit<NullWritable, Integer> createIS(int partition) {
      return new NullInputSplit(partition);
   }

}
