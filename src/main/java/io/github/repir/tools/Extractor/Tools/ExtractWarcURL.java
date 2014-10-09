package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Extractor;
import static io.github.repir.tools.Lib.ByteTools.*;
import io.github.repir.tools.Lib.Log;

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
   public void process(Entity entity, Entity.Section section, String attribute) {
      String url = extract(entity.content, warcurl, eol, section.open, section.close, false, false);
      EntityChannel entityurl = new EntityChannel(entity, attribute);
      entity.put(attribute, entityurl);
      entityurl.add(url);
   }
}
