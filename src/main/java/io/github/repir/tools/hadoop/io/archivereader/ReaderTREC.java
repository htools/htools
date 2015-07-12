package io.github.repir.tools.hadoop.io.archivereader;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.HDFSIn;
import io.github.repir.tools.lib.Log;
import java.io.ByteArrayOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * An implementation of EntityReader that scans the input for TREC style
 * documents, that are enclosed in <DOC></DOC> tags. The used tags may be
 * overridden by setting different tags in entityreader.entitystart and
 * entityreader.entityend.
 * <p/>
 * NOTE that the original TREC disks contain .z files, which cannot be
 * decompressed by Java. The files must therefore be decompressed outside this
 * framework.
 * <p/>
 * @author jeroen
 */
public class ReaderTREC extends Reader {

   public static Log log = new Log(ReaderTREC.class);
   private byte[] startTag;
   private byte[] endTag;

   @Override
   public void initialize(FileSplit fileSplit) {
      startTag = conf.get("entityreader.entitystart", "<DOC>").getBytes();
      endTag = conf.get("entityreader.entityend", "</DOC>").getBytes();
      Path file = fileSplit.getPath();
      if (end < HDFSIn.getLengthNoExc(filesystem, file)) { // only works for uncompressed files
         fsin.setCeiling(end);
      }
   }

   ByteSearch einstein = ByteSearch.create("Einstein");
   
   @Override
   public boolean nextKeyValue() {
      if (fsin.hasMore()) {
         if (readUntilStart() && fsin.getOffset() - startTag.length < fsin.getCeiling()) {
            key.set(fsin.getOffset());
            if (readEntity()) {
               //if (einstein.exists(entitywritable.entity.content, 0, entitywritable.entity.content.length)) {
               entitywritable.addSectionPos("all", 
                                entitywritable.content, 0, 0, entitywritable.content.length, entitywritable.content.length);
               return true;
               //}
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

//               if (needleposition == 0 && !fsin.hasMore()) {  // see if we've passed the stop point:
//                  return false;
//               }
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
