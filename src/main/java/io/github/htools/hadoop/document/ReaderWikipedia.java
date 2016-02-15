package io.github.htools.hadoop.document;

import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSection;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An implementation of EntityReader that scans the input for Wikipedia XML
 * dumps, that are enclosed in &lt;page&gt;&lt;/page&gt; tags.
 * <p>
 *
 * @author jeroen
 */
public class ReaderWikipedia extends DocumentAbstractReader {

    public static Log log = new Log(ReaderWikipedia.class);
    private byte[] startTag = "<page>".getBytes();
    private byte[] endTag = "</page>".getBytes();
    private ByteSection title = ByteSection.create("<title>", "</title>");
    private ByteSearch redirect = ByteSearch.create("<redirect ");
    private ByteSearch bodyStart = ByteSearch.create("<text");

    @Override
    public void initialize(FileSplit fileSplit) {
        Path file = fileSplit.getPath();
    }

    @Override
    public byte[] readDocument() throws IOException {
        while (getDatafileIn().getOffset() < getEnd() && readUntilStart()) {
            getCurrentKey().set(getDatafileIn().getOffset());
            byte[] bytes = nextDocument();
            if (bytes != null && bodyStart.exists(bytes))
                return bytes;
        }
        return null;
    }

    private byte[] nextDocument() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int needleposition = 0;
        while (true) {
            try {
                int b = getDatafileIn().readByte();
                if (b != endTag[needleposition]) { // check if we match needle
                    if (needleposition > 0) {
                        buffer.write(endTag, 0, needleposition);
                        needleposition = 0;
                    }
                }
                if (b == endTag[needleposition]) {
                    needleposition++;
                    if (needleposition >= endTag.length) {
                        byte[] result = buffer.toByteArray();
                        if (!redirect.exists(result))
                            return result;
                        else
                            return null;
                    }
                } else {
                    buffer.write(b);

//               if (needleposition == 0 && !fsin.hasMore()) {  // see if we've passed the stop point:
//                  return false;
//               }
                }
            } catch (EOCException ex) {
                return null;
            }
        }
    }

    private boolean readUntilStart() throws IOException {
        int needleposition = 0;
        while (getDatafileIn().hasMore() && (getDatafileIn().getOffset() < this.getEnd() || needleposition > 0)) {
            try {
                int b = getDatafileIn().readByte();
                if (b < 0) {
                    needleposition = 0;
                } else {
                    if (b != startTag[needleposition]) { // check if we match needle
                        needleposition = 0;
                    }
                    if (b == startTag[needleposition]) {
                        needleposition++;
                        if (needleposition >= startTag.length) {
                            return true;
                        }
                    } else {
                        if (needleposition == 0 && !getDatafileIn().hasMore()) {  // see if we've passed the stop point:
                            return false;
                        }
                    }
                }
            } catch (EOCException ex) {
                return false;
            }
        }
        return false;
    }
}
