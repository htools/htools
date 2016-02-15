package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

import java.util.ArrayDeque;

/**
 * Remove mediawiki macro's e.g. "{{cite ...}}".
 * <p>
 * @author jbpvuurens
 */
public class RemoveWikipediaMacros extends ExtractorProcessor {

    public static Log log = new Log(RemoveWikipediaMacros.class);
    ArrayDeque<Integer> open = new ArrayDeque<Integer>();
    ArrayDeque<Integer> inMacro = new ArrayDeque<Integer>();

    public RemoveWikipediaMacros(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        if (open.size() > 0) {
            open = new ArrayDeque<Integer>();
        }
        byte buffer[] = entity.content;
        for (int p = section.innerstart; p < section.innerend; p++) {
            switch (buffer[p]) {
                case '{':
                    if (open.size() > 0 && open.getLast() == p - 1) {
                        inMacro.push(open.getLast());
                    }
                    open.push(p);
                    break;
                case '}':
                    if (inMacro.size() > 0 && p < section.innerend - 1 && buffer[p + 1] == '}') {
                        int prevopen = inMacro.poll();
                        for (int i = p + 1; i >= prevopen; i--) {
                            buffer[i] = 32;
                        }
                        p++;
                    }
                    break;
            }
        }
    }
}
