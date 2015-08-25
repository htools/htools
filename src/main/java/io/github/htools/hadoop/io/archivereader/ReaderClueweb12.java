package io.github.htools.hadoop.io.archivereader;

import io.github.htools.extract.Content;
import io.github.htools.io.Datafile;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.io.EOCException;
import java.io.ByteArrayOutputStream;

/**
 * An implementation of Reader that reads the ClueWeb12 collection, 
 * similar to {@link ReaderClueweb9}, just some differences in Record structure.
 * <p>
 * @author jeroen
 */
public class ReaderClueweb12 extends Reader {

   public static Log log = new Log(ReaderClueweb12.class);
   private byte[] warcTag = "WARC/1.0".getBytes();
   private byte[] contentlengthtag = "\nContent-Length: ".getBytes();
   private byte[] doctype = "<!DOCTYPE".getBytes();
   ByteRegex WarcIDTag = new ByteRegex("WARC\\-TREC\\-ID\\:\\s*");
   ByteRegex EOL = new ByteRegex("\\s");
   private byte[] warcIDTag = "WARC-TREC-ID: ".getBytes();
   private byte[] eol = "\n".getBytes();
   private idlist ids;

   @Override
   public void initialize(FileSplit fileSplit) {
      Path file = fileSplit.getPath();
      String directory = getDir(file);
      String idlist = conf.get("repository.idlist", null);
      if (idlist != null) {
         ids = SubSetFile.getIdList(new Datafile(filesystem, idlist + "/" + directory + ".idlist"));
      }
      readEntity(); // skip the first warc tag, isn't a document
   }

   @Override
   public boolean nextKeyValue() {
      while (fsin.hasMore()) {
         readEntity();
         Position pos = new Position();
         ByteSearchPosition find = WarcIDTag.findPos(entitywritable.content, 0, entitywritable.content.length);
         if (find.found()) {
            ByteSearchPosition find1 = EOL.findPos(entitywritable.content, find.end, entitywritable.content.length);
            if (find1.found()) {
               String id = new String(entitywritable.content, find.end, find1.start - find.end);
               //log.info("entity %s", new String(entitywritable.entity.content));
               if (id.length() == 25 && (ids == null || ids.get(id))) {
                  entitywritable.get("collectionid").add(id);
                  int recordlength = getLength(pos);
                  if (recordlength > 0) {
                     int warcheaderend = pos.endpos;
                     int startdoctype = io.github.htools.lib.ByteTools.find(entitywritable.content, doctype, pos.startpos, entitywritable.content.length - pos.startpos, false, false);
                     if (startdoctype > 0) {
                        int enddoctype = 1 + ByteTools.find(entitywritable.content, (byte) '>', startdoctype, entitywritable.content.length);
                        entitywritable.addSectionPos("warcheader", 
                                entitywritable.content, 0, 0, warcheaderend, warcheaderend);
                        entitywritable.addSectionPos("all", 
                                entitywritable.content, enddoctype, enddoctype, entitywritable.content.length, entitywritable.content.length);
                     }
                  }
                  return true;
               }
            }
         }
      }
      return false;
   }

   private int getLength(Position pos) {
      int lengthstart = io.github.htools.lib.ByteTools.find(entitywritable.content, contentlengthtag, pos.startpos, entitywritable.content.length - pos.startpos, false, false);
      if (lengthstart >= 0) {
         pos.startpos = lengthstart + contentlengthtag.length;
         pos.endpos = ByteTools.find(entitywritable.content, (byte) '\n', pos.startpos, entitywritable.content.length);
         if (pos.endpos > pos.startpos) {
            String length = new String(entitywritable.content, pos.startpos, pos.endpos - pos.startpos).trim();
            if (Character.isDigit(length.charAt(0))) {
               return Integer.parseInt(length);
            }
         }
      }
      return -1;
   }

   private void readEntity() {
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
