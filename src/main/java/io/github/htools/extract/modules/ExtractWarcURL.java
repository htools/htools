package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

import static io.github.htools.lib.ByteTools.extract;

/**
 * Extracts the url from a WARC header
 * <p>
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
