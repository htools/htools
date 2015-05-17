package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.Job;
import io.github.repir.tools.lib.ClassTools;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;

/**
 * @author jeroen
 */
public abstract class FileInputFormat<K, V>
        extends org.apache.hadoop.mapreduce.lib.input.FileInputFormat<K, V> {

    public static Log log = new Log(FileInputFormat.class);
    public static org.apache.hadoop.mapreduce.InputFormat singleton;
    private static final String SPLITABLE = "structuredinputformat.issplitable";
    protected final Class fileclass;

    public FileInputFormat(Class fileclass) {
        this.fileclass = fileclass;
    }

    public static org.apache.hadoop.mapreduce.InputFormat getInputFormat(Job job) {
        if (singleton == null) {
            try {
                Constructor cons = ClassTools.getAssignableConstructor(
                        job.getInputFormatClass(), org.apache.hadoop.mapreduce.InputFormat.class);
                singleton = (org.apache.hadoop.mapreduce.InputFormat)ClassTools.construct(cons);
            } catch (ClassNotFoundException ex) {
                log.fatalexception(ex, "getInputFormat() InputFormat either not set or not constructable");
            }
        }
        return singleton;
    }
    
    public static void setNonSplitable(Job job) {
        job.getConfiguration().setBoolean(SPLITABLE, false);
        job.getConfiguration().setLong("mapreduce.input.fileinputformat.split.minsize", Long.MAX_VALUE);
    }

    /**
     * Recursively adds all files in the given path. Files that start with _ are
     * omitted.
     *
     * @param job
     * @param dir
     * @throws IOException
     */
    public static void addDirs(Job job, String dir) throws IOException {
        ArrayList<String> list = getDirList(job.getFS(), dir);
        for (String d : list)
            FileInputFormat.addInputPath(job, new Path(d));
    }

    public static ArrayList<String> getDirList(FileSystem fs, String dir) throws IOException {
        ArrayList<String> list = new ArrayList();
        if (dir.length() > 0) {
            if (dir.contains(",")) {
                for (String d : dir.split(",")) {
                    getDirList(list, new HDFSPath(fs, d));
                }
            } else {
                getDirList(list, new HDFSPath(fs, dir));
            }
        }
        return list;
    }

    protected static void getDirList(ArrayList<String> list, HDFSPath d) throws IOException {
        if (!d.getName().startsWith("_")) {
            if (d.isFile()) {
                list.add(d.getCanonicalPath());
            } else {
                for (String f : d.getFilepathnames()) {
                    Path path = new Path(f);
                    if (!path.getName().startsWith("_")) {
                        list.add(f);
                    }
                }
                for (HDFSPath f : d.getDirs()) {
                    getDirList(list, f);
                }
            }
        }
    }

    public static void addDirs(Job job, ArrayList<String> dirs) throws IOException {
        for (String dir : dirs) {
            addDirs(job, dir);
        }
    }

    public static void addFile(Job job, Path path) {
        try {
            addInputPath(job, path);
        } catch (IOException ex) {
            log.exception(ex, "add( %s, %s )", job, path);
        }
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return context.getConfiguration().getBoolean(SPLITABLE, true);
    }
}
