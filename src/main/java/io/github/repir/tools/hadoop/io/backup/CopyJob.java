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
public class CopyJob extends Job {

    private static final Log log = new Log(CopyJob.class);
    
    protected CopyJob(Conf conf) throws IOException {
        this(conf, CopyMap.class);
    }
    
    protected CopyJob(Conf conf, Class mapclass) throws IOException {
        super(conf);
        setInputFormatClass(FilePairInputFormat.class);
        setNumReduceTasks(0);
        setMapperClass(mapclass);
        setOutputFormatClass(NullOutputFormat.class);
    }
    
    public static void copy(Conf conf, Class<? extends PathModifier> modifier, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        CopyJob job = new CopyJob(conf);
        FilePairInputFormat.addPaths(job, getModifier(conf, modifier), path);
        job.waitForCompletion(true);
    }
    
    protected static PathModifier getModifier(Configuration conf, Class<? extends PathModifier> modifier) throws ClassNotFoundException {
        Constructor<PathModifier> constructor = ClassTools.getAssignableConstructor(modifier, PathModifier.class);
        PathModifier m = ClassTools.construct(constructor);
        m.setConf(conf);
        return m;
    }
    
    public static void copyPath(Conf conf, String search, String replace, Collection<HDFSPath> path) throws IOException, ClassNotFoundException, InterruptedException {
        ConfigurableModifier.setSearchReplace(conf, search, replace);
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        CopyJob job = new CopyJob(conf);
        FilePairInputFormat.addPaths(job, new ConfigurableModifier(), path);
        job.waitForCompletion(true);
    }
    
    public static void copyDatafiles(Conf conf, String search, String replace, Collection<Datafile> path) throws IOException, ClassNotFoundException, InterruptedException {
        ConfigurableModifier.setSearchReplace(conf, search, replace);
        conf.setMapMemoryMB(1024);
        conf.setMapSpeculativeExecution(false);
        CopyJob job = new CopyJob(conf);
        FilePairInputFormat.addDatafiles(job, new ConfigurableModifier(), path);
        job.waitForCompletion(true);
    }
}
