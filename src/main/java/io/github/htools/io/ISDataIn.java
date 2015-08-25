package io.github.htools.io;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class reads contents from a resource in a jar file.
 * <p>
 * @author jbpvuurens
 */
public class ISDataIn implements DataIn {

    public static Log log = new Log(ISDataIn.class);
    protected InputStream inputstream;
    private BufferReaderWriter buffer;
    private long offset = 0;

    protected ISDataIn() {
    }

    public ISDataIn(InputStream is) {
        this.inputstream = is;
    }

    @Override
    public void mustMoveBack() {
        throw new RuntimeException("Cannot call mustMoveBack on ISDataIn");
    }

    public void setBuffer(BufferReaderWriter buffer) {
        this.buffer = buffer;
    }

    @Override
    public void fillBuffer(BufferReaderWriter buffer) throws EOCException {
        //log.info("readStringFillBuffer");
        if (!buffer.hasMore()) {
            log.fatal("Trying to read past Ceiling (offset %d pos %d end %d ceiling %d)", buffer.offset, buffer.bufferpos, buffer.end, buffer.ceiling);
        }
        int newread, maxread = buffer.readSpace();
        int read = readBytes(buffer.offset + buffer.end, buffer.buffer, buffer.end, maxread);
        //log.info("fillBuffer off %d end %d maxread %d read %d", buffer.offset, buffer.end, maxread, read);
        if (read > 0) {
            buffer.setEnd(buffer.end + read);
        } else {
            buffer.hasmore = false;
            //log.info("EOF reached");
            throw new EOCException("EOF reached");
        }
    }

    public int getBufferSize() {
        return buffer.buffer.length;
    }

    public void setBufferSize(int buffersize) {
        buffer.setBufferSize(buffersize);
    }

    @Override
    public byte[] readFully() throws EOCException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[1000000];

        while ((nRead = getInputStream().read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        byte[] toByteArray = buffer.toByteArray();
        buffer.close();
        getInputStream().close();
        return toByteArray;
    }

    @Override
    public long getLength() throws IOException {
        return Integer.MAX_VALUE;
    }

    private void setOffset(long offset) {
        if (offset < this.offset) {
            mustMoveBack();
        }
        if (offset > this.offset) {
            try {
                getInputStream().skip(offset - this.offset);
                this.offset = offset;
            } catch (IOException ex) {
                log.exception(ex, "setOffset( %d ) inputstream %s", offset, inputstream);
            }
        }
    }
    
    protected void resetOffset() {
        this.offset = 0;
    }

    @Override
    public int readBytes(long offset, byte[] b, int pos, int length) {
        //log.info("readBytes() fileoffset=%d offset=%d pos=%d length=%d", this.offset, offset, pos, length);
        try {
            if (offset != this.offset) {
                setOffset(offset);
            }
            int read = getInputStream().read(b, pos, length);
            if (read > -1) {
                for (int i = pos + read; i < length; i++) {
                    b[i] = 0;
                }
                this.offset += read;
            }
            return read;
        } catch (IOException ex) {
            log.exception(ex, "readBytes( %d, %s, %d, %d ) inputstream %s", offset, b, pos, length, inputstream);
        }
        return 0;
    }

    @Override
    public void openRead() throws IOException {
        getInputStream();
    }

    @Override
    public void close() {
        if (inputstream != null) {
            try {
                inputstream.close();
            } catch (IOException ex) {
                log.exception(ex);
            }
            inputstream = null;
        }
    }
    
    public InputStream getInputStream() throws IOException {
        return inputstream;
    }
}
