package io.github.repir.tools.hadoop;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import io.github.repir.tools.Structure.StructuredRecordFile;
import java.io.IOException;
import java.lang.reflect.Constructor;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileAlreadyExistsException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * @author jeroen
 */
public abstract class OutputFormat<F extends StructuredRecordFile, V extends Writable> extends TextOutputFormat<NullWritable, V> {

    public static Log log = new Log(OutputFormat.class);
    private final Class fileclass;
    private static String SINGLEFILE = "structuredoutputformat.singlefile";

    public OutputFormat(Class fileclass) {
        this.fileclass = fileclass;
    }

    public F getFile(Datafile datafile) {
        Constructor constructor = ClassTools.getAssignableConstructor(fileclass, StructuredRecordFile.class, Datafile.class);
        return (F) ClassTools.construct(constructor, datafile);
    }

    public Datafile getDatafile(TaskAttemptContext context) throws IOException {
        return getDatafile(context, context.getConfiguration().get("mapreduce.output.fileoutputformat.outputdir"));
    }

    public static Datafile getDatafile(TaskAttemptContext context, String folder) throws IOException {
        Configuration conf = context.getConfiguration();
        FileSystem fs = HDFSDir.getFS(conf);
        String filename = conf.get(SINGLEFILE);
        if (filename == null || context.getNumReduceTasks() > 1) {
            int partition = conf.getInt("mapreduce.task.partition", -1);
            filename = PrintTools.sprintf("%s/%s.%05d", folder, "part", partition);
        }
        return new Datafile(fs, filename);
    }

    @Override
    public RecordWriter<NullWritable, V> getRecordWriter(TaskAttemptContext tac) throws IOException, InterruptedException {
        F file = getFile(getDatafile(tac));
        return new StructuredRecordWriter(file);
    }
    
    public static void setSingleOutput(Job job, Path file) throws IOException {
        job.getConfiguration().set(SINGLEFILE, file.toString());
        TextOutputFormat.setOutputPath(job, HDFSDir.getDir(file));
    }

    @Override
    public void checkOutputSpecs(JobContext job) throws FileAlreadyExistsException, IOException {
        // Ensure that the output directory is set and not already there
        if (job.getConfiguration().get(SINGLEFILE, null) == null) {
            super.checkOutputSpecs(job);
        }
    }
    
    public class StructuredRecordWriter<NullWritable, V extends Writable> extends RecordWriter<NullWritable, V> {

        F fsout;

        public StructuredRecordWriter(F file) {
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
