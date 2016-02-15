package io.github.htools.hadoop.io.archivereader;

import io.github.htools.extract.Content;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An implementation of ArchiveReader for flat text archives such as tar files
 * in which every document is a separate file. The entire entry in the tar is
 * then just read as a document.
 * <p>
 * @author jeroen
 */
public class ReaderTar extends ArchiveReader {

    public static Log log = new Log(ReaderTar.class);

    @Override
    public void initialize(FileSplit fileSplit) {
    }

    @Override
    public boolean nextKeyValue() throws IOException {
        while (fsin.hasMore() && fsin.getOffset() < end) {
            key.set(fsin.getOffset());
            if (readEntity()) {
                return true;
            }
        }
        return false;
    }

    private boolean readEntity() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entitywritable = new Content();
        try {
            while (true) {
                int b = fsin.readByte();
                if (b < 0) { // enf of entry encountered in an archivefile
                    return true;
                }
                buffer.write(b);
            }
        } catch (EOCException ex) {
            // end of archivefile
            return false;
        }
    }
}
