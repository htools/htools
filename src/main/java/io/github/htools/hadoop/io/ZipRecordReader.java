package io.github.htools.hadoop.io;

import io.github.htools.hadoop.ContextTools;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.lib.Log;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Jeroen
 */
public class ZipRecordReader extends RecordReader<ZipEntry, byte[]> {

    public static Log log = new Log(ZipRecordReader.class);
    private FileSystem fs = null;
    private FSDataInputStream fsin = null;
    private ZipEntryIterator iter = null;

    public ZipRecordReader(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException {
       initialize(inputSplit, context);
    }

    @Override
    public void close() throws IOException {
        iter.zipInputStream.close();
    }

    @Override
    public float getProgress() throws IOException {
        return 0.0f;
    }

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext context) throws IOException, InterruptedException {
        FileSplit fileSplit = (FileSplit) inputSplit;
        Path path = fileSplit.getPath();
        fs = ContextTools.getFileSystem(context);
        fsin = fs.open(path);
        iter = new ZipEntryIterator(path.toString(), fsin);
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        return iter.hasNext();
    }

    @Override
    public ZipEntry getCurrentKey() throws IOException, InterruptedException {
        return iter.zipEntry;
    }

    @Override
    public byte[] getCurrentValue() throws IOException, InterruptedException {
        return iter.content;
    }

    class ZipEntryIterator implements Iterator<ZipEntry> {

        private ZipInputStream zipInputStream = null;
        private String zipFilename;
        ZipEntry zipEntry = null;
        byte[] content;

        public ZipEntryIterator(String filename, InputStream in) throws IOException {
            this.zipFilename = filename;
            zipInputStream = new ZipInputStream(in);
        }

        @Override
        public ZipEntry next() {
            return zipEntry;
        }

        @Override
        public boolean hasNext() {
            if (zipEntry != null) {
                try {
                    zipInputStream.closeEntry();
                } catch (IOException ex) {
                    log.exception(ex, "hasNext");
                }
            }
            zipEntry = null;
            try {
                do {
                    zipEntry = zipInputStream.getNextEntry();
                } while (zipEntry != null && zipEntry.isDirectory());
                if (zipEntry != null && !zipEntry.isDirectory()) {
                    readContent();
                }
            } catch (IOException ex) {
                log.exception(ex, "hasNext");
            }
            if (zipEntry == null) {
                try {
                    zipInputStream.closeEntry();
                } catch (IOException ex) {
                    log.exception(ex, "hasNext");
                }
            }
            return zipEntry != null;
        }

        void readContent() throws IOException {
            BufferDelayedWriter writer = new BufferDelayedWriter();
            byte[] buffer = new byte[(int)zipEntry.getSize()];
            int size = (int)zipEntry.getSize();
            int n = 0;
            while (-1 != (n = zipInputStream.read(buffer)) && writer.getSize() < size) {
                writer.write(buffer, 0, n);
            }
            this.content = writer.getBytes();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Delete operation not supported.");
        }
   }
}
