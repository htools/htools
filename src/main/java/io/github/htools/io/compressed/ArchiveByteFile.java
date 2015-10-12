package io.github.htools.io.compressed;

import io.github.htools.io.ByteReader;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Jeroen
 * @param <E>
 */
public abstract class ArchiveByteFile<E> extends ArchiveFile<E> implements ByteReader {
    public static Log log = new Log(ArchiveByteFile.class);
    long offset;
   
    public ArchiveByteFile(InputStream is) throws IOException {
        super(is);
    }

   public void closeRead() throws IOException {
       close();
   }

   public String getCurrentFilename() {
       return this.getName(entry.entry);
   }

    @Override
    public long getOffset() {
        return offset;
    }
    
    /**
     * @return -1 when the archive entry has no more content, otherwise the next byte in the stream 
     * or throw an EOCException when there is no more content in the archive
     */
    @Override
    public int readByte() throws IOException {
        if (bytesLeftInCurrentStream > 0) {
            // bytes left in current stream, ouput the next byte
            bytesLeftInCurrentStream--;
            offset++;
            return entry.readByte();
        }
        if (bytesLeftInCurrentStream == 0) {
            // no bytes left in current stream, output a -1 to indicate an
            // end of the current document
            --bytesLeftInCurrentStream;
            return -1;
        }
        while (bytesLeftInCurrentStream < 1 && this.hasMore()) {
            // an end of document was given last call, now try and read the
            // next entry from the archive
            this.hasNext();
        }
        if (bytesLeftInCurrentStream > 0) {
            // output the first byte from the new entry
            bytesLeftInCurrentStream--;
            offset++;
            return entry.readByte();
        }
        // there is no new entry, throw an EndOfContent exception to signal
        throw new EOCException("");
    }
}
