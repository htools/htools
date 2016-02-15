package io.github.htools.io.compressed;

import io.github.htools.io.Datafile;
import io.github.htools.io.FSPath;
import io.github.htools.io.HDFSIn;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 *
 * @author Jeroen
 * @param <E>
 */
public abstract class ArchiveFile<E> implements Iterator<ArchiveEntry>, Iterable<ArchiveEntry> {

    public static Log log = new Log(ArchiveFile.class);
    ArchiveEntry<E> entry;
    long bytesLeftInCurrentStream = -1;
    boolean hasMore = true;
    static ByteRegex tarLz4File = ByteRegex.create("\\.tar\\.lz4$");
    static ByteRegex tarFile = ByteRegex.create("\\.tar$");
    static ByteRegex zipFile = ByteRegex.create("\\.zip$");
    static ByteSearch suffix = ByteRegex.combine(tarLz4File, tarFile, zipFile);

    public ArchiveFile(InputStream is) {
        BufferedInputStream bis = new BufferedInputStream(is, 1024 * 1024);
        initialize(bis);
        entry = new ArchiveEntry(this);
    }

    public static ArchiveByteFile getReader(String file, InputStream is)  {
        ByteSearchPosition pos = suffix.findLastPos(file);
        if (pos.found()) {
            switch (pos.pattern) {
                case 0:
                    return new TarFile(is);
                case 1:
                    return new TarFile(is);
                case 2:
                    return new ZipFile(is);
            }
        }
        log.fatal("Not supported extension for file " + file);
        return null;
    }

    public static ArchiveByteFile getReader(String file) throws IOException {
        return ArchiveFile.getReader(file, new Datafile(file).getInputStream());
    }

    /**
     * @param conf
     * @param file
     * @return An ArchiveFile for the given file, or null if the extension is
     * not supported.
     * @throws IOException
     */
    public static ArchiveByteFile getReader(Configuration conf, String file) {
        return ArchiveFile.getReader(file, new HDFSIn(conf, file).getInputStream());
    }

    public static ArchiveByteFile getReader(Configuration conf, Path file) {
        return ArchiveFile.getReader(conf, file.toString());
    }

    public static ArchiveByteFile getReader(Datafile file) {
        return ArchiveFile.getReader(file.getCanonicalPath(), file.getInputStream());
    }

    protected abstract void initialize(BufferedInputStream bis);

    @Override
    public ArchiveEntry next() {
        return entry;
    }

    protected abstract E getEntry() throws IOException;

    protected abstract boolean isDirectory(E entry);

    protected abstract int read() throws IOException;

    public boolean hasMore() {
        return hasMore;
    }

    /**
     * @param entry
     * @return the (file)name of the current entry, or null if there is no
     * current entry
     * @throws IOException
     */
    protected abstract String getName(E entry);

    protected abstract long getSize(E entry) throws IOException;

    protected abstract long getLastModified(E entry) throws IOException;

    protected abstract void close() throws IOException;

    protected void closeEntry() throws IOException {

    }

    /**
     *
     * @return true if a next non-empty file (not directory) was found
     */
    @Override
    public boolean hasNext() {
        try {
            do {
                if (entry.entry != null) {
                    closeEntry();
                }
                entry.entry = getEntry();
                if (entry.entry != null && !isDirectory(entry.getKey())) {
                    bytesLeftInCurrentStream = entry.size();
                } else {
                    bytesLeftInCurrentStream = 0;
                }
            } while (entry.entry != null && bytesLeftInCurrentStream < 1);
        } catch (IOException ex) {
            log.exception(ex, "hasNext");
        } finally {
            if (entry.entry == null) {
                try {
                    close();
                } catch (IOException ex) { }
                hasMore = false;
            }
        }
        return hasMore;
    }

    @Override
    public Iterator<ArchiveEntry> iterator() {
        return this;
    }
    
    
    public static void main(String[] args) throws IOException {
        ArgsParser ap = new ArgsParser(args, "-i input -o [output]");
        Datafile in = ap.getDatafile("input");
        FSPath path = ap.getPath("output");
        ArchiveFile tz = ArchiveFile.getReader(in);
        Iterator<ArchiveEntry> iter = tz.iterator();
        while (iter.hasNext()) {
            ArchiveEntry next = iter.next();
            if (next.isDirectory()) {
                FSPath newdir = path.getSubdir(next.getName());
                newdir.mkdir();
            } else {
               Datafile df = path.getFile(next.getName());
               df.openWrite();
               df.writeRaw(next.readAll());
               df.closeWrite();
            }
        }
    }    
}
