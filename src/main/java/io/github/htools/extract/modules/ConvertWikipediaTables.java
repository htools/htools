package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;

/**
 * Convert references in Wikipedia XML source, transforming tables into 
 * a sentence per row and stripping the markup.
 *
 * @author jbpvuurens
 */
public class ConvertWikipediaTables extends ExtractorProcessor {

    public static Log log = new Log(ConvertWikipediaTables.class);
    ByteSection table = ByteSection.create("{\\|", "\\|}");

    public ConvertWikipediaTables(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        log.fatal("not implemented");
    }
}
