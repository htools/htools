package io.github.htools.io.compressed;

import java.io.IOException;
import io.github.htools.lib.Log;
import java.io.BufferedInputStream;
import java.io.InputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 *
 * @author Jeroen
 */
public class TarLz4File extends ArchiveByteFile<org.apache.commons.compress.archivers.ArchiveEntry> {

    public static Log log = new Log(TarLz4File.class);
    private LZ4FrameInputStream lz4Stream;
    TarArchiveInputStream tar;

    public TarLz4File(InputStream is) throws IOException {
        super(is);
    }

    @Override
    protected void initialize(BufferedInputStream is) {
        try {
            lz4Stream = new LZ4FrameInputStream(is);
            tar = new TarArchiveInputStream(lz4Stream);
        } catch (IOException ex) {
            log.fatalexception(ex, "initialize");
        }
    }

    @Override
    protected org.apache.commons.compress.archivers.ArchiveEntry getEntry() throws IOException {
        org.apache.commons.compress.archivers.ArchiveEntry nextEntry = null;
        do {
            nextEntry = tar.getNextEntry();
        } while (nextEntry != null && !tar.canReadEntryData(nextEntry));
        return nextEntry;
    }

    @Override
    protected boolean isDirectory(org.apache.commons.compress.archivers.ArchiveEntry entry) {
        return entry.isDirectory();
    }

    @Override
    protected int read() throws IOException {
        return tar.read();
    }

    @Override
    protected void close() throws IOException {
        tar.close();
        tar = null;
        lz4Stream = null;
    }

    @Override
    protected String getName(org.apache.commons.compress.archivers.ArchiveEntry entry) {
        return entry != null ? entry.getName() : null;
    }

    @Override
    protected long getSize(org.apache.commons.compress.archivers.ArchiveEntry entry) throws IOException {
        return entry.getSize();
    }

    @Override
    protected long getLastModified(org.apache.commons.compress.archivers.ArchiveEntry entry) throws IOException {
        return entry.getLastModifiedDate().getTime();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Delete operation not supported.");
    }
}
