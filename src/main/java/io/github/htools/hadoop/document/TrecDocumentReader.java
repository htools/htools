package io.github.htools.hadoop.document;

import io.github.htools.io.EOCException;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.ByteArrayOutputStream;

/**
 * An implementation of DocumentAbstractReader that scans the input for TREC style
 * documents, that are enclosed in &lt;DOC&gt;&lt;/DOC&gt; tags. The used tags may be
 * overridden by setting different tags in entityreader.entitystart and
 * entityreader.entityend.
 * <p>
 * NOTE that the original TREC disks contain .z files, which cannot be
 * decompressed by Java. The files must therefore be decompressed outside this
 * framework.
 * <p>
 * @author jeroen
 */
public class TrecDocumentReader extends DocumentAbstractReader {

   public static Log log = new Log(TrecDocumentReader.class);
   private byte[] startTag;
   private byte[] endTag;

   @Override
   public void initialize(FileSplit fileSplit) {
      startTag = ByteTools.toBytes(getStartLabel());
      endTag = ByteTools.toBytes(getEndLabel());
      Path file = fileSplit.getPath();
   }
   
   @Override
   public byte[] readDocument() {
      if (getDatafileIn().getOffset() < getEnd()) {
         if (readUntilStart()) {
            getCurrentKey().set(getDatafileIn().getOffset());
            return nextDocument();
         }
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
                  return buffer.toByteArray();
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

   private boolean readUntilStart() {
      int needleposition = 0;
      while (true) {
         try {
            int b = getDatafileIn().readByte();
            if (b != startTag[needleposition]) { // check if we match needle
               needleposition = 0;
            }
            if (b == startTag[needleposition]) {
               needleposition++;
               if (needleposition >= startTag.length) {
                  return true;
               }
            } else {
               if (needleposition == 0 && getDatafileIn().getOffset() >= getEnd()) {  // see if we've passed the stop point:
                  return false;
               }
            }
         } catch (EOCException ex) {
            return false;
         }
      }
   }
}
