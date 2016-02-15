package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.extract.ExtractorConf;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

/**
 * This is used to remove chars from test set queries, that are configured using
 * "extractor.rrtestset.removechars=".
 * <p>
 * @author jbpvuurens
 */
public class RemoveQueryChars extends ExtractorProcessor {

   public static Log log = new Log(RemoveQueryChars.class);
   public boolean removechars[];

   public RemoveQueryChars(Extractor extractor, String process) {
      this(extractor, process, getConfiguration(process, extractor) );
   }

   public RemoveQueryChars(Extractor extractor, String process, String removechars) {
      super(extractor, process);
      this.removechars = io.github.htools.lib.ByteTools.getByteArray( removechars );
   }

   public static String getConfiguration(String process, Extractor extractor) {
       if (extractor instanceof ExtractorConf) {
           return ((ExtractorConf)extractor).getConfigurationString(process, "removechars", "");
       }
       return "";
   }
   
   public void setChars(String chars) {
      removechars = io.github.htools.lib.ByteTools.getByteArray( chars );
   }
   
   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      byte buffer[] = entity.content;
      for (int i = section.innerstart; i < section.innerend; i++) {
         if ( removechars[ buffer[i] ] )
            buffer[i] = 32;
      }
   }
}
