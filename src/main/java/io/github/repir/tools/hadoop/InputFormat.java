package io.github.repir.tools.hadoop;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.DirComponent;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import static org.apache.hadoop.mapreduce.lib.input.FileInputFormat.INPUT_DIR;
import org.apache.hadoop.util.StringUtils;

/**
 * EntityReaderInputFormat extends FileInputFormat to supply Hadoop with the
 * input to process. To use EntityReaderInputFormat, instantiate with {@link #EntityReaderInputFormat(org.apache.hadoop.mapreduce.Job, java.lang.String[])
 * }
 * using an array of paths on the HDFS, that contain the input files to process.
 * The paths can be files or directories, which are scanned recursively for any
 * file. Before adding a file to the list of inputs,
 * {@link #acceptFile(java.lang.String)} is called to check if this file is to
 * be processed. This way the readme, program and .dtd files in the original
 * TREC collections are skipped.
 * <p/>
 * The input is configured by "repository.inputdir", which can be a comma
 * seperated list of folders, or an array, e.g. multiple
 * "+repository.inputdir=...". The dirs are scanned recursively for input files.
 * See {@link FileFilter} if certain files can be included or excluded.
 * <p/>
 * By default, valid files are submitted to an instantiation of the configured
 * "repository.entityreader". Alternatively, different entityreaders can be
 * configured for different file types, by assigning an entity reader for files
 * that end with some extension, e.g. "+repository.assignentityreader=.pdf
 * EntitReaderPDF"
 * <p/>
 * !!Note that Java does not have a way to uncompress .z files, so the .z files
 * on the original TREC disks have to be uncompressed outside this framework.
 * <p/>
 * @author jeroen
 */
public abstract class InputFormat<W> extends FileInputFormat<LongWritable, W> {

    public static Log log = new Log(InputFormat.class);
    private static final String SPLITABLE = "structuredinputformat.issplitable";
    static FileFilter filefilter;

    public static void addDirs(Job job, String dir) throws IOException {
        FileSystem fs = HDFSPath.getFS(job.getConfiguration());
        HDFSPath path = new HDFSPath(fs, dir);
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
        job.getConfiguration().setBoolean(SPLITABLE, false);
        job.getConfiguration().setLong("mapreduce.input.fileinputformat.split.minsize", Long.MAX_VALUE);
    }

    public static void addFileList(Job job, String file) {
        FileSystem fs = HDFSPath.getFS(job.getConfiguration());
        Datafile df = new Datafile(fs, file);
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
