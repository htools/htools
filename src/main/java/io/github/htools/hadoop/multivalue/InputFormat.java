package io.github.htools.hadoop.multivalue;

import io.github.htools.hadoop.io.buffered.DelayedWritable;
import io.github.htools.search.ByteSearch;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import static io.github.htools.lib.PrintTools.sprintf;
import io.github.htools.io.struct.StructuredRecordFile;
import io.github.htools.type.Tuple2;
import io.github.htools.hadoop.io.MultiReduceWritable;
import io.github.htools.hadoop.Job;
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
import org.apache.hadoop.conf.Configuration;

/**
 * @author jeroen
 */
public class InputFormat extends FileInputFormat<LongWritable, DelayedWritable> {

    public static Log log = new Log(InputFormat.class);
    private static final String SPLITABLE = "structuredinputformat.issplitable";
    private static final String CLASSPATTERN = "structuredinputformat.classpattern";
    public static final String CLASSCLASS = "structuredinputformat.class";
    private static ArrayList<Tuple2<ByteSearch, Class>> alternativefileclass;
    private Job job;

    public InputFormat() {}
    
    public InputFormat(Job job) {
        this.job = job;
        job.setInputFormatClass(getClass());
    }

    public void add(String pattern, Class fileclass, Class writableclass) {
        String[] patterns = job.getConfiguration().getStrings(CLASSPATTERN, new String[0]);
        String[] fileclasses = job.getConfiguration().getStrings(CLASSCLASS, new String[0]);
        String[] writableclasses = job.getConfiguration().getStrings(MultiReduceWritable.CLASSCLASS, new String[0]);
        job.getConfiguration().setStrings(CLASSPATTERN, ArrayTools.addArr(patterns, pattern));
        job.getConfiguration().setStrings(CLASSCLASS, ArrayTools.addArr(fileclasses, fileclass.getCanonicalName()));
        job.getConfiguration().setStrings(MultiReduceWritable.CLASSCLASS, ArrayTools.addArr(writableclasses, writableclass.getCanonicalName()));
    }

    public static ArrayList<Tuple2<ByteSearch, Class>> getFileClasses(Configuration conf) throws ClassNotFoundException {
        if (alternativefileclass == null) {
            alternativefileclass = new ArrayList();
            String[] patterns = conf.getStrings(CLASSPATTERN);
            String[] classes = conf.getStrings(CLASSCLASS);
            for (int i = 0; i < patterns.length; i++) {
                alternativefileclass.add(new Tuple2(ByteSearch.create(patterns[i]), Class.forName(classes[i])));
            }
        }
        return alternativefileclass;
    }

    public static void setNonSplitable(Job job) {
        job.getConfiguration().setBoolean(SPLITABLE, false);
    }

    public static void addDirs(Job job, String dir) throws IOException {
        FileSystem fs = HDFSPath.getFS(job.getConfiguration());
        ArrayList<HDFSPath> paths = new ArrayList<HDFSPath>();
        ArrayList<Path> files = new ArrayList<Path>();
        if (dir.length() > 0) {
            HDFSPath d = new HDFSPath(fs, dir);
            if (d.isFile()) {
                addFile(job, new Path(dir));
            } else {
                for (String f : d.getFilepathnames()) {
                    addFile(job, new Path(f));
                }
                for (HDFSPath f : d.getDirs()) {
                    addDirs(job, f.getCanonicalPath());
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

    private static Class getFileClass(Configuration conf, Datafile datafile) throws ClassNotFoundException {
        for (Tuple2<ByteSearch, Class> entry : getFileClasses(conf)) {
            if (entry.key.exists(datafile.getCanonicalPath())) {
                return entry.value;
            }
        }
        throw new RuntimeException(sprintf("File is not matched by any pattern: %s", datafile.getCanonicalPath()));
    }

    @Override
    public RecordReader<LongWritable, DelayedWritable> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        return new StructuredRecordReader();
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return context.getConfiguration().getBoolean(SPLITABLE, true);
    }

    protected static StructuredRecordFile getFile(Configuration conf, Datafile datafile) throws ClassNotFoundException {
        return getFile(getFileClass(conf, datafile), datafile);
    }

    protected static StructuredRecordFile getFile(Class clazz, Datafile datafile) throws ClassNotFoundException {
        Constructor constructor = ClassTools.getAssignableConstructor(clazz, StructuredRecordFile.class, Datafile.class);
        return (StructuredRecordFile) ClassTools.construct(constructor, datafile);
    }

    class StructuredRecordReader extends org.apache.hadoop.mapreduce.RecordReader<LongWritable, DelayedWritable> {

        protected TaskAttemptContext context;
        protected long start;
        protected long end;
        protected StructuredRecordFile structuredRecordFile;
        protected LongWritable key = new LongWritable();
        protected DelayedWritable record;
        protected FileSystem filesystem;
        protected org.apache.hadoop.conf.Configuration conf;

        @Override
        public void initialize(InputSplit is, TaskAttemptContext tac) {
            context = tac;
            try {
                initialize(is, tac.getConfiguration());
            } catch (ClassNotFoundException ex) {
                log.fatalexception(ex, "Constructing StructuredRecordReader");
            }
        }

        public void initialize(InputSplit is, org.apache.hadoop.conf.Configuration conf) throws ClassNotFoundException {
            //log.info("initialize");
            try {
                this.conf = conf;
                filesystem = FileSystem.get(conf);
                FileSplit fileSplit = (FileSplit) is;
                Path file = fileSplit.getPath();
                start = fileSplit.getStart();
                end = start + fileSplit.getLength();
                structuredRecordFile = getFile(conf, new Datafile(filesystem, file));
                structuredRecordFile.setOffset(start);
                structuredRecordFile.setBufferSize(10000000);
                structuredRecordFile.openRead();
                structuredRecordFile.findFirstRecord();
                structuredRecordFile.setCeiling(end);
                log.info("StructuredRecordReader %s %s", structuredRecordFile.getClass().getCanonicalName(), structuredRecordFile.getDatafile().getCanonicalPath());
            } catch (IOException ex) {
                log.exception(ex, "initialize( %s ) conf %s filesystem %s fsin %s", is, conf, filesystem, structuredRecordFile);
            }
        }

        /**
         * Reads the input file, scanning for the next document, setting key and
         * entitywritable with the offset and byte contents of the document
         * read.
         * <p>
         * @return true if a next document was read
         */
        @Override
        public boolean nextKeyValue() {
            if (structuredRecordFile.hasMore()) {
                if (structuredRecordFile.getOffset() <= structuredRecordFile.getCeiling()) {
                    key.set(structuredRecordFile.getOffset());
                    if (structuredRecordFile.nextRecord()) {
                        record = (DelayedWritable) structuredRecordFile.readRecord();
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
        public DelayedWritable getCurrentValue() throws IOException, InterruptedException {
            return record;
        }

        /**
         * NB this indicates progress as the data that has been read, for some
         * MapReduce tasks processing the data continues for some startTime,
         * causing the progress indicator to halt at 100%.
         * <p>
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
