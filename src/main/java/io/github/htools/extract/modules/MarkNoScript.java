package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * Marks &lt;noscript&gt; sections.
 * <p>
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
