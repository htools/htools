package io.github.htools.hadoop.document;

import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

/**
 * A document reader read an input file, identifying document markers to store
 * one document at a startTime in a BytesWritable, that is used in a map()
 * process. The LongWritable that is passed along indicates the offset in the
 * input file, which can be used to trace problems.
 * <p>
 * Note, Hadoop can split uncompressed files, to divide the work between
 * mappers. These splits are likely to place an offset inside a document. The
 * desired cause of action if that the mapper who starts reading a document that
 * encounters the InputSplit's ceiling, keeps reading past the ceiling (you can,
 * the ceiling is just an indicator). The other Mapper starts at the designated
 * offset and searches from that point until the first start of document tag.
 * This way no documents are processed twice or get lost.
 * <p>
 * EntityReader is used internally by {@link ReaderInputFormat}, to read
 * the next entity from a source archive, for processing by a Mapper.
 *
 * @author jeroen
 */
public abstract class DocumentAbstractReader extends RecordReader<LongWritable, byte[]> {

    public static Log log = new Log(DocumentAbstractReader.class);
    private TaskAttemptContext context;
    private long start;
    private long end;
    private Datafile fsin;
    private LongWritable key = new LongWritable();
    private byte[] document;
    private FileSystem filesystem;
    private Configuration conf;

    @Override
    public final void initialize(InputSplit is, TaskAttemptContext tac) {
        context = tac;
        try {
            this.conf = tac.getConfiguration();
            filesystem = FileSystem.get(conf);
            FileSplit fileSplit = (FileSplit) is;
            Path file = fileSplit.getPath();
            start = fileSplit.getStart();
            end = start + fileSplit.getLength();
            fsin = new Datafile(filesystem, file);
            fsin.setOffset(start);
            fsin.setBufferSize(10000000);
            fsin.openRead();
            initialize(fileSplit);
        } catch (IOException ex) {
            log.exception(ex, "initialize( %s ) conf %s filesystem %s fsin %s", is, conf, filesystem, fsin);
        }
    }

    protected Configuration getConfiguration() {
        return conf;
    }
    
    protected TaskAttemptContext getTaskAttemptContext() {
        return context;
    }
    
    protected String getStartLabel() {
        return conf.get(DocumentInputFormat.STARTLABEL, "<page>");
    }
    
    protected String getEndLabel() {
        return conf.get(DocumentInputFormat.ENDLABEL, "</page>");
    }
    
    protected FileSystem getFileSystem() {
        return filesystem;
    }
    
    protected long getStart() {
        return start;
    }
    
    protected long getEnd() {
        return end;
    }
    
    protected Datafile getDatafileIn() {
        return fsin;
    }
    
    public abstract void initialize(FileSplit fileSplit);

    /**
     * Reads the input file, scanning for the next document, setting key and
     * entitywritable with the offset and byte contents of the document read.
     * <p>
     * @return true if a next document was read
     */
    @Override
    public boolean nextKeyValue() throws IOException {
        document = readDocument();
        return document != null;
    }

    public abstract byte[] readDocument() throws IOException;
    
    @Override
    public final LongWritable getCurrentKey() {
        return key;
    }

 
    
    @Override
    public final byte[] getCurrentValue() {
        return document;
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
    public float getProgress() throws IOException, InterruptedException {
        return (fsin.getOffset() - start) / (float) (end - start);
    }

    @Override
    public void close() throws IOException {
        fsin.close();
    }
}
