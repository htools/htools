package io.github.htools.hadoop.hashjoin;

import io.github.htools.hadoop.Job;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
/**
 *
 * @author jeroen
 */
public class HashPartitioner<K, V> extends org.apache.hadoop.mapreduce.lib.partition.HashPartitioner<K, V> {
   public static final Log log = new Log( HashPartitioner.class );
   private static final String HPCONFIG = "hashpartitioner.outpath";

   public static void setupNullOutputFormat(Job job, String outpath, int reducers) {
       job.getConfiguration().set(HPCONFIG, outpath);
       job.setOutputFormatClass(NullOutputFormat.class);
       job.setNumReduceTasks(reducers);
   }
   
   public static String getOutfile(Context context) {
       try {
           if (HashPartitioner.class.isAssignableFrom(context.getPartitionerClass())) {
               String outpath = context.getConfiguration().get(HPCONFIG);
               if (outpath != null) {
               int partition = Job.getReducerId(context);
               return PrintTools.sprintf("%s/partition.%5d", outpath, partition);
               } else {
                   log.info("must use setOutPath on HashPartitioner");
               }
           }
       } catch (ClassNotFoundException ex) {
           log.exception(ex, "HashPartitioner");
       }
       throw new RuntimeException("fatal");
   }
}
