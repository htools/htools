package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Extractor;

/**
 * Find mediawiki macro's e.g. "{{cite ...}}" that spans more than one line.
 * <p/>
 * @author jbpvuurens
 */
public class MarkWikipediaMLMacro extends SectionMarker {

    public static Log log = new Log(MarkWikipediaMLMacro.class);
    ByteRegex macroEnd = new ByteRegex("}}");
    ByteRegex combi = ByteRegex.combine((ByteRegex) startmarker, macroEnd);
    ByteRegex tableRow = new ByteRegex("\\n|-");

    public MarkWikipediaMLMacro(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("{{");
    }

    @Override
    public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
        int macroopen = 2;
        int pos = position.end;
        int newlines = 0;
        while (macroopen > 0) {
            for (; pos < sectionend && macroopen > 0; pos++) {
                switch (entity.content[pos]) {
                    case '{':
                        macroopen++;
                        break;
                    case '}':
                        macroopen--;
                        break;
                    case '\n':
                        newlines++;
                }
            }
        }
        if (macroopen == 0 && entity.content[pos - 2] == '}' && newlines > 1) {
            return entity.addSectionPos(outputsection, position.start, position.end, pos - 2, pos);
        }
        return null;
    }
}
