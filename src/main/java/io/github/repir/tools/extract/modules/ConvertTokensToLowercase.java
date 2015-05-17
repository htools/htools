package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.Extractor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class ConvertTokensToLowercase extends ExtractorProcessor {

   private static Log log = new Log(ConvertTokensToLowercase.class);
   Pattern capital = Pattern.compile("[A-Z]");
   
   public ConvertTokensToLowercase(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      ExtractChannel attribute = entity.get(attributename);
      Iterator<String> iter = attribute.iterator();
      for (int i = 0; i < attribute.size(); i++) {
         String word = attribute.get(i);
         if (capital.matcher(word).find()) 
            attribute.set(i, word.toLowerCase());
      }
   }
}