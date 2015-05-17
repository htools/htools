package io.github.repir.tools.hadoop.io.backup;

import io.github.repir.tools.hadoop.Conf;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.Job;
import io.github.repir.tools.hadoop.io.DatafileInputFormat;
import io.github.repir.tools.hadoop.io.FilePairInputFormat;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.ClassTools;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

/**
 * @author jeroen
 */
public class RestoreJob extends CopyJob {

    private static final Log log = new Log(RestoreJob.class);
    
    private RestoreJob(Conf conf) throws IOException {
        super(conf, RestoreMap.class);   
    }
    
    public static void copyPaths(Conf conf, Class<? extends PathModifier> modifier, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        RestoreJob bj = new RestoreJob(conf);
        FilePairInputFormat.addPaths(bj, CopyJob.getModifier(conf, modifier), path);
        bj.waitForCompletion(true);
    }
    
    public static void copyPaths(Conf conf, String search, String replace, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        RestoreJob job = new RestoreJob(conf);
        FilePairInputFormat.addPaths(job, new ConfigurableModifier(search, replace), path);
        job.waitForCompletion(true);
    }
    
    public static void copyDatafiles(Conf conf, String search, String replace, Collection<Datafile> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        RestoreJob job = new RestoreJob(conf);
        FilePairInputFormat.addDatafiles(job, new ConfigurableModifier(search, replace), path);
        job.waitForCompletion(true);
    }
}
