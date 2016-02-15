package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchSection;

/**
 * Find mediawiki macro's e.g. "{{cite ...}}" that spans more than one line.
 * <p>
 * @author jbpvuurens
 */
public class MarkWikipediaReferences extends SectionMarker {

    public static Log log = new Log(MarkWikipediaReferences.class);

    public MarkWikipediaReferences(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("\\[");
    }

    @Override
    public ByteSearchSection process(Content entity, ByteSearchSection section) {
        int macroopen = 1;
        int pos = section.innerstart;
        int newlines = 0;
        while (macroopen > 0 && pos < section.innerend) {
            for (; pos < section.innerend && macroopen > 0; pos++) {
                switch (entity.content[pos]) {
                    case '[':
                        macroopen++;
                        break;
                    case ']':
                        macroopen--;
                        break;
                    case '\n':
                        newlines++;
                }
            }
        }
        if (macroopen == 0 && entity.content[pos - 1] == ']') {
            return entity.addSectionPos(outputsection, entity.content, section.start, section.innerstart, pos - 1, pos);
        }
        return null;
    }
}
