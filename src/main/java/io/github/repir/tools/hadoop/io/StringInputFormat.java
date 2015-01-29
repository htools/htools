package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.lib.Log;
import org.apache.hadoop.io.Text;

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
public class StringInputFormat extends ConstInputFormat<String, String> {

   public static Log log = new Log(StringInputFormat.class);

   public StringInputFormat() {}
   
   @Override
   public MRInputSplit<String, String> createIS(String text) {
      return new StringInputSplit(text);
   }
}
