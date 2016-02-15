package io.github.htools.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author jeroen
 */
public abstract class RecordReader<W> extends org.apache.hadoop.mapreduce.RecordReader<LongWritable, W> {

    protected TaskAttemptContext context;
    protected long start;
    protected long end;
    protected InputStream inputStream;
    protected LongWritable key = new LongWritable();
    protected W record;
    protected FileSystem filesystem;
    protected Configuration conf;
    protected boolean empty;
    
    @Override
    public final void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        this.context = context;
        initialize(split, context.getConfiguration());
    }

    public final void initialize(InputSplit is, Configuration conf) throws IOException {
        this.conf = conf;
        filesystem = FileSystem.get(conf);
        FileSplit fileSplit = (FileSplit) is;
        setOffsetEnd(fileSplit);
        initialize(filesystem, fileSplit);
    }

    public static InputStream getDirectInputStream(FileSystem fs, FileSplit fileSplit) throws IOException {
        Path path = fileSplit.getPath();
        InputStream fsdin = fs.open(path, 4096);
        return fsdin;
    }

    public static InputStream getInputStream(FileSystem fs, FileSplit fileSplit) throws IOException {
        InputStream fsdin = getDirectInputStream(fs, fileSplit);

        CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(fs.getConf());
        CompressionCodec codec = compressionCodecs.getCodec(fileSplit.getPath());
        if (codec != null) {
            return codec.createInputStream(fsdin);
        }
        return fsdin;
    }

    public void initialize(FileSystem fs, FileSplit fileSplit) throws IOException {
        inputStream = getInputStream(fs, fileSplit);
    }

    public void setOffsetEnd(FileSplit fileSplit) {
        start = fileSplit.getStart();
        end = start + fileSplit.getLength();
    }
    
    /**
     * Reads the input file, scanning for the next document, setting key and
     * entitywritable with the offset and byte contents of the document read.
     * <p>
     * @return true if a next document was read
     */
    @Override
    public abstract boolean nextKeyValue();

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    @Override
    public W getCurrentValue() throws IOException, InterruptedException {
        return record;
    }

    /**
     * NB this indicates progress as the data that has been read, for some
     * MapReduce tasks processing the data continues for some startTime, causing
     * the progress indicator to halt at 100%.
     * <p>
     * @return @throws IOException
     * @throws InterruptedException
     */
    @Override
    public abstract float getProgress() throws IOException, InterruptedException;

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
