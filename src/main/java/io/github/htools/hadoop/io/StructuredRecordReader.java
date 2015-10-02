package io.github.htools.hadoop.io;

import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredRecordFile;
import io.github.htools.lib.ClassTools;
import java.io.IOException;
import java.lang.reflect.Constructor;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
/**
 *
 * @author jeroen
 */
public abstract class StructuredRecordReader<F extends StructuredRecordFile, K, W extends org.apache.hadoop.io.Writable> extends org.apache.hadoop.mapreduce.RecordReader<K, W> {
    protected TaskAttemptContext context;
    protected Class fileclass;
    protected long start;
    protected long end;
    protected F structuredRecordFile;
    protected K key;
    protected W record;
    protected FileSystem filesystem;
    protected org.apache.hadoop.conf.Configuration conf;

    public StructuredRecordReader(Class fileclass) {
        this.fileclass = fileclass;
    }
    
    @Override
    public void initialize(InputSplit is, TaskAttemptContext tac) {
        context = tac;
        initialize(is, tac.getConfiguration());
    }

    protected F getFile(Datafile datafile) throws ClassNotFoundException {
        Constructor constructor = ClassTools.getAssignableConstructor(fileclass, StructuredRecordFile.class, Datafile.class);
        return (F) ClassTools.construct(constructor, datafile);
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
            structuredRecordFile.setBufferSize(FileInputFormat.getBufferSize(conf));
            structuredRecordFile.openRead();
            structuredRecordFile.findFirstRecord();
            structuredRecordFile.setCeiling(end);
            FileInputFormat.log.info("RecordReader start %d end %d buffer %d path %s", start, end, structuredRecordFile.getDatafile().getBufferSize(), file.toString());
        } catch (IOException ex) {
            FileInputFormat.log.exception(ex, "initialize( %s ) conf %s filesystem %s fsin %s", is, conf, filesystem, structuredRecordFile);
        } catch (ClassNotFoundException ex) {
            FileInputFormat.log.exception(ex, "initialize( %s ) conf %s filesystem %s fsin %s", is, conf, filesystem, structuredRecordFile);
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
        //log.info("nextKeyValue() hasMore %b", structuredRecordFile.hasMore());
        if (structuredRecordFile.hasMore()) {
            //log.info("nextKeyValue() offset %d ceiling %d",
            //        structuredRecordFile.getOffset(), structuredRecordFile.getCeiling());
            if (structuredRecordFile.getOffset() <= structuredRecordFile.getCeiling()) {
                if (structuredRecordFile.nextRecord()) {
                    key = nextKey();
                    //log.info("nextKeyValue() nextRecord %b", true);
                    record = (W) structuredRecordFile.readRecord();
                    //log.info("new offset %d", structuredRecordFile.getOffset());
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract K nextKey();
    
    @Override
    public final K getCurrentKey() throws IOException, InterruptedException {
        return key;
    }
    
    
    @Override
    public final W getCurrentValue() throws IOException, InterruptedException {
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
