package io.github.htools.hadoop.io;

import io.github.htools.hadoop.FileFilter;
import io.github.htools.hadoop.Job;
import io.github.htools.io.DirComponent;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jeroen
 */
public abstract class FileInputFormat<K, V>
        extends org.apache.hadoop.mapreduce.lib.input.FileInputFormat<K, V> {

    public static Log log = new Log(FileInputFormat.class);
    public static org.apache.hadoop.mapreduce.InputFormat singleton;
    public static final String TESTINPUT = FileInputFormat.class.getCanonicalName().toLowerCase() + ".testinput";
    private static final String SPLITABLE = FileInputFormat.class.getCanonicalName().toLowerCase() + ".issplitable";
    public static final String BUFFERSIZE = FileInputFormat.class.getCanonicalName().toLowerCase() + ".buffersize";
    public static FileFilter fileFilter;
    public static FileFilter splitableFiles = new SplitableFiles();

    public static void setBufferSize(Configuration conf, int buffersize) {
        conf.setInt(BUFFERSIZE, buffersize);
    }

    public static int getBufferSize(Configuration conf) {
        return conf.getInt(BUFFERSIZE, 10000000);
    }

    public static void setTest(Configuration conf) {
        conf.setBoolean(TESTINPUT, true);
    }

    public static void setFilter(FileFilter filter) {
        fileFilter = filter;
    }

    public static org.apache.hadoop.mapreduce.InputFormat getInputFormat(Job job) {
        if (singleton == null) {
            try {
                Constructor cons = ClassTools.getAssignableConstructor(
                        job.getInputFormatClass(), org.apache.hadoop.mapreduce.InputFormat.class);
                singleton = (org.apache.hadoop.mapreduce.InputFormat) ClassTools.construct(cons);
            } catch (ClassNotFoundException ex) {
                log.fatalexception(ex, "getInputFormat() InputFormat either not set or not constructable");
            }
        }
        return singleton;
    }

    @Override
    public List<InputSplit> getSplits(JobContext job) throws IOException {
        boolean test = job.getConfiguration().getBoolean(TESTINPUT, false);
        List<InputSplit> splits = super.getSplits(job);
        if (test) {
            ArrayList<InputSplit> single = new ArrayList();
            single.add(splits.get(0));
            return single;
        }
        return splits;
    }

    /**
     * Ensures that input files are not split
     */
    public static void setNonSplitable(Job job) {
        job.getConfiguration().setBoolean(SPLITABLE, false);
        //job.getConfiguration().setLong("mapreduce.input.fileinputformat.split.minsize", Long.MAX_VALUE);
    }

    @Override
    protected boolean isSplitable(JobContext context, Path filename) {
        //log.info("split %s %b", filename, context.getConfiguration().getBoolean(SPLITABLE, true) && splitableFiles.acceptFile(filename));
        return context.getConfiguration().getBoolean(SPLITABLE, true) && splitableFiles.acceptFile(filename);
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
        ArrayList<String> list = getDirList(job.getFileSystem(), dir);
        for (String d : list) {
            FileInputFormat.addInputPath(job, new Path(d));
        }
    }

    @Override
    protected List<FileStatus> listStatus(JobContext job) throws IOException {
        List<FileStatus> list = super.listStatus(job);
        if (fileFilter != null) {
            Iterator<FileStatus> iter = list.iterator();
            while (iter.hasNext()) {
                FileStatus file = iter.next();
                if (file.isFile() && !fileFilter.acceptFile(file.getPath())) {
                    iter.remove();
                }
            }
        }
        return list;
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
            if (d.existsFile()) {
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

    public static void addInputPath(Job job, Iterator<DirComponent> iter) throws IOException {
        ArrayList<Path> paths = new ArrayList();
        while (iter.hasNext()) {
            DirComponent d = iter.next();
            addInputPath(job, new Path(d.getCanonicalPath()));
        }
    }

    protected static class SplitableFiles extends FileFilter {

        public SplitableFiles() {
            this.setInvalidFileNameEnd(".tar", ".tar.lz4", ".lz4");
        }
    }
}
