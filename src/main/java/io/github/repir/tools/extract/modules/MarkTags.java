package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;

/**
 * Marks <section> </section> sections.
 * <p/>
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
