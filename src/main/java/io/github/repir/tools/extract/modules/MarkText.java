package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 * Marks <text> </text> sections, which are used in some news wires to tag the
 * body of the document.
 * <p/>
 * @author jbpvuurens
 */
public class MarkText extends SectionMarker {

    public static Log log = new Log(MarkText.class);
    public ByteSearch endmarker = ByteSearch.create("</text>");
    public ByteRegex startmarker = new ByteRegex("<text");

    public MarkText(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return startmarker;
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        int tagclose = findQuoteSafeTagEnd(section) + 1;
        if (tagclose > 0) {
            ByteSearchPosition end = endmarker.findPos(section, tagclose);
            log.info("MarkText %d %d %d %b", section.innerstart, tagclose, end.start, end.found());
            if (end.found()) {
                return content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, section.end);
            }
        }
        return null;
    }
}
