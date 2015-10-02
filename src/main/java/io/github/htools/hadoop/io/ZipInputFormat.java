package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.zip.ZipEntry;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 *
 * @author Jeroen
 */
class ZipInputFormat extends FileInputFormat<ZipEntry, byte[]> {

    public static Log log = new Log(ZipInputFormat.class);

    @Override
    protected boolean isSplitable(JobContext context, Path filename) {
        return false;
    }

    @Override
    public RecordReader<ZipEntry, byte[]> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        return new ZipRecordReader(split, context);
    }

}
