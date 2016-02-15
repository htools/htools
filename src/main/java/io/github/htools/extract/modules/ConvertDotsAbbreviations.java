package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

import java.util.ArrayList;

/**
 * Converts dots depending on the context. Dots that are recognized as a decimal
 * point are kept. Dots that are recognized as abbreviations are removed in such
 * way the letters are connected (eg u.s.a. -&gt; usa). Other dots are replaced
 * by spaces.
 * <p>
 * @author jbpvuurens
 */
public class ConvertDotsAbbreviations extends ExtractorProcessor {

    public static Log log = new Log(ConvertDotsAbbreviations.class);
    ByteRegex abbrev = new ByteRegex("\\.(?<=[^\\c\\.]\\c\\.)(\\c\\.)+");
    ByteRegex abbrevstart = new ByteRegex("^\\c\\.(\\c\\.)+");
    ByteRegex combined = ByteRegex.combine(abbrev, abbrevstart);

    public ConvertDotsAbbreviations(Extractor extractor, String process) {
        super(extractor, process);
    }

    public void process(Content entity, ByteSearchSection pos, String attribute) {
        ArrayList<ByteSearchPosition> positions = combined.findAllPos(entity.content, pos.innerstart, pos.innerend);
        for (ByteSearchPosition p : positions) {
            for (int i = p.start; i < p.end - 1; i++) {
                if (entity.content[i] == '.') {
                    entity.content[i] = 0;
                }
            }
               //for (int i = p.start - 1; i < p.end - 1; i += 2)
            //   entity.content[i] &= (255 - 32);
            entity.content[p.end - 1] = 32;
            break;
        }
    }
}
