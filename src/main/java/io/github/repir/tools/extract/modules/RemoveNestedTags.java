package io.github.repir.tools.extract.modules;

import io.github.repir.tools.collection.ArrayMap;
import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p/>
 * @author jeroen
 */
public class RemoveNestedTags extends ExtractorProcessor {

    public static Log log = new Log(RemoveNestedTags.class);
    public ByteRegex combine;

    protected RemoveNestedTags(Extractor extractor, String process, ByteRegex open, ByteRegex close) {
        super(extractor, process);
        combine = ByteRegex.combine(open, close);
    }

    protected RemoveNestedTags(Extractor extractor, String process, String open, String close) {
        this(extractor, process, new ByteRegex(open), new ByteRegex(close));
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        int open = 0;
        int start = 0;
        ArrayList<ByteSearchPosition> pos = combine.findAllPos(entity.content, section.innerstart, section.innerend);
        for (ByteSearchPosition p : pos) {
            switch (p.pattern) {
                case 0:
                    if (open == 0) {
                        start = p.start;
                    }
                    int tagend = this.findQuoteSafeTagEnd(entity, p.start, section.innerend);
                    if (tagend > 0) {
                        if (entity.content[tagend - 1] == '/') {
                            for (int i = p.start; i <= tagend; i++) {
                                entity.content[i] = 32;
                            }
                        } else {
                            open++;
                        }
                    }
                    break;
                case 1:
                    if (open > 0) {
                        if (--open == 0) {
                            for (int i = start; i < p.end; i++) {
                                entity.content[i] = 32;
                            }
                        }
                    }
            }
        }
    }
}
