package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Marks &lt;body&gt; sections.
 * <p>
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
