package io.github.repir.tools.hadoop;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Extension of Hadoop Job.
 * @author jer
 */
public class Job extends org.apache.hadoop.mapreduce.Job {

   public static Log log = new Log(Job.class);

   public Job(Configuration configuration) throws IOException {
      // Jars need to be added to the Configuration before construction 
      super(configuration);
   }

   public Job(Configuration configuration, String jobname, Object ... params) throws IOException {
      this(configuration);
      setJobName(PrintTools.sprintf(jobname, params));
   }

  public void submit() throws IOException, InterruptedException, ClassNotFoundException {
      setJarByClass(this.getMapperClass());
      super.submit();
  }
     
   public static int getReducerId(Reducer.Context context) {
      return context.getTaskAttemptID().getTaskID().getId();
   }

   public static enum MATCH_COUNTERS {
      MAPTASKSDONE,
      MAPTASKSTOTAL,
      REDUCETASKSDONE,
      REDUCETASKSTOTAL
   }

   public static void reduceReport(Reducer.Context context) {
      context.getCounter(MATCH_COUNTERS.REDUCETASKSDONE).increment(1);
      context.progress();
   }

}
