package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class RemoveFilteredWords extends ExtractorProcessor {

   private static Log log = new Log(RemoveFilteredWords.class);
   HashSet<String> words = new HashSet<String>();

   public RemoveFilteredWords(Extractor extractor, String process) {
      super(extractor, process);
   }
   
   public RemoveFilteredWords(Extractor extractor, Collection<String> words) {
      super(extractor, null);
      addWords(words);
   }
   
   
   public void addWords(Collection<String> words) {
       this.words.addAll(words);
   }

   @Override
   public void process(Content entity, ByteSearchSection pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      ExtractChannel attribute = entity.get(attributename);
      Iterator<String> iter = attribute.iterator();
      while (iter.hasNext()) {
         String word = iter.next();
         if (words.contains(word)) {
            iter.remove();
         }
      }
   }
   
   public void process(Iterable<String> list) {
      Iterator<String> iter = list.iterator();
      while (iter.hasNext()) {
         String word = iter.next();
         if (words.contains(word)) {
            iter.remove();
         }
      }
   }
}