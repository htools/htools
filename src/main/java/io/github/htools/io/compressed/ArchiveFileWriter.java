package io.github.htools.io.compressed;

import java.io.IOException;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 *
 * @author Jeroen
 */
public abstract class ArchiveFileWriter {
    public static Log log = new Log(ArchiveFileWriter.class);
    static ByteRegex tarLz4File = ByteRegex.create("\\.tar\\.lz4$");
    static ByteSearch suffix = ByteRegex.combine(tarLz4File);
    protected int compressionlevel = -1;

    public ArchiveFileWriter(OutputStream is, int compressionlevel) throws IOException {
        this.compressionlevel = compressionlevel < 0?getDefaultCompressionLevel():compressionlevel;
        BufferedOutputStream bis = new BufferedOutputStream(is);
        initialize(bis, compressionlevel);
    }

    public static ArchiveFileWriter getWriter(String file, int compressionlevel) throws IOException {
        ByteSearchPosition pos = suffix.findLastPos(file);
        if (pos.found()) {
            FileOutputStream fis = new FileOutputStream(file);
            switch (pos.pattern) {
                case 0:
                    return new TarLz4FileWriter(fis, compressionlevel);
            }
        }
        throw new IOException("Not supported extension for file " + file);
    }
    
    protected abstract int getDefaultCompressionLevel();
    
    protected abstract void initialize(BufferedOutputStream bis, int compressionlevel) throws IOException;

    public abstract void write(File file) throws IOException;

    public abstract void close() throws IOException;
}
