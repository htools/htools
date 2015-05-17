package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.PrintTools;

/**
 * Abstract class that can search and replace Strings in the contents. The
 * search and replace strings should have the same byte length, for instance by
 * padding the replace string with \32 for a token break or \0 to not break a
 * token.
 * <p/>
 * @author jbpvuurens
 */
public abstract class Translator extends ExtractorProcessor {

    public static Log log = new Log(Translator.class);
    protected boolean[] translate = new boolean[256];
    protected byte[][] search = new byte[0][];
    protected byte[][] replace = new byte[0][];

    public Translator(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        byte buffer[] = entity.content;
        int c, p, i, j, length;
        LOOP:
        for (p = section.innerstart; p < section.innerend; p++) {
            if (translate[buffer[p] & 0xFF]) {
                for (i = 0; i < search.length; i++) {
                    if (buffer[p] == search[i][0]) {
                        length = search[i].length;
                        if (p + length <= section.innerend) {
                            for (j = 1; j < length && buffer[p + j] == search[i][j]; j++);
                            if (j == length) {
//                                log.printf("replace %d %b\n%s\n%s", p, j == length,
//                                        PrintTools.memoryDump(buffer, p, length),
//                                        PrintTools.memoryDump(replace[i], 0, length));
                                for (j = 0; j < length; j++) {
                                    buffer[p + j] = replace[i][j];
                                }
                                continue LOOP;
                            }
                        }
                    }
                }
            }
        }
    }

    public void add(byte[] search, byte[] replace) {
        int i, category = search[0] & 0xFF;
        if (search.length != replace.length) {
            log.fatal("add() length of search has to match length of replace");
        }
        //log.info("%d", category, search, replace);
        this.translate[category] = true;
        byte newsearch[][] = new byte[this.search.length + 1][];
        byte newreplace[][] = new byte[this.replace.length + 1][];
        for (i = 0; i < this.search.length; i++) {
            newsearch[i] = this.search[i];
            newreplace[i] = this.replace[i];
        }
        //log.info("Translator\n%s\n%s", PrintTools.memoryDump(search), PrintTools.memoryDump(replace));
        newsearch[i] = search;
        newreplace[i] = replace;
        this.search = newsearch;
        this.replace = newreplace;
    }

    public void add(String search, String replace) {
        byte bsearch[] = search.getBytes();
        byte br[] = replace.getBytes();
        byte breplace[] = new byte[bsearch.length];
        int p;
        for (p = 0; p < bsearch.length - br.length; p++) {
            breplace[p] = 0;
        }
        for (; p < bsearch.length; p++) {
            breplace[p] = br[p - (bsearch.length - br.length)];
        }
        add(bsearch, breplace);
    }
}
