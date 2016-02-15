package io.github.htools.hadoop.io.archivereader;

import io.github.htools.extract.Content;
import io.github.htools.io.EOCException;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An implementation of EntityReader that scans the input for Wikipedia XML
 * dumps, that are enclosed in &lt;page&gt;&lt;/page&gt; tags.
 * <p>
 * @author jeroen
 */
public class ReaderWikipedia extends ArchiveReader {

    public static Log log = new Log(ReaderWikipedia.class);
    private byte[] startTag = "<page>".getBytes();
    private byte[] endTag = "</page>".getBytes();
    private byte[] idStart = "<id>".getBytes();
    private byte[] idEnd = "</id>".getBytes();
    private byte[] titleStart = "<title>".getBytes();
    private byte[] titleEnd = "</title>".getBytes();
    private byte[] redirect = "<redirect ".getBytes();
    private byte[] nsStart = "<ns>".getBytes();
    private byte[] nsEnd = "</ns>".getBytes();
    private byte[] bodyStart = "<text".getBytes();
    private byte[] bodyStartEnd = ">".getBytes();
    private byte[] bodyEnd = "</text>".getBytes();

    @Override
    public void initialize(FileSplit fileSplit) {
        Path file = fileSplit.getPath();
    }

    @Override
    public boolean nextKeyValue() throws IOException {
        while (fsin.hasMore() && fsin.getOffset() < end) {
            if (readUntilStart() && fsin.getOffset() - startTag.length < end) {
            log.info("nextKeyValue");
                key.set(fsin.getOffset());
                if (readEntity()) {
                    int starttext = ByteTools.find(entitywritable.content, bodyStart, 0, entitywritable.content.length, false, false);
                    if (starttext > 0) {
                        starttext = ByteTools.find(entitywritable.content, bodyStartEnd, starttext + bodyStart.length, entitywritable.content.length, false, false);
                        if (starttext > 0) {
                            // check for redirect page
                            int endtext = ByteTools.find(entitywritable.content, bodyEnd, starttext + bodyStartEnd.length, entitywritable.content.length, false, false);
                            if (endtext > starttext) {
                                int redirectpos = ByteTools.find(entitywritable.content, redirect, 0, starttext, false, false);
                                if (redirectpos < 0) {
                                    String id = ByteTools.extract(entitywritable.content, idStart, idEnd, 0, starttext, false, false);
                                    String title = ByteTools.extract(entitywritable.content, titleStart, titleEnd, 0, starttext, false, false);
                                    entitywritable.addSectionPos("all", entitywritable.content, starttext + 1, starttext + 1, endtext, endtext);
                                    entitywritable.get("literaltitle").add(title);
                                    entitywritable.get("collectionid").add(id);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean readEntity() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entitywritable = new Content();
        int needleposition = 0;
        while (true) {
            try {
                int b = fsin.readByte();
                if (b < 0) { // eof encountered in an archivefile
                    buffer = new ByteArrayOutputStream();
                    needleposition = 0;
                    if (!fsin.hasMore()) {
                        return false;
                    }
                } else {
                    if (b != endTag[needleposition]) { // check if we match needle
                        if (needleposition > 0) {
                            buffer.write(endTag, 0, needleposition);
                            needleposition = 0;
                        }
                    }
                    if (b == endTag[needleposition]) {
                        needleposition++;
                        if (needleposition >= endTag.length) {
                            entitywritable.content = buffer.toByteArray();
                            return true;
                        }
                    } else {
                        buffer.write(b);
                    }
                }
            } catch (EOCException ex) {
                return false;
            }
        }
    }

    private boolean readUntilStart() throws IOException {
        int needleposition = 0;
        while (true) {
            try {
                int b = fsin.readByte();
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
                        if (needleposition == 0 && !fsin.hasMore()) {  // see if we've passed the stop point:
                            return false;
                        }
                    }
                }
            } catch (EOCException ex) {
                return false;
            }
        }
    }
}
