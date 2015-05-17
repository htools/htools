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
 * Marks <title> </title> sections, which is use in some news wires and in HTML
 * documents to mark the title.
 * <p/>
 * @author jbpvuurens
 */
public class MarkTitle extends SectionMarker {

    public static Log log = new Log(MarkTitle.class);
    public ByteSearch endmarker = new ByteSection("</title", ">");

    public MarkTitle(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<title");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        int tagclose = findQuoteSafeTagEnd(section);
        if (tagclose > -1) {
            ByteSearchPosition end = endmarker.findPos(section, tagclose);
            if (end.found()) {
                ByteSearchSection s = content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, end.end);
                //log.info("section marker title %d %d", s.innerstart, s.innerend);
                return s;
            }
        }
        return null;
    }
}
