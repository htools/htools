package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.Extractor;
import static io.github.repir.tools.lib.ByteTools.*;
import io.github.repir.tools.lib.Log;

/**
 * Extracts the url from a WARC header
 * <p/>
 * @author jbpvuurens
 */
public class ExtractWarcURL extends ExtractorProcessor {

   public static Log log = new Log(ExtractWarcURL.class);
   private byte warcurl[] = "WARC-Target-URI: ".getBytes();
   private byte eol[] = "\n".getBytes();

   public ExtractWarcURL(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      String url = extract(entity.content, warcurl, eol, section.innerstart, section.innerend, false, false);
      ExtractChannel entityurl = new ExtractChannel(entity, attribute);
      entity.put(attribute, entityurl);
      entityurl.add(url);
   }
}
