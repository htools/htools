package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;

/**
 * Marks a <ti> </ti> section within a <header> </header> section, which is
 * sometimes used in news wired to tag the title.
 * <p/>
 * @author jbpvuurens
 */
public class MarkHeader extends SectionMarker {

    public static Log log = new Log(MarkHeader.class);
    public ByteSearch endmarker = ByteSearch.create("</header>");
    public ByteSearch ti = ByteSearch.create("<TI>");
    public ByteSearch endti = ByteSearch.create("</TI>");

    public MarkHeader(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<header>");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
      ByteSearchPosition end = endmarker.findPos(section);
        if (end.found() && end.start > section.innerstart) {
            ByteSearchPosition t = ti.findPos(section);
            if (t.found()) {
                section.innerstart = t.end;
                t = endti.findPos(section);
                if (t.found()) {
                    end.start = t.start;
                    return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, end.start, end.end);
                }
            }
        }
        return null;
    }
}
