package io.github.htools.hadoop.io.archivereader;

import io.github.htools.extract.Content;
import io.github.htools.io.Datafile;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.htools.search.ByteSearch;
import io.github.htools.io.EOCException;
import io.github.htools.search.ByteSection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An implementation of Reader that reads the ClueWeb12 collection, 
 * similar to {@link ReaderClueweb9}, just some differences in Record structure.
 * <p>
 * @author jeroen
 */
public class ReaderClueweb12new extends ArchiveReader {

   public static Log log = new Log(ReaderClueweb12new.class);
   private byte[] warcTag = "WARC/1.0".getBytes();
   private ByteSearch doctype = ByteSearch.create("<!DOCTYPE");
   private ByteSearch endtag = ByteSearch.create(">");
   private ByteSearch contentlength = ByteSearch.create("\nContent-Length: ");
   private ByteSearch warcIDTag = ByteSearch.create("WARC-TREC-ID: ");
   private ByteSearch eol = ByteSearch.create("\n");
   private ByteSection warcID = new ByteSection(warcIDTag, eol); 
   private idlist ids;

   @Override
   public void initialize(FileSplit fileSplit) throws IOException {
      Path file = fileSplit.getPath();
      String directory = getDir(file);
      String idlist = conf.get("repository.idlist", null);
      if (idlist != null) {
         ids = SubSetFile.getIdList(new Datafile(filesystem, idlist + "/" + directory + ".idlist"));
      }
      readEntity(); // skip the first warc tag, isn't a document
   }

   @Override
   public boolean nextKeyValue() throws IOException {
      while (fsin.hasMore()) {
         readEntity();
         Position pos = new Position();
         String id = warcID.getFirstString(entitywritable.content, 0, entitywritable.content.length);

               if (id.length() == 25 && (ids == null || ids.get(id))) {
                  //log.info("id %s", id);
                  entitywritable.get("collectionid").add(id);
                  int recordlength = getLength(pos);
                  if (recordlength > 0) {
                     int warcheaderend = pos.endpos;
                     int startdoctype = doctype.find(entitywritable.content, pos.startpos, entitywritable.content.length);
                     if (startdoctype > 0) {
                        int enddoctype = 1 + ByteTools.find(entitywritable.content, (byte) '>', startdoctype, entitywritable.content.length);
                        entitywritable.addSectionPos("warcheader", 
                                entitywritable.content, 0, 0, warcheaderend, warcheaderend);
                        entitywritable.addSectionPos("all", 
                                entitywritable.content, enddoctype, enddoctype, entitywritable.content.length, entitywritable.content.length);
                     }
                  }
                  key.set(fsin.getOffset());
                  return true;
               }
  
      }
      return false;
   }

   private int getLength(Position pos) {
      int lengthend = contentlength.findEnd(entitywritable.content, pos.startpos, entitywritable.content.length - pos.startpos);
      if (lengthend >= 0) {
         pos.startpos = lengthend;
         pos.endpos = ByteTools.find(entitywritable.content, (byte) '\n', pos.startpos, entitywritable.content.length);
         if (pos.endpos > pos.startpos) {
            String length = ByteTools.toTrimmedString(entitywritable.content, pos.startpos, pos.endpos);
            if (Character.isDigit(length.charAt(0))) {
               return Integer.parseInt(length);
            }
         }
      }
      return -1;
   }

   private void readEntity() throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      entitywritable = new Content();
      key.set(fsin.getOffset());
      int match = 0;
      while (true) {
         try {
            int b = fsin.readByte();
            if (match > 0 && b != warcTag[match]) { // output falsely cached chars
               buffer.write(warcTag, 0, match);
               match = 0;
            }
            if (b == warcTag[match]) { // check if we're matching needle
               match++;
               if (match >= warcTag.length) {
                  break;
               }
            } else {
               buffer.write(b);
            }
         } catch (EOCException ex) {
            buffer.write(warcTag, 0, match);
            break;
         }
      }
      entitywritable.content = buffer.toByteArray();
   }

   public String getDir(Path p) {
      String file = p.toString();
      int pos = file.lastIndexOf('/');
      int pos2 = file.lastIndexOf('/', pos - 1);
      if (pos < 0 || pos2 < 0) {
         log.fatal("illegal path %s", file);
      }
      return file.substring(pos2 + 1, pos);
   }

   class Position {

      int startpos;
      int endpos;
   }
}
