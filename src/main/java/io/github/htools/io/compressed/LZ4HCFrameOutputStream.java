package io.github.htools.io.compressed;

import io.github.htools.lib.Log;
import java.io.IOException;
import java.io.OutputStream;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

public class LZ4HCFrameOutputStream extends LZ4FrameOutputStream {
    public static Log log = new Log(LZ4HCFrameOutputStream.class); 

    public LZ4HCFrameOutputStream(OutputStream out, BLOCKSIZE blockSize, FLG.Bits... bits) throws IOException {
        super(out, blockSize, -1L, bits);
    }

    public LZ4HCFrameOutputStream(OutputStream out, BLOCKSIZE blockSize, long knownSize, FLG.Bits... bits) throws IOException {
        super(out, blockSize, knownSize, bits);
    }

    public LZ4HCFrameOutputStream(OutputStream out, BLOCKSIZE blockSize) throws IOException {
        super(out, blockSize);
    }

    public LZ4HCFrameOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected LZ4Compressor getCompressor() {
        return LZ4Factory.fastestInstance().highCompressor(17);
    }
}
