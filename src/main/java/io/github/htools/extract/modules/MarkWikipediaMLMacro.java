package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;

/**
 * Find mediawiki macro's e.g. "{{cite ...}}" that spans more than one line.
 * <p>
 * @author jbpvuurens
 */
public class MarkWikipediaMLMacro extends SectionMarker {

    public static Log log = new Log(MarkWikipediaMLMacro.class);
    //ByteRegex macroEnd = new ByteRegex("\\}\\}");
    //ByteRegex combi = ByteRegex.combine((ByteRegex) startmarker, macroEnd);
    //ByteRegex tableRow = new ByteRegex("\\n|-");

    public MarkWikipediaMLMacro(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("\\{\\{");
    }

    @Override
    public ByteSearchSection process(Content entity, ByteSearchSection section) {
        int macroopen = 2;
        int pos = section.innerstart;
        int newlines = 0;
        while (macroopen > 0 && pos < section.innerend) {
            for (; pos < section.innerend && macroopen > 0; pos++) {
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
            return entity.addSectionPos(outputsection, entity.content, section.start, section.innerstart, pos - 2, pos);
        }
        return null;
    }
}
