package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

import java.text.Normalizer;
import java.util.ArrayList;

/**
 * Convert HTML ASCII code like &#101; to the corresponding byte.
 * <p>
 * @author jeroen
 */
public class ConvertHtmlASCIICodes extends ExtractorProcessor {

   public static Log log = new Log(ConvertHtmlASCIICodes.class);
   private ByteSearch regex = ByteSearch.create("&#\\d+;");

   public ConvertHtmlASCIICodes(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      ArrayList<ByteSearchPosition> pos = regex.findAllPos(entity.content, section.innerstart, section.innerend);
      for (ByteSearchPosition p : pos) {
         int ascii = 0;
         for (int i = p.start + 2; i < p.end - 1; i++) {
            ascii = ascii * 10 + entity.content[i] - '0';
         }
         if (ascii > 31 && ascii < 128) {
            entity.content[p.start] = (ascii > 31 && ascii < 256) ? (byte) ascii : 0;
         } else if (ascii > 128) {
             String replaceAll = Normalizer.normalize("" + (char)ascii, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
             if (replaceAll.length() > 0)
                 entity.content[p.start] = (byte)replaceAll.charAt(0);
             else
                 entity.content[p.start] = 0;
         } else {
             entity.content[p.start] = 0;
         }
         for (int i = p.start + 1; i < p.end; i++) {
            entity.content[i] = 0;
         }
      }
   }
}
