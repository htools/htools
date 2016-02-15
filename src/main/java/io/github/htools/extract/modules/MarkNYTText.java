package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Marks &lt;text&gt; sections, which are used in some news wires to tag the
 * body of the document.
 * <p>
 * @author jbpvuurens
 */
public class MarkNYTText extends SectionMarker {

    public static Log log = new Log(MarkNYTText.class);
    public ByteSearch endmarker = ByteSearch.create("</block>");
    public ByteSearch leadsection = ByteSearch.create("<p>\\s*LEAD:.*?</p>");

    public MarkNYTText(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<block[^>]+class=\"full_text\"");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        int tagclose = findQuoteSafeTagEnd(section);
        if (tagclose > 0) {
            ByteSearchPosition end = endmarker.findPos(section, tagclose);
            //log.info("MarkText %d %d %d %b", section.innerstart, tagclose, end.start, end.found());
            if (end.found()) {
                ByteSearchPosition findPos = leadsection.findPos(section.haystack, tagclose, end.start);
                if (findPos.found()) {
                   return content.addSectionPos(outputsection, content.content, section.start, findPos.end, end.start, section.end);
                } else {
                   return content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, section.end);
                }
            }
        }
        return null;
    }
}
