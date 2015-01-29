package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 * Marks <noscript> </nscript> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkNoScript extends SectionMarker {

    public static Log log = new Log(MarkNoScript.class);
    public ByteSearch endmarker = new ByteSection("</noscript", ">");

    public MarkNoScript(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<noscript");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        int tagclose = findQuoteSafeTagEnd(section) + 1;
        if (tagclose > -1) {
            //log.info("content %d %d %d %d %s", 0, entity.content.length, sectionstart, sectionend, new String( entity.content ));
            ByteSearchPosition end = endmarker.findPos(section, tagclose);
            if (end.found()) {
                return content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, end.end);
            }
        }
        return null;
    }
}
