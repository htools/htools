package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.*;

/**
 * Marks &lt;audio&gt; sections.
 * <p>
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
        int tagclose = findQuoteSafeTagEnd(section);
        if (tagclose > -1) {
            ByteSearchPosition end = endmarker.findPos(section);
            if (end.found() && end.start > section.end) {
                return content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, end.end);
            }
        }
        return null;
    }
}
