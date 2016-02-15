package io.github.htools.hadoop.io.backup;

import io.github.htools.hadoop.Conf;
import io.github.htools.hadoop.io.FilePairInputFormat;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static io.github.htools.hadoop.io.backup.BackupJob.fileList;

/**
 * @author jeroen
 */
public class RestoreJob extends CopyJob {

    private static final Log log = new Log(RestoreJob.class);
    
    private RestoreJob(Conf conf) throws IOException {
        super(conf, RestoreMap.class);   
    }
    
    public static void main(String[] args) throws Exception {

        Conf conf = new Conf(args, "-s source -d dest");
        conf.setQueue("express");
        HDFSPath in = conf.getHDFSPath("source");
        HDFSPath out = conf.getHDFSPath("dest");
        restore(conf, in, out);
    }
    
    public static void restore(Conf conf, HDFSPath source, HDFSPath dest) throws IOException, ClassNotFoundException, InterruptedException {
        ArrayList<Datafile> fileList = fileList(new ArrayList(), source);
        if (fileList.size() > 0) {
           RestoreJob.copyDatafiles(conf, source.getCanonicalPath(), dest.getCanonicalPath(), fileList);
        }
        HDFSPath.removeNonExisting(source, dest);
    }    
    
    public static boolean copyPaths(Conf conf, Class<? extends PathModifier> modifier, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        RestoreJob rj = new RestoreJob(conf);
        FilePairInputFormat.addPaths(rj, CopyJob.getModifier(conf, modifier), path);
        return rj.waitForCompletion(true);
    }
    
    public static boolean copyPaths(Conf conf, String search, String replace, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        RestoreJob job = new RestoreJob(conf);
        FilePairInputFormat.addPaths(job, new ConfigurableModifier(conf, search, replace), path);
        return job.waitForCompletion(true);
    }
    
    public static boolean copyDatafiles(Conf conf, String search, String replace, Collection<Datafile> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        RestoreJob job = new RestoreJob(conf);
        FilePairInputFormat.addDatafiles(job, new ConfigurableModifier(conf, search, replace), path);
        return job.waitForCompletion(true);
    }
    
}
