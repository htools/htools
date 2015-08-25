package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.ByteTools;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;

/**
 * Shows the current raw content buffer for debugging
 * <p>
 * @author jeroen
 */
public class ShowContent extends ExtractorProcessor {

    public static Log log = new Log(ShowContent.class);

    public ShowContent(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        //if (entity.get("collectionid").getContentStr().equals("736")) {
        log.info("\n\n--- %s", attribute);
        int lastpos = section.innerstart;
        String s = ByteTools.toFullTrimmedString(entity.content, section.start, section.end - section.start);
        if (s.length() > 0) {
            log.printf("%s", s);
        }
    }
}
