package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Extractor;
import java.util.ArrayDeque;

/**
 * Remove mediawiki tables's e.g. "{{cite ...}}" and "{| table |}".
 * <p/>
 * @author jbpvuurens
 */
public class MarkWikipediaTable extends SectionMarker {

    public static Log log = new Log(MarkWikipediaTable.class);
    ByteRegex tableEnd = new ByteRegex("\\|\\}");
    ByteRegex combi = ByteRegex.combine((ByteRegex) startmarker, tableEnd);
    ByteRegex tableRow = new ByteRegex("\\n\\|-");

    public MarkWikipediaTable(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("\\{\\|");
    }

    @Override
    public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
        if (position.start == sectionstart || entity.content[position.start - 1] != '{') {
            ByteSearchPosition end = null;
            int tableopen = 1;
            while (tableopen > 0) {
                end = combi.findPos(entity.content, position.end, sectionend);
                if (end.found()) {
                    if (end.pattern == 0) {
                        if (entity.content[end.start - 1] != '{') {
                            tableopen++;
                        }
                    } else {
                        if (end.end == sectionend || entity.content[end.end + 1] != '}') {
                            tableopen--;
                        }
                    }
                } else {
                    break;
                }
            }
            if (tableopen == 0 && tableRow.exists(entity.content, position.end, end.start)) {
                return entity.addSectionPos(outputsection, position.start, position.end, end.start, end.end);
            }
        }
        return null;
    }
}
