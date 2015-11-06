package io.github.htools.io.compressed;

import io.github.htools.io.Datafile;
import java.io.IOException;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Writer to a (compressed) archive file. Currently only .tar.lz4 is supported with
 * compression levels 1 (fast) or 9 (hc). Note that 9 compresses 10x slower 
 * but decompresses faster.
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
            FileOutputStream fos = new FileOutputStream(file);
            switch (pos.pattern) {
                case 0:
                    return new TarLz4FileWriter(fos, compressionlevel);
            }
        }
        throw new IOException("Not supported extension for file " + file);
    }
    
    public static ArchiveFileWriter getWriter(Datafile file, int compressionlevel) throws IOException {
        ByteSearchPosition pos = suffix.findLastPos(file.getName());
        if (pos.found()) {
            OutputStream fos = file.getOutputStream();
            switch (pos.pattern) {
                case 0:
                    return new TarLz4FileWriter(fos, compressionlevel);
            }
        }
        throw new IOException("Not supported extension for file " + file);
    }
    
    protected abstract int getDefaultCompressionLevel();
    
    protected abstract void initialize(BufferedOutputStream bis, int compressionlevel) throws IOException;

    public abstract void write(File file) throws IOException;

    public abstract void write(String filename, int size, InputStream inputStream) throws IOException;
    
    public abstract void close() throws IOException;
}
