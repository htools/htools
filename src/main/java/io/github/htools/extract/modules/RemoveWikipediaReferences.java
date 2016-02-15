package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

import java.util.ArrayList;

/**
 * Remove mediawiki macro's e.g. "{{cite ...}}" and "{| table |}".
 * <p>
 * @author jbpvuurens
 */
public class RemoveWikipediaReferences extends ExtractorProcessor {

    public static Log log = new Log(RemoveWikipediaReferences.class);
    ByteSearch refopen = ByteSearch.create("</?ref(?=[\\s>/])");

    public RemoveWikipediaReferences(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        ArrayList<ByteSearchPosition> allPos = refopen.findAllPos(section);
        for (int i = 0; i < allPos.size(); i++) {
            ByteSearchPosition pos = allPos.get(i);
            int endTag = this.findQuoteSafeTagEnd(entity, pos.start, section.innerend);
            log.trace("Ref %d %d %d %d %s", i, pos.start, pos.end, endTag, pos.toString());
            if (endTag > 0) {
                if (pos.byteAt(1) == '/') {
                    log.trace("Ref loose close %d %s", i, pos.toString());
                    for (int j = pos.start; j <= endTag; j++) {
                        entity.content[j] = 0;
                    }
                } else {
                    int selfclosepos = endTag - 1;
                    for (; selfclosepos > pos.start && pos.haystack[selfclosepos] == 0; selfclosepos--);
                    if (pos.haystack[selfclosepos] == '/') {
                        log.trace("Ref self close %d %s", i, pos.toString());
                        for (int j = pos.start; j <= endTag; j++) {
                            entity.content[j] = 0;
                        }
                    } else {
                        int nested = 1;
                        while (nested > 0 && i < allPos.size() - 1) {
                            i++;
                            ByteSearchPosition nextpos = allPos.get(i);
                            endTag = this.findQuoteSafeTagEnd(entity, nextpos.start, section.innerend);
                            if (nextpos.byteAt(1) == '/') {
                                nested--;
                                log.trace("Ref nestedc %d %d %d %d %d", i, nextpos.start, nextpos.end, endTag, nested);
                            } else {
                                selfclosepos = endTag - 1;
                                for (; selfclosepos > nextpos.start && nextpos.haystack[selfclosepos] == 0; selfclosepos--);
                                if (pos.haystack[selfclosepos] != '/') {
                                    nested++;
                                }
                                log.trace("Ref nestedo %d %d %d %d", i, nextpos.start, endTag, nested);
                            }
                        }
                        if (endTag > 0) {
                            log.trace("Ref clear1 %d %d", pos.start, endTag);
                            for (int j = pos.start; j <= endTag; j++) {
                                entity.content[j] = 0;
                            }
                        } else if (i < allPos.size() - 1) {
                            log.trace("Ref clear2 %d %d", pos.start, allPos.get(i + 1).start);
                            for (int j = pos.start; j < allPos.get(i + 1).start; j++) {
                                entity.content[j] = 0;
                            }
                        } else {
                            log.trace("Ref clear3 %d %d", pos.start, section.innerend);
                            for (int j = pos.start; j < section.innerend; j++) {
                                entity.content[j] = 0;
                            }
                        }
                    }
                }
            }
        }
    }
}
