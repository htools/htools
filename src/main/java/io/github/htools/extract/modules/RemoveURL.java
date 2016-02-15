package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p>
 * @author jeroen
 */
public class RemoveURL extends ExtractorProcessor {

    public static Log log = new Log(RemoveURL.class);
    public ByteSearch url = ByteSearch.create("://[\\w/%:@#\\(\\)_\\-\\+=;\\.,\\?\\[\\]\\{\\}\\|~]+");
    public ByteSearch domain = ByteSearch.create("\\.\\c[\\w_\\-]*(\\.\\c[\\w_\\-]*)+");
    public boolean letter[] = BoolTools.letter();
    public boolean name0[] = BoolTools.word0();

    private RemoveURL(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        int startpos = section.innerstart;
        int endpos = section.innerend;
        for (ByteSearchPosition pos : url.findAllPos(entity.content, startpos, endpos)) {
            int bpos = pos.start;
            while (bpos > section.innerstart && letter[entity.content[bpos - 1] & 0xFF]) {
                bpos--;
            }
            for (int a = bpos; a < pos.end; a++) {
                entity.content[a] = 32;
            }
        }
        for (ByteSearchPosition pos : domain.findAllPos(entity.content, startpos, endpos)) {
            int bpos = pos.start;
            for (; bpos > section.innerstart && entity.content[bpos - 1] == 0; bpos--);
            if (bpos > section.innerstart && name0[entity.content[bpos - 1] & 0xFF]) {
                while (bpos > section.innerstart && name0[entity.content[bpos - 1] & 0xFF]) {
                    bpos--;
                }
                for (int a = bpos; a < pos.end; a++) {
                    entity.content[a] = 32;
                }
            }
        }
    }
}
