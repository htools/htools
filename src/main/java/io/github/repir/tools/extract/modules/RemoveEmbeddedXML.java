package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.ByteTools;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p/>
 * @author jeroen
 */
public class RemoveEmbeddedXML extends ExtractorProcessor {

    public static Log log = new Log(RemoveEmbeddedXML.class);
    public ByteSearch open = ByteSearch.create("<\\?xml[^A-Za-z]");
    public ByteSearch rootopen = ByteSearch.create("<\\w+\\W");
    public ByteSearch rootclose = ByteSearch.create("</\\w+\\W");

    private RemoveEmbeddedXML(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        int firstopen = 0;
        int start = section.innerstart;
        LOOP:
        for (ByteSearchPosition p = open.findPos(section, start);
                p.found();
                p = open.findPos(section, start)) {
            firstopen = this.findQuoteSafeTagEnd(entity, p.start, section.innerend);
            ByteSearchPosition rootpos = rootopen.findPos(section, firstopen);
            if (rootpos.found()) {
                int nextpos = this.findQuoteSafeTagEnd(entity, rootpos.end, section.innerend);
                String name = rootpos.substring(1, rootpos.end - 1);
                for (ByteSearchPosition closepos = rootclose.findPos(section, nextpos);
                        closepos.found();
                        closepos = open.findPos(section, nextpos)) {
                    nextpos = this.findQuoteSafeTagEnd(entity, nextpos, section.innerend);
                    String closename = closepos.substring(2, closepos.end - 1);
                    if (closename.equalsIgnoreCase(name)) {
                        for (int i = p.start; i < nextpos; i++) {
                            entity.content[i] = 32;
                        }
                        start = nextpos;
                        continue LOOP;
                    }
                }
            }
            for (int i = p.start; i < firstopen; i++) {
                entity.content[i] = 32;
            }
            start = firstopen;
        }
    }

}
