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
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * @author jeroen
 */
public abstract class StructuredOutputFormat<F extends StructuredRecordFile, V extends StructuredRecordWritable> extends TextOutputFormat<NullWritable, V> {

    public static Log log = new Log(StructuredOutputFormat.class);
    private final Class fileclass;
    private static String SINGLEFILE = "structuredoutputformat.singlefile";
    
    public StructuredOutputFormat(Class fileclass) {
        this.fileclass = fileclass;
    }
    
    public F getFile(Datafile datafile) {
        Constructor constructor = ClassTools.getAssignableConstructor(fileclass, StructuredRecordFile.class, Datafile.class);
        return (F)ClassTools.construct(constructor, datafile);
    }

    public Datafile getDatafile(TaskAttemptContext context) throws IOException {
        Configuration conf = context.getConfiguration();
        FileSystem fs = HDFSDir.getFS(conf);
        String filename = conf.get(SINGLEFILE);
        if (filename == null || context.getNumReduceTasks() > 1) {
           String name = conf.get("mapred.output.dir");
           int partition = conf.getInt("mapred.task.partition", -1);
           filename = PrintTools.sprintf("%s/%s.%05d", name, "part", partition);
        }
        return new Datafile(fs, filename);
    }

    @Override
    public RecordWriter<NullWritable, V> getRecordWriter(TaskAttemptContext tac) throws IOException, InterruptedException {
        F file = getFile(getDatafile(tac));
        return new StructuredRecordWriter<NullWritable, V>(file);
    }

    public static void setSingleOutput(Job job, String filename) {
        job.getConfiguration().set(SINGLEFILE, filename);
    }
    
    public class StructuredRecordWriter<NullWritable, V extends StructuredRecordWritable> extends RecordWriter<NullWritable, V> {

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
        }
    }

}
