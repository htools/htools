package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.Job;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;

/**
 * @author jeroen
 */
public abstract class FileInputFormat<K, V>
        extends org.apache.hadoop.mapreduce.lib.input.FileInputFormat<K, V> {

    public static Log log = new Log(FileInputFormat.class);
    private static final String SPLITABLE = "structuredinputformat.issplitable";
    protected final Class fileclass;

    public FileInputFormat(Class fileclass) {
        this.fileclass = fileclass;
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
        FileSystem fs = HDFSPath.getFS(job.getConfiguration());
        ArrayList<HDFSPath> paths = new ArrayList<HDFSPath>();
        ArrayList<Path> files = new ArrayList<Path>();
        if (dir.length() > 0) {
            if (dir.contains(",")) {
                for (String d : dir.split(",")) {
                    addDirs(job, d);
                }
            } else {
                HDFSPath d = new HDFSPath(fs, dir);
                if (!d.getName().startsWith("_")) {
                    if (d.isFile()) {
                        addFile(job, new Path(dir));
                    } else {
                        for (String f : d.getFilepathnames()) {
                            Path path = new Path(f);
                            if (!path.getName().startsWith("_")) {
                                addFile(job, path);
                            }
                        }
                        for (HDFSPath f : d.getDirs()) {
                            addDirs(job, f.getCanonicalPath());
                        }
                    }
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
