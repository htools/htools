package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;

/**
 * Marks a &lt;ti&gt; section, which is
 * sometimes used in news wired to tag the title.
 * <p>
 * @author jbpvuurens
 */
public class MarkTI extends SectionMarker {

    public static Log log = new Log(MarkTI.class);
    public ByteRegex ti = ByteRegex.create("<TI>");
    public ByteSearch endti = ByteSearch.create("</TI>");

    public MarkTI(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return ti;
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        ByteSearchPosition findPos = endti.findPos(section);
        if (findPos.found()) {
            return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, findPos.start, findPos.end);
        }
        return null;
    }
}
