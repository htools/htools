package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import io.github.htools.extract.ExtractorConf;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;

/**
 * Removes a marked section in the {@link Content}'s content.
 * @author jer
 */
public class RemoveSectionMatching extends ExtractorProcessor {

   public static Log log = new Log(RemoveSectionMatching.class);
   ByteSearch pattern;

   public RemoveSectionMatching(Extractor extractor, String process) {
      this(extractor, process, getConfiguration(process, extractor));
   }

   public RemoveSectionMatching(Extractor extractor, String process, String regex) {
      super(extractor, process);
      pattern = ByteSearch.create("regex");
   }

   public static String getConfiguration(String process, Extractor extractor) {
       if (extractor instanceof ExtractorConf) {
           return ((ExtractorConf)extractor).getConfigurationString(process, "removepatternsmatching", "^$");
       }
       log.fatal("Must specify the pattern in te constructor or the Configuration");
       return null;
   }
   
   
   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      
      for (int i = section.start; i < section.end; i++) {
         entity.content[i] = 32;
      }
   }
}
