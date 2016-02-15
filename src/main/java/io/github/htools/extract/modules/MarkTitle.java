package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.*;

/**
 * Marks &lt;title&gt; sections, which is use in some news wires and in HTML
 * documents to mark the title.
 * <p>
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
                return s;
            }
        }
        return null;
    }
}
