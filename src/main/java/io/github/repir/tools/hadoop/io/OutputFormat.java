package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.ClassTools;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import io.github.repir.tools.io.struct.StructuredFileRecord;
import io.github.repir.tools.io.struct.StructuredRecordFile;
import io.github.repir.tools.hadoop.ContextTools;
import io.github.repir.tools.hadoop.Job;
import java.io.IOException;
import java.lang.reflect.Constructor;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileAlreadyExistsException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * @author jeroen
 */
public abstract class OutputFormat<F extends StructuredRecordFile, V extends FileWritable>
        extends TextOutputFormat<NullWritable, V> implements Configurable {

    public static Log log = new Log(OutputFormat.class);
    protected static final String OUTPUTDIR = "mapreduce.output.fileoutputformat.outputdir";
    protected static final String SINGLEFILE = "structuredoutputformat.singlefile";
    protected static final String PARTITIONS = "mapreduce.task.partition";
    protected static Class fileclass;
    protected static Class writableclass;
    protected static OutputFormat singleton;
    protected Configuration conf;

    public OutputFormat(Job job, Class fileclass, Class writableclass) {
        this(fileclass, writableclass);
        job.setOutputFormatClass(getClass());
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(writableclass);
        singleton = this;
    }

    public OutputFormat(Class fileclass, Class writableclass) {
        this.fileclass = fileclass;
        this.writableclass = writableclass;
    }

    public static Class getWritableClass() {
        return writableclass;
    }

    public static Class getFileClass() {
        return fileclass;
    }

    public F getFile(Datafile datafile) throws ClassNotFoundException {
        Constructor constructor = ClassTools.getAssignableConstructor(fileclass, StructuredRecordFile.class, Datafile.class);
        return (F) ClassTools.construct(constructor, datafile);
    }

    public static OutputFormat getOutputFormat(Configuration conf) throws ClassNotFoundException {
        if (singleton == null) {
            Class clazz = conf.getClass(Job.OUTPUT_FORMAT_CLASS_ATTR, OutputFormat.class);
            Constructor constructor = ClassTools.getAssignableConstructor(clazz, OutputFormat.class);
            return (OutputFormat) ClassTools.construct(constructor);
        } else {
            return singleton;
        }
    }

    public Datafile getDatafile(TaskAttemptContext context) throws IOException {
        return getDatafile(context, context.getConfiguration().get(OUTPUTDIR));
    }

    public static HDFSPath getLogDir(TaskAttemptContext context) {
        TaskAttemptID taskAttemptID = context.getTaskAttemptID();
        TaskType taskType = taskAttemptID.getTaskType();
        Configuration conf = context.getConfiguration();
        String filename = conf.get(SINGLEFILE);
        if (filename == null) {
            return new HDFSPath(conf, conf.get(OUTPUTDIR)).getSubdir("_log");
        }
        return new HDFSPath(conf, filename).getParentPath().getSubdir("_log_" + filename.substring(filename.lastIndexOf('/') + 1));
    }

    public Datafile getDatafile(TaskAttemptContext context, String folder) throws IOException {
        int task = ContextTools.getTaskID(context);
        Configuration conf = context.getConfiguration();
        FileSystem fs = HDFSPath.getFS(conf);
        String filename = conf.get(SINGLEFILE);
        if (filename == null || context.getNumReduceTasks() > 1) {
            HDFSPath dir = new HDFSPath(conf, folder);
            filename = dir.getFilename(PrintTools.sprintf("%s.%05d", "part", task));
        }
        return new Datafile(fs, filename);
    }

    @Override
    public RecordWriter<NullWritable, V> getRecordWriter(TaskAttemptContext tac) throws IOException, InterruptedException {
        try {
            F file = getFile(getDatafile(tac));
            return new StructuredRecordWriter(file);
        } catch (ClassNotFoundException ex) {
            log.fatalexception(ex, "getRecordWriter(%s %s)", OUTPUTDIR, tac.getConfiguration().get(OUTPUTDIR));
        }
        return null;
    }

    public static void setSingleOutput(Job job, Path file) throws IOException {
        job.getConfiguration().set(SINGLEFILE, file.toString());
        HDFSPath tempDirectorySingle = getTempDirectorySingle(job.getConfiguration());
        if (tempDirectorySingle.exists()) {
            tempDirectorySingle.remove();
        }
        TextOutputFormat.setOutputPath(job, getTempDirectorySingle(job.getConfiguration()));
    }

    public static HDFSPath getTempDirectorySingle(Configuration conf) {
        return new HDFSPath(conf, conf.get(SINGLEFILE) + ".temp");
    }

    @Override
    public void checkOutputSpecs(JobContext job) throws FileAlreadyExistsException, IOException {
        // Ensure that the output directory is set and not already there
        if (!isSingleFile(job.getConfiguration())) {
            super.checkOutputSpecs(job);
        }
    }

    public void cleanupTempDir(Job job) throws IOException {
        if (isSingleFile(job.getConfiguration())) {
            getTempDirectorySingle(job.getConfiguration()).trash();
        }
    }

    static boolean isSingleFile(Configuration conf) {
        return conf.get(SINGLEFILE, null) != null;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    public class StructuredRecordWriter<NullWritable, V extends StructuredFileRecord> extends RecordWriter<NullWritable, V> {

        F fsout;

        public StructuredRecordWriter(F file) {
            log.info("StructuredRecordWriter %s %s", file.getClass().getCanonicalName(), file.getDatafile().getCanonicalPath());
            this.fsout = file;
            fsout.openWrite();
        }

        @Override
        public void write(NullWritable k, V v) throws IOException, InterruptedException {
            fsout.write(v);
        }

        @Override
        public void close(TaskAttemptContext tac) throws IOException, InterruptedException {
            fsout.closeWrite();
            if (fsout.getLength() == 0) {
                fsout.delete();
            }
        }

    }

}
