package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Marks &lt;ITAG tagnum=56&gt; sections, which is used in some news wires to
 * tag a title.
 * <p>
 * @author jbpvuurens
 */
public class MarkITAG10 extends SectionMarker {

    public static Log log = new Log(MarkITAG10.class);
    public ByteSearch endmarker = ByteSearch.create("</ITAG>");

    public MarkITAG10(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<ITAG\\s+tagnum\\s+=\\s+56\\s+>");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        ByteSearchPosition end = endmarker.findPos(section);
        if (end.found() && end.start > section.innerstart) {
            return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, end.start, end.end);
        }
        return null;
    }
}
