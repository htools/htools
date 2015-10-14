package io.github.htools.io.compressed;

import java.io.IOException;
import io.github.htools.lib.Log;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;
import net.jpountz.lz4.LZ4BlockInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 *
 * @author Jeroen
 */
public class TarFile extends ArchiveByteFile<org.apache.commons.compress.archivers.ArchiveEntry> {

    public static Log log = new Log(TarFile.class);
    TarArchiveInputStream tar;

    public TarFile(InputStream is) throws IOException {
        super(is);
    }

    @Override
    protected void initialize(BufferedInputStream is) {
        tar = new TarArchiveInputStream(is);
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
