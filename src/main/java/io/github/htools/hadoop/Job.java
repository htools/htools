package io.github.htools.hadoop;

import io.github.htools.hadoop.io.OutputFormat;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import java.io.IOException;
import java.lang.reflect.Constructor;
import javax.ws.rs.core.UriBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.JobStatus.State;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Extension of Hadoop Job.
 *
 * @author jer
 */
public class Job extends org.apache.hadoop.mapreduce.Job {

    public static Log log = new Log(Job.class);

    public Job(Configuration configuration, Object... params) throws IOException {
        // Jars need to be added to the Configuration before construction 
        super(configuration);
        setJobName(params);
        this.setJarByClass(ClassTools.getMainClass());
        log.info("Job %s", this.getJobName());
    }

    // sets the jobname to the class containing the main, placing the params in squared brackets
    private void setJobName(Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append(ClassTools.getMainClass().getCanonicalName()).append(" ");
        for (Object o : params) {
            sb.append("[").append(o.toString()).append("] ");
        }
        setJobName(sb.toString());
    }

    public FileSystem getFS() {
        return HDFSPath.getFS(this.conf);
    }

    @Override
    public void submit() throws IOException, InterruptedException, ClassNotFoundException {
        setJarByClass(this.getMapperClass());
        super.submit();
    }

    @Override
    public boolean waitForCompletion(boolean verbose) throws ClassNotFoundException, InterruptedException {
        try {
            return super.waitForCompletion(verbose);
        } catch (IOException | InterruptedException ex) {
            log.exception(ex, "waitForCompletion");
            try {
                while (getJobState() != State.FAILED && getJobState() != State.KILLED) {
                    try {
                        return this.monitorAndPrintJob();
                    } catch (IOException | InterruptedException ex1) {
                        log.exception(ex1, "mintorAndPrintJob");
                    }
                }
            } catch (IOException|IllegalStateException ex1) { 
                try {
                    this.killJob();
                } catch (IOException ex2) {
                    log.exception(ex, "killjob %s", this.getJobName());
                }
            }
        }
        return false;
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

    public void addCacheFile(String hdfs_path, String label) {
        super.addCacheFile(UriBuilder.fromPath(hdfs_path).fragment(label).build());
    }
}
