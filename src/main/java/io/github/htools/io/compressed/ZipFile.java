package io.github.htools.io.compressed;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Jeroen
 */
public class ZipFile extends ArchiveByteFile<ZipEntry> {
    ZipInputStream zipInputStream;

    public ZipFile(InputStream is) throws IOException {
        super(is);
    }

    @Override
    protected int read() throws IOException {
        return zipInputStream.read();
    }

    @Override
    protected void initialize(BufferedInputStream bis) {
        zipInputStream = new ZipInputStream(bis);
    }

    @Override
    protected ZipEntry getEntry() throws IOException {
        return zipInputStream.getNextEntry();
    }

    @Override
    protected boolean isDirectory(ZipEntry entry) {
        return entry.isDirectory();
    }

    @Override
    protected void close() throws IOException {
       zipInputStream.close();
    }
    
    @Override
    protected void closeEntry() throws IOException {
       zipInputStream.closeEntry();
    }

    @Override
    protected String getName(ZipEntry entry) {
        return entry != null?entry.getName():null;
    }

    @Override
    protected long getSize(ZipEntry entry) throws IOException {
        return entry.getSize();
    }

    @Override
    protected long getLastModified(ZipEntry entry) throws IOException {
        return entry.getLastModifiedTime().toMillis();
    }
}
