package io.github.htools.io;

import io.github.htools.hadoop.ContextTools;
import io.github.htools.io.ZipFile.Entry;
import io.github.htools.io.buffer.BufferDelayedWriter;
import java.io.IOException;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import io.github.htools.lib.Log;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 *
 * @author Jeroen
 */
public class ZipFile implements Iterator<Entry>, Iterable<Entry> {

    public static Log log = new Log(ZipFile.class);
    private FileSystem fs = null;
    private FSDataInputStream fsin = null;
    private ZipInputStream zipInputStream = null;
    Entry entry = new Entry();

    public ZipFile(InputStream is) {
        this(new BufferedInputStream(is));
    }

    public ZipFile(BufferedInputStream is) {
        zipInputStream = new ZipInputStream(is);
    }

    @Override
    public Entry next() {
        return entry;
    }

    @Override
    public boolean hasNext() {
        try {
            do {
                entry.zipEntry = zipInputStream.getNextEntry();
            } while (entry.zipEntry != null && entry.zipEntry.isDirectory());
            if (entry.zipEntry != null && !entry.zipEntry.isDirectory()) {
                readContent();
            }
        } catch (IOException ex) {
            log.exception(ex, "hasNext");
        }
        if (entry.zipEntry == null) {
            try {
                zipInputStream.closeEntry();
            } catch (IOException ex) {
                log.exception(ex, "hasNext");
            }
        }
        return entry != null;
    }

    void readContent() throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        byte[] buffer = new byte[(int) entry.zipEntry.getSize()];
        int size = (int) entry.zipEntry.getSize();
        int n = 0;
        while (-1 != (n = zipInputStream.read(buffer)) && writer.getSize() < size) {
            writer.write(buffer, 0, n);
        }
        entry.content = writer.getBytes();
    }

    @Override
    public Iterator<Entry> iterator() {
        return this;
    }
    
    public static class Entry implements java.util.Map.Entry<ZipEntry, byte[]> {
        public ZipEntry zipEntry;
        public byte[] content;

        @Override
        public ZipEntry getKey() {
            return zipEntry;
        }

        @Override
        public byte[] getValue() {
            return content;
        }

        @Override
        public byte[] setValue(byte[] value) {
            byte[] old = content;
            content = value;
            return old;
        }
    };
}
