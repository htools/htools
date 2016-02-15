package io.github.htools.hadoop;

import io.github.htools.io.Datafile;
import io.github.htools.io.DirComponent;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * InputFormat extends FileInputFormat to supply Hadoop with the input to
 * process.
 *
 * @author jeroen
 */
public abstract class InputFormat<W> extends FileInputFormat<LongWritable, W> {

    public static Log log = new Log(InputFormat.class);
    private static final String SPLITABLE = "structuredinputformat.issplitable";
    static FileFilter filefilter;

    public static void addDirs(Job job, String dir) throws IOException {
        HDFSPath path = new HDFSPath(job.getConfiguration(), dir);
        addDirs(job, path);
    }

    public static void addDirs(Job job, HDFSPath parentpath) throws IOException {
        for (DirComponent d : parentpath.wildcardIterator()) {
            if (d instanceof Datafile) {
                addFile(job, new Path(d.getCanonicalPath()));
            } else {
                HDFSPath path = (HDFSPath) d;
                for (String f : path.getFilepathnames()) {
                    addFile(job, new Path(f));
                }
                for (HDFSPath f : path.getDirs()) {
                    addDirs(job, f);
                }
            }
        }
    }

    public static void setNonSplitable(Job job) {
        setNonSplitable(job.getConfiguration());
    }

    public static long getSplitSize(Job job) {
        return getSplitSize(job.getConfiguration());
    }

    public static void setNonSplitable(Configuration conf) {
        setSplitSize(conf, Long.MAX_VALUE);
    }

    public static void setSplitSize(Configuration conf, long size) {
        conf.setBoolean(SPLITABLE, size < Long.MAX_VALUE);
        conf.setLong("mapreduce.input.fileinputformat.split.minsize", size);
    }

    public static long getSplitSize(Configuration conf) {
        if (conf.getBoolean(SPLITABLE, false)) {
            return conf.getLong("mapreduce.input.fileinputformat.split.minsize", Long.MAX_VALUE);
        }
        return Long.MAX_VALUE;
    }

    public static void addFileList(Job job, String file) throws IOException {
        Datafile df = new Datafile(job.getConfiguration(), file);
        String contents = df.readAsString();
        String lines[] = contents.split(" ");
        for (String line : lines) {
            int space = line.indexOf(' ');
            if (space > 0) {
                line = line.substring(0, space);
            }
            addFile(job, new Path(line));
        }
    }

    /**
     * @param job
     * @param list
     * @return a list of Datafiles of maximum SplitSize length, by splitting 
     * longer Datafiles into several files with offset and ceiling set to
     * cover all data in disjoint files.
     */
    public static ArrayList<Datafile> split(Job job, ArrayList<Datafile> list) {
        long splitsize = getSplitSize(job);
        ArrayList<Datafile> result = new ArrayList();
        for (Datafile d : list) {
            if (d.getLength() > splitsize) {
                int parts = 1 + (int) (d.getLength() / splitsize);
                long partlength = d.getLength() / parts;
                for (int i = 0; i < parts; i++) {
                    Datafile partfile = new Datafile(d);
                    partfile.setOffset(i * partlength);
                    partfile.setCeiling(Math.min(d.getLength(), (i + 1) * partlength));
                    result.add(partfile);
                }
            } else {
                result.add(d);
            }
        }
        return result;
    }    
    
    public static ArrayList<String> topDirs(Configuration conf) throws IOException {
        Path[] inputPaths = getInputPaths(conf);
        HashSet<String> dirs = new HashSet();
        for (Path p : inputPaths) {
            String toString = p.getParent().toString();
            dirs.add(toString);
        }
        return new ArrayList(dirs);
    }

    public static Path[] getInputPaths(Configuration conf) {
        String dirs = conf.get(INPUT_DIR, "");
        String[] list = StringUtils.split(dirs);
        Path[] result = new Path[list.length];
        for (int i = 0; i < list.length; i++) {
            result[i] = new Path(StringUtils.unEscapeString(list[i]));
        }
        return result;
    }

    public static void setFileFilter(FileFilter filter) {
        filefilter = filter;
    }

    public static void addFile(Job job, Path path) {
        try {
            if (filefilter == null || filefilter.acceptFile(path)) {
                addInputPath(job, path);
            }
        } catch (IOException ex) {
            log.exception(ex, "add( %s, %s )", job, path);
        }
    }

    @Override
    public List<InputSplit> getSplits(JobContext job) throws IOException {
        return super.getSplits(job);
    }

    @Override
    public abstract RecordReader<LongWritable, W> createRecordReader(InputSplit is, TaskAttemptContext tac);

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return context.getConfiguration().getBoolean(SPLITABLE, true);
    }
}
