package io.github.htools.hadoop.io.backup;

import io.github.htools.hadoop.Conf;
import io.github.htools.hadoop.io.FilePairInputFormat;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;

import java.io.IOException;
import java.util.Collection;

/**
 * @author jeroen
 */
public class UpdateJob extends CopyJob {

    private static final Log log = new Log(UpdateJob.class);
    
    private UpdateJob(Conf conf) throws IOException {
        super(conf, UpdateMap.class);
    }
    
    public static void copyPaths(Conf conf, Class<? extends PathModifier> modifier, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        UpdateJob bj = new UpdateJob(conf);
        FilePairInputFormat.addPaths(bj, CopyJob.getModifier(conf, modifier), path);
        bj.waitForCompletion(true);
    }
    
    public static boolean copyPaths(Conf conf, String search, String replace, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        UpdateJob job = new UpdateJob(conf);
        FilePairInputFormat.addPaths(job, new ConfigurableModifier(conf, search, replace), path);
        return job.waitForCompletion(true);
    }
    
    public static boolean copyDatafiles(Conf conf, String search, String replace, Collection<Datafile> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        UpdateJob job = new UpdateJob(conf);
        FilePairInputFormat.addDatafiles(job, new ConfigurableModifier(conf, search, replace), path);
        return job.waitForCompletion(true);
    }
}
