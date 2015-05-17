package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;

/**
 * Marks <head> </head> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkBody extends SectionMarker {

    public static Log log = new Log(MarkBody.class);
    public ByteSearch endmarker = ByteSearch.create("</body");

    public MarkBody(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<body");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        int tagclose = findQuoteSafeTagEnd(section);
        //log.info("head marker %d %d", section.start, tagclose);
        if (tagclose > -1) {
            ByteSearchPosition end = endmarker.findPos(section, tagclose);
            if (end.found() && end.start > section.innerstart) {
                ByteSearchSection s = content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, end.end);
                //log.info("head marker %d %d", s.innerstart, s.innerend);
                return s;
            }
        }
        return null;
    }
}
