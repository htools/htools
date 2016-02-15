package io.github.htools.io;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An in memory buffer that acts as an InputStream and implements DataIn to use
 * with Datafile and BufferReaderWriter.
 */
public class BytesIn extends ByteArrayInputStream implements DataIn {

    public static Log log = new Log(BytesIn.class);
    BufferReaderWriter buffer;

    public BytesIn(byte[] content) {
        super(content);
    }

    public final void setBuffer(BufferReaderWriter buffer) {
        this.buffer = buffer;
    }

    public void mustMoveBack() {
        super.reset();
    }

    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            log.fatal("close()");
        }
    }

    public void fillBuffer(BufferReaderWriter buffer) {
        int read = readBytes(super.pos, buffer.buffer, buffer.end, buffer.buffer.length - buffer.end);
        if (read == 0)
            throw new EOCException();
        else
            buffer.end += read;
    }

    public long getLength() {
        return super.available();
    }

    public int readBytes(long offset, byte[] b, int pos, int length) {
        if (offset != super.pos) {
            mustMoveBack();
            skip(offset);
        }
        int read = 0;
        length = Math.min(length, (int) getLength());
        while (length > read) {
            int r = super.read(b, pos + read, length - read);
            if (r > 0)
                read += r;
            else
                break;
        }
        return read;
    }

    public void openRead() {
    }

    public InputStream getInputStream() {
        return this;
    }

    public byte[] readFully() throws EOCException, IOException {
        byte[] result = new byte[super.available()];
        int offset = 0;
        while (offset < result.length) {
            int read = super.read(result, offset, result.length - offset);
            offset += read;
        }
        return result;
    }

    public boolean isCompressed() {
        return false;
    }
}
