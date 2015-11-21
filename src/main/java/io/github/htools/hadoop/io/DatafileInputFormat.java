package io.github.htools.hadoop.io;

import io.github.htools.hadoop.InputFormat;
import static io.github.htools.hadoop.io.FileInputFormat.TESTINPUT;
import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class DatafileInputFormat extends FileInputFormat<Long, Datafile> {

    public static Log log = new Log(DatafileInputFormat.class);

    @Override
    public RecordReader<Long, Datafile> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        return new Records();
    }

    public class Records extends RecordReader<Long, Datafile> {

        Datafile df;
        float progress;

        @Override
        public void initialize(InputSplit is, TaskAttemptContext tac) {
            initialize(is, tac.getConfiguration());
        }

        public final void initialize(InputSplit is, Configuration conf) {
            //log.info("initialize");
            FileSplit fileSplit = (FileSplit) is;
            Path file = fileSplit.getPath();
            long start = fileSplit.getStart();
            long end = fileSplit.getStart() + fileSplit.getLength();
            df = new Datafile(conf, file.toString());
            df.setOffset(start);
            df.setCeiling(end);
            progress = df.getLength() / 2;
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
            return df != null;
        }

        @Override
        public Long getCurrentKey() {
            return df.getOffset();
        }

        @Override
        public Datafile getCurrentValue() {
            Datafile result = df;
            df = null;
            return result;
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
        public float getProgress() {
            return (df == null) ? progress : 0;
        }

        @Override
        public void close() {
        }
    }

}
