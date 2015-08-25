package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p>
 * @author jeroen
 */
public class RemoveNestedTagsQuoteSafe extends ExtractorProcessor {

    public static Log log = new Log(RemoveNestedTagsQuoteSafe.class);
    public ByteRegex combine, open, close;

    protected RemoveNestedTagsQuoteSafe(Extractor extractor, String process, ByteRegex open, ByteRegex close) {
        super(extractor, process);
        combine = ByteRegex.combine(open, close);
        this.open = open;
        this.close = close;
    }

    protected RemoveNestedTagsQuoteSafe(Extractor extractor, String process, String open, String close) {
        this(extractor, process, new ByteRegex(open), new ByteRegex(close));
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        int opens = 0;
        int firstopen = 0;
        int start = section.innerstart;
        for (ByteSearchPosition p = this.open.findPos(section, start);
                p.found(); 
                p = (opens > 0)?open.findPosDoubleQuoteSafe(section.haystack, start, section.innerend):open.findPos(section, start)) {
            if (opens++ == 0) {
                firstopen = p.start;
            }
            ByteSearchPosition nextclose = close.findPosDoubleQuoteSafe(section.haystack, p.end, section.innerend);
            ByteSearchPosition nextopen = open.findPosDoubleQuoteSafe(section.haystack, p.end, nextclose.end);
            //log.info("removeScript %d %d %d %d %b %b", firstopen, nextclose.end, p.start, opens, nextclose.found(), nextopen.found());
            if (!nextopen.found() && nextclose.found()) {
                if (--opens == 0) {
                    for (int i = firstopen; i < nextclose.end; i++) {
                        entity.content[i] = 32;
                    }
                }
                start = nextclose.end;
            } else {
                start = p.end;
            }
        }
    }

}
