package io.github.htools.extract.modules;

import io.github.htools.collection.ArrayMap;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

import java.util.Map;

/**
 * Abstract class that can search and replace Strings in the contents. The
 * search and replace strings should have the same byte length, for instance by
 * padding the replace string with \32 for a token break or \0 to not break a
 * token.
 * <p>
 * @author jbpvuurens
 */
public abstract class Translator extends ExtractorProcessor {

    public static Log log = new Log(Translator.class);
    ArrayMap<byte[], byte[]>[] translations = new ArrayMap[256];

    public Translator(Extractor extractor, String process) {
        super(extractor, process);
        ArrayMap<byte[], byte[]> searchReplace = initSearchReplace();
        for (Map.Entry<byte[], byte[]> entry : searchReplace) {
            byte[] search = entry.getKey();
            byte[] replace = entry.getValue();
            if (translations[search[0] & 0xff] == null) {
                translations[search[0] & 0xff] = new ArrayMap();
            }
            translations[search[0] & 0xff].add(entry);
        }
//        log.info("translate\n%s", ArrayTools.toString(translate));
//        log.info("search\n%s", ArrayTools.toString(search));
//        log.info("replace\n%s", ArrayTools.toString(replace));
    }

    protected abstract ArrayMap<byte[], byte[]> initSearchReplace();

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
//        log.trace("Translator %s %d %d", this.getClass().getCanonicalName(), 
//                section.innerstart, section.innerend);
        byte buffer[] = entity.content;
        int c, p, i, j, length;
        LOOP:
        for (p = section.innerstart; p < section.innerend; p++) {
            if (translations[buffer[p] & 0xFF] != null) {
                for (Map.Entry<byte[], byte[]> entry : translations[buffer[p] & 0xFF]) {
                    byte[] search = entry.getKey();
                    byte[] replace = entry.getValue();
                    length = search.length;
                    if (p + length <= section.innerend) {
                        for (j = 1; j < length && buffer[p + j] == search[j]; j++);
                        if (j == length) {
//                                log.printf("replace %d %b\n%s\n%s", p, j == length,
//                                        PrintTools.memoryDump(buffer, p, length),
//                                        PrintTools.memoryDump(replace[i], 0, length));
                            System.arraycopy(replace, 0, buffer, p, replace.length);
                            continue LOOP;
                        }
                    }
                }
            }
        }
    }
}
