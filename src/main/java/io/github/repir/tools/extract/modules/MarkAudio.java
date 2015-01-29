package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;
import io.github.repir.tools.lib.Log;

/**
 * Marks <audio> </audio> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkAudio extends SectionMarker {

    public static Log log = new Log(MarkAudio.class);
    public ByteSection endmarker = ByteSearch.create("</audio").toSection(ByteSearch.create(">"));

    public MarkAudio(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<audio");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        int tagclose = findQuoteSafeTagEnd(section) + 1;
        if (tagclose > -1) {
            ByteSearchPosition end = endmarker.findPos(section);
            if (end.found() && end.start > section.end) {
                return content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, end.end);
            }
        }
        return null;
    }
}
