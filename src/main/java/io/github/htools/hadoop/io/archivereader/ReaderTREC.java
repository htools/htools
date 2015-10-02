package io.github.htools.hadoop.io.archivereader;

import io.github.htools.extract.Content;
import io.github.htools.search.ByteSearch;
import io.github.htools.io.EOCException;
import io.github.htools.io.HDFSIn;
import io.github.htools.lib.Log;
import java.io.ByteArrayOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * An implementation of EntityReader that scans the input for TREC style
 * documents, that are enclosed in &lt;DOC&gt; tags.
 * <p>
 * NOTE that the original TREC disks contain .z files, which cannot be
 * decompressed by Java. The files must therefore be decompressed outside this
 * framework.
 * <p>
 * @author jeroen
 */
public class ReaderTREC extends ArchiveReader {

   public static Log log = new Log(ReaderTREC.class);
   private byte[] startTag;
   private byte[] endTag;

   @Override
   public void initialize(FileSplit fileSplit) {
      startTag = getDocStartTag().getBytes();
      endTag = getDocEndTag().getBytes();
      Path file = fileSplit.getPath();
      if (end < HDFSIn.getLengthNoExc(filesystem, file)) { // only works for uncompressed files
         fsin.setCeiling(end);
      }
   }
   
   public String getDocStartTag() {
       return "<DOC>";
   }
   
   public String getDocEndTag() {
       return "</DOC>";
   }
   
   @Override
   public boolean nextKeyValue() {
      if (fsin.hasMore()) {
         if (readUntilStart() && fsin.getOffset() - startTag.length < fsin.getCeiling()) {
            key.set(fsin.getOffset());
            if (readEntity()) {
               entitywritable.addSectionPos("all", 
                                entitywritable.content, 
                                0, 
                                0, 
                                entitywritable.content.length, 
                                entitywritable.content.length);
               return true;
            }
         }
      }
      return false;
   }

   private boolean readEntity() {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      entitywritable = new Content();
      int needleposition = 0;
      while (true) {
         try {
            int b = fsin.readByte();
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
         } catch (EOCException ex) {
            return false;
         }
      }
   }

   private boolean readUntilStart() {
      int needleposition = 0;
      while (true) {
         try {
            int b = fsin.readByte();
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
         } catch (EOCException ex) {
            return false;
         }
      }
   }
}
