package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Remove mediawiki tables's e.g. "{{cite ...}}" and "{| table |}".
 * <p>
 * @author jbpvuurens
 */
public class MarkWikipediaTable extends SectionMarker {

    public static Log log = new Log(MarkWikipediaTable.class);
    ByteRegex tableEnd = new ByteRegex("\\|\\}");
    ByteRegex combi = ByteRegex.combine((ByteRegex) startmarker, tableEnd);
    ByteRegex tableRow = new ByteRegex("\\n\\|\\-");

    public MarkWikipediaTable(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("\\{\\|");
    }

    @Override
    public ByteSearchSection process(Content entity, ByteSearchSection section) {
        if (entity.content[section.innerstart - 1] != '{') {
            ByteSearchPosition end = new ByteSearchPosition(section.haystack, section.innerstart, section.innerend);
            int tableopen = 1;
            while (tableopen > 0) {
                end = combi.findPos(entity.content, section.innerstart, section.innerend);
                if (end.found()) {
                    if (end.pattern == 0) {
                        if (entity.content[end.start - 1] != '{') {
                            tableopen++;
                        }
                    } else {
                        if (end.end == section.end || entity.content[end.end + 1] != '}') {
                            tableopen--;
                        }
                    }
                    section = new ByteSearchSection(section.haystack, section.start, end.end, section.innerend, section.end);
                } else {
                    break;
                }
            }
            if (tableopen == 0 && tableRow.exists(entity.content, section.end, end.start)) {
                return entity.addSectionPos(outputsection, entity.content, section.start, section.start, end.start, end.end);
            }
        }
        return null;
    }
}
