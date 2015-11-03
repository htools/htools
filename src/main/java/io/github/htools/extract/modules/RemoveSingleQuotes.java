package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.BoolTools;

/**
 * Converts a single quote to a space, except when part of a contraction.
 * <p>
 * @author jbpvuurens
 */
public class RemoveSingleQuotes extends ExtractorProcessor {

    public static Log log = new Log(RemoveSingleQuotes.class);
    boolean alphanumeric[] = BoolTools.alphanumeric();

    public RemoveSingleQuotes(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection pos, String attribute) {
        LOOP:
        for (int p = pos.innerstart + 1; p < pos.innerend; p++) {
            if (entity.content[p] == '\'') {
                int s = p - 1;
                for (; s > pos.innerstart && entity.content[s] == 0; s--);
                if (s >= pos.innerstart && alphanumeric[entity.content[s] & 0xff]) {
                    int end = p + 1;
                    for (; end < pos.innerend && entity.content[end] == 0; end++);
                    if (end < pos.innerend && alphanumeric[entity.content[end] & 0xff]) {
                        continue LOOP;
                    }
                }
                entity.content[p] = 0;
            }
        }
    }
}
