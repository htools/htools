package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

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
    public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
        ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
        if (end.found() && end.start > position.end) {
            ByteSearchPosition t = ti.findPos(entity.content, position.end, end.start);
            if (t.found()) {
                position.end = t.end;
                t = endti.findPos(entity.content, position.end, end.start);
                if (t.found()) {
                    end.start = t.start;
                    return entity.addSectionPos(outputsection, position.start, position.end, end.start, end.end);
                }
            }
        }
        return null;
    }
}
