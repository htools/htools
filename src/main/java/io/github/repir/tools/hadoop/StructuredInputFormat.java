package io.github.repir.tools.hadoop;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Structure.StructuredRecordFile;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * @author jeroen
 */
public abstract class StructuredInputFormat<F extends StructuredRecordFile, V extends StructuredRecordWritable> extends FileInputFormat<LongWritable, V> {

    public static Log log = new Log(StructuredInputFormat.class);
    private static final String SPLITABLE = "structuredinputformat.issplitable";
    private final Class fileclass;

    public StructuredInputFormat(Class fileclass) {
        this.fileclass = fileclass;
    }
    
    public static void setNonSplitable(Job job) {
        job.getConfiguration().setBoolean(SPLITABLE, false);
    }

    public static void addDirs(Job job, String dir) {
        FileSystem fs = HDFSDir.getFS(job.getConfiguration());
        ArrayList<HDFSDir> paths = new ArrayList<HDFSDir>();
        ArrayList<Path> files = new ArrayList<Path>();
        if (dir.length() > 0) {
            HDFSDir d = new HDFSDir(fs, dir);
            if (d.isFile()) {
                addFile(job, new Path(dir));
            } else {
                for (Path f : d.getFiles()) {
                    addFile(job, f);
                }
                for (HDFSDir f : d.getSubDirs()) {
                    addDirs(job, f.getCanonicalPath());
                }
            }
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
    public RecordReader<LongWritable, V> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        return new StructuredRecordReader();
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return context.getConfiguration().getBoolean(SPLITABLE, true);
    }

    protected F getFile(Datafile datafile) {
        Constructor constructor = ClassTools.getAssignableConstructor(fileclass, StructuredRecordFile.class, Datafile.class);
        return (F) ClassTools.construct(constructor, datafile);
    }

    class StructuredRecordReader extends org.apache.hadoop.mapreduce.RecordReader<LongWritable, V> {

        protected TaskAttemptContext context;
        protected long start;
        protected long end;
        protected F structuredRecordFile;
        protected LongWritable key = new LongWritable();
        protected V record;
        protected FileSystem filesystem;
        protected org.apache.hadoop.conf.Configuration conf;

        @Override
        public void initialize(InputSplit is, TaskAttemptContext tac) {
            context = tac;
            initialize(is, tac.getConfiguration());
        }

        public void initialize(InputSplit is, org.apache.hadoop.conf.Configuration conf) {
            //log.info("initialize");
            try {
                this.conf = conf;
                filesystem = FileSystem.get(conf);
                FileSplit fileSplit = (FileSplit) is;
                Path file = fileSplit.getPath();
                start = fileSplit.getStart();
                end = start + fileSplit.getLength();
                structuredRecordFile = getFile(new Datafile(filesystem, file));
                structuredRecordFile.setOffset(start);
                structuredRecordFile.setBufferSize(10000000);
                structuredRecordFile.openRead();
                structuredRecordFile.findFirstRecord();
                structuredRecordFile.setCeiling(end);
            } catch (IOException ex) {
                log.exception(ex, "initialize( %s ) conf %s filesystem %s fsin %s", is, conf, filesystem, structuredRecordFile);
            }
        }

        /**
         * Reads the input file, scanning for the next document, setting key and
         * entitywritable with the offset and byte contents of the document
         * read.
         * <p/>
         * @return true if a next document was read
         */
        @Override
        public boolean nextKeyValue() {
            if (structuredRecordFile.hasMore()) {
                if (structuredRecordFile.getOffset() <= structuredRecordFile.getCeiling()) {
                    key.set(structuredRecordFile.getOffset());
                    if (structuredRecordFile.nextRecord()) {
                        record = (V) structuredRecordFile.readRecord();
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public LongWritable getCurrentKey() throws IOException, InterruptedException {
            return key;
        }

        @Override
        public V getCurrentValue() throws IOException, InterruptedException {
            return record;
        }

        /**
         * NB this indicates progress as the data that has been read, for some
         * MapReduce tasks processing the data continues for some startTime,
         * causing the progress indicator to halt at 100%.
         * <p/>
         * @return @throws IOException
         * @throws InterruptedException
         */
        @Override
        public float getProgress() throws IOException, InterruptedException {
            return (structuredRecordFile.getOffset() - start) / (float) (end - start);
        }

        @Override
        public void close() throws IOException {
            structuredRecordFile.closeRead();
        }
    }
}
