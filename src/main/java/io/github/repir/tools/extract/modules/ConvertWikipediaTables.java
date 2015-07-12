package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSection;

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
