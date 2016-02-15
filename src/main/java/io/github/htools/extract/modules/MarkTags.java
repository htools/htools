package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Marks &lt;section&gt; sections.
 * <p>
 * @author jbpvuurens
 */
public class MarkTags extends SectionMarker {

    public static Log log = new Log(MarkTags.class);
    public ByteSearch endmarker = ByteSearch.create("</").toSection(ByteSearch.create(">"));

    public MarkTags(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<[A-Za-z]+[0-9]?");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        
        ByteSearchPosition end = endmarker.findPos(section);
        if (end.found()) {
            return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, end.start, end.end);
        }
        return null;
    }
}
