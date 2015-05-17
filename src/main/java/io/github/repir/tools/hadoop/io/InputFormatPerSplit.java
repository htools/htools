package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.hadoop.Job;
import io.github.repir.tools.lib.Log;
import java.util.Collection;

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
public abstract class InputFormatPerSplit<VALUE> extends ConstInputFormat<Integer, VALUE> {

   public static Log log = new Log(InputFormatPerSplit.class);
   static MRInputSplit currentsplit;
   static int maxsplitsize = Integer.MAX_VALUE;

   public InputFormatPerSplit() {}
   
   public static void setMaxSplitSize(int size) {
       maxsplitsize = size;
   }

    public static void add(Job job, Object value) {
        if (currentsplit == null || currentsplit.size() >= maxsplitsize) {
            currentsplit = getInputFormat(job).createSplit(size());
            putSplit(size(), currentsplit);
        }
        currentsplit.add(value);
    }
   
    public static void add(Job job, Collection values) {
        for (Object o : values)
            add(job, o);
    }
   
   protected abstract MRInputSplit<Integer, VALUE> createSplit(Integer key);
}
