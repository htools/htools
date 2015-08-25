package io.github.htools.hadoop.io;

import io.github.htools.hadoop.Job;
import io.github.htools.lib.Log;
import java.util.Collection;

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
public abstract class InputFormatPerNode<VALUE> extends ConstInputFormat<Integer, VALUE> {

   public static Log log = new Log(InputFormatPerNode.class);
   static int elements = 0;
   static MRInputSplit currentsplit;
   static int maxnodes = 1;

   public InputFormatPerNode() {}
   
   public static void setMaxNodes(int size) {
       maxnodes = size;
   }

    public static void add(Job job, Object value) {
        int node = elements++ % maxnodes;
        currentsplit = getSplit(node);
        if (currentsplit == null) {
            currentsplit = getInputFormat(job).createSplit(node);
            putSplit(node, currentsplit);
        }
        currentsplit.add(value);
    }
   
    public static void add(Job job, Collection values) {
        for (Object o : values)
            add(job, o);
    }
   
   protected abstract MRInputSplit<Integer, VALUE> createSplit(Integer key);
}
