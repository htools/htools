package io.github.htools.hadoop.io.archivereader;

import io.github.htools.extract.Content;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * An implementation of EntityReader that reads the CommonCrawl WARC files,
 * similar to {@link ReaderClueweb9}.
 * <p>
 * @author jeroen
 */
public class RecordReaderCC extends Reader {

   public static Log log = new Log(RecordReaderCC.class);
   private byte[] warcTag = "WARC/1.0".getBytes();
   ByteSection domainregex = new ByteSection("WARC\\-Target\\-URI\\:\\s*\\c+://+([^/@:]*(:[^/@]*)?@)?", "(/|\\s)");
   ByteSearch newSection = ByteSearch.create("\\n\\n");
   ByteSection warcType = new ByteSection("Content-Type:\\s*", "\\s");

   @Override
   public void initialize(FileSplit fileSplit) {
      Path file = fileSplit.getPath();
   }

   @Override
   public boolean nextKeyValue() {
      while (fsin.hasMore()) {
         readEntity();
         ArrayList<ByteSearchPosition> sectionbreaks = newSection.findPos(entitywritable.content, 0, entitywritable.content.length, 2);
         if (sectionbreaks.size() == 2) {
            ByteSearchSection typesection = warcType.findPos(entitywritable.content, sectionbreaks.get(0).end, entitywritable.content.length);
            if (sectionbreaks.get(1).end < entitywritable.content.length) {
               if (typesection.found() && typesection.toString().startsWith("text")) {
                  ByteSearchSection domainsection = domainregex.findPos(entitywritable.content, 0, entitywritable.content.length);
                  if (domainsection.found()) {
                     String domain = domainsection.toString();
                     if (domain.contains(".")) {
                        log.info("domain %s", domain);
                        entitywritable.get("domain").add(domain);
                        entitywritable.addSectionPos("all", 
                                entitywritable.content, 0, 
                                sectionbreaks.get(1).end, 
                                entitywritable.content.length, 
                                entitywritable.content.length);
                        return true;
                     }
                  }
               }
            }
         }
      }
      return false;
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
}
