package io.github.htools.hadoop.io;

import io.github.htools.hadoop.Job;
import io.github.htools.io.HDFSIn;
import io.github.htools.io.HDFSPath;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.buffer.BufferSerializable;
import io.github.htools.io.struct.StructuredRecordFile;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * @author jeroen
 */
public abstract class ParamFileInputFormat<F extends StructuredRecordFile, K extends BufferSerializable, V extends org.apache.hadoop.io.Writable>
        extends FileClassInputFormat<K, V> {

    public static List<InputSplit> splits = new ArrayList();

    public ParamFileInputFormat(Class fileclass) {
        super(fileclass);
    }

    protected abstract K createKey();
    
    public static <K extends BufferSerializable> void add(Job job, K key, Path path) throws IOException {
        log.info("add(%s)", path.toString());
        FileSystem fs = job.getFileSystem();
        FileStatus file = fs.getFileStatus(path);
        path = file.getPath();
        long length = file.getLen();
        if (length != 0) {
            BlockLocation[] blkLocations;
            if (file instanceof LocatedFileStatus) {
                blkLocations = ((LocatedFileStatus) file).getBlockLocations();
            } else {
                blkLocations = fs.getFileBlockLocations(file, 0, length);
            }
            splits.add(new Split(path, 0, length, blkLocations[0].getHosts(), key));
        } else {
            //Create empty hosts array for zero length files
            splits.add(new Split(path, 0, length, new String[0], key));
        }
    }

    public List<InputSplit> getSplits(JobContext tac) throws IOException {
        return splits;
    }

    @Override
    public RecordReader<K, V> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        return new MyRecordReader(fileclass, createKey());
    }

    class MyRecordReader extends StructuredRecordReader<F, K, V> {

        K params;

        protected MyRecordReader(Class fileclass, K key) {
            super(fileclass);
            params = key;
        }

        @Override
        public void initialize(InputSplit is, TaskAttemptContext tac) {
            super.initialize(is, tac);
            ((Split<K>) is).getParams(params);
        }

        @Override
        protected K nextKey() {
            return params;
        }
    }

    static class Split<K extends BufferSerializable> extends FileSplit {

        private BufferReaderWriter reader;
        private K params;

        public Split() {
            super();
        }

        public Split(Path path, long start, long length, String[] locations, String[] cachedlocations, K params) throws IOException {
            super(path, start, length, locations);
            this.params = params;
        }

        public Split(Path path, long start, long length, String[] locations, K params) throws IOException {
            super(path, start, length, locations);
            this.params = params;
        }

        @Override
        public void write(DataOutput out) throws IOException {
            super.write(out);
            BufferDelayedWriter writer = new BufferDelayedWriter();
            params.write(writer);
            writer.writeBuffer(out);
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            super.readFields(in);
            reader = new BufferReaderWriter(in);
        }

        public void getParams(K params) {
            params.read(reader);
        }

    }
}
