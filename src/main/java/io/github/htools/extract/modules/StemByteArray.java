package io.github.htools.extract.modules;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.words.englishStemmer;

import java.util.HashMap;

/**
 * Stems words that appear in the raw byte array using the snowball
 * (Porter 2) stemmer.
 */
public class StemByteArray extends ExtractorProcessor {

   private static Log log = new Log(StemByteArray.class);
   private static final byte[] alreadyStemmed = new byte[0];
   boolean[] isWord = BoolTools.alphanumeric();
   englishStemmer stemmer = englishStemmer.get();
   static HashMap<byte[], byte[]> translateStemmed = new HashMap();

   public StemByteArray(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      byte[] content = entity.getContent();
      for (int wordstart = pos.innerstart; wordstart < pos.innerend - 1; wordstart++) {
          // non-word position, meaning a new word may come
          if (content[wordstart] != 0 && !isWord[content[wordstart] & 0xff]) {
              // move to first word position
              for (wordstart++; wordstart < pos.innerend && !isWord[content[wordstart] & 0xff]; wordstart++);
              if (wordstart < pos.innerend) {
                  int wordend = wordstart + 1;
                  // find end of word
                  for (; wordend < pos.innerend && isWord[content[wordend] & 0xff]; wordend++);
                  byte[] word = ByteTools.toBytes(content, wordstart, wordend);
                  byte[] wordStemmed = translateStemmed.get(word);
                  if (wordStemmed == null) {
                      wordStemmed = ByteTools.toBytes(stemmer.stem(ByteTools.toString(word)));
                      if (ArrayTools.equals(word, wordStemmed))
                          wordStemmed = alreadyStemmed;
                      translateStemmed.put(word, wordStemmed);
                  }
                  if (wordStemmed != alreadyStemmed) {
                      System.arraycopy(wordStemmed, 0, content, wordstart, wordStemmed.length);
                      if (wordStemmed.length < word.length) {
                          Arrays.fill(content, wordstart + wordStemmed.length, wordend, (byte)0);
                      }
                  }
              }
          }
      }
   }
}