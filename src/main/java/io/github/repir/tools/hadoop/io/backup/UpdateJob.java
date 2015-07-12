package io.github.repir.tools.hadoop.io.backup;

import io.github.repir.tools.hadoop.Conf;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.io.FilePairInputFormat;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.HDFSPath;
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
    
    public static void copyPaths(Conf conf, String search, String replace, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        UpdateJob job = new UpdateJob(conf);
        FilePairInputFormat.addPaths(job, new ConfigurableModifier(conf, search, replace), path);
        job.waitForCompletion(true);
    }
    
    public static void copyDatafiles(Conf conf, String search, String replace, Collection<Datafile> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        UpdateJob job = new UpdateJob(conf);
        FilePairInputFormat.addDatafiles(job, new ConfigurableModifier(conf, search, replace), path);
        job.waitForCompletion(true);
    }
}
