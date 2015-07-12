package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import java.util.ArrayDeque;

/**
 * Convert references in Wikipedia XML source, keeping only the label part in
 * "[[label part]]" and [[Something|label part]], removing he remaining
 * characters.
 *
 * @author jbpvuurens
 */
public class ConvertWikipediaReferences extends ExtractorProcessor {

    public static Log log = new Log(ConvertWikipediaReferences.class);
    ArrayDeque<Integer> open = new ArrayDeque<Integer>();
    ArrayDeque<Integer> colon = new ArrayDeque<Integer>();
    ArrayDeque<Integer> bar = new ArrayDeque<Integer>();
    boolean suffix[] = new boolean[256];

    public ConvertWikipediaReferences(Extractor extractor, String process) {
        super(extractor, process);
        for (int i = '\''; i < 'z'; i++) {
            suffix[i] = (i == '\'' || (i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z'));
        }
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        if (open.size() > 0) {
            open = new ArrayDeque<Integer>();
        }
        if (colon.size() > 0) {
            colon = new ArrayDeque<Integer>();
        }
        if (bar.size() > 0) {
            bar = new ArrayDeque<Integer>();
        }
        byte buffer[] = entity.content;
        for (int p = section.innerstart; p < section.innerend; p++) {
            switch (buffer[p]) {
                case ':':
                    if (open.size() > 0) {
                        colon.push(p);
                    }
                    break;
                case '|':
                    if (open.size() > 0) {
                        bar.push(p);
                    }
                    break;
                case '[':
                    open.push(p);
                    break;
                case ']':
                    if (open.size() > 0) {
                        int lastopen = open.poll();
                        if (p < section.innerend - 1 && buffer[p + 1] == ']' && open.size() > 0) {
                            int prevopen = open.poll();
                            int lastcolon = (colon.size() == 0) ? -1 : colon.getFirst();
                            int lastbar = (bar.size() == 0) ? -1 : bar.getFirst();

                            if (lastcolon < prevopen) { // no file reference
                                if (lastbar > lastopen) { // take abel/caption
                                    for (int i = prevopen; i <= lastbar; i++) {
                                        buffer[i] = 32;
                                    }
                                    buffer[p] = 0;
                                    buffer[++p] = 0;
                                } else if (prevopen == lastopen - 1) { //simple internal reference
                                    buffer[prevopen] = 32;
                                    buffer[lastopen] = 32;
                                    buffer[p] = 0;
                                    buffer[++p] = 0;
                                }
                            } else { // could be file or category if no space behind :, then better remove all
                                for (lastcolon++; lastcolon < section.innerend && buffer[lastcolon] == 0; lastcolon++);
                                if (lastcolon >= section.innerend || buffer[lastcolon] > ' ') {
                                    for (int i = p + 1; i >= prevopen; i--) {
                                        buffer[i] = 32;
                                    }
                                    for (p++; p + 1 < section.innerend && suffix[buffer[p + 1] & 0xFF]; p++) {
                                        buffer[p + 1] = 32; // remove any suffix, as in "[[train]]s"
                                    }
                                } else {
                                    if (lastbar > lastopen) { // take abel/caption
                                        for (int i = prevopen; i <= lastbar; i++) {
                                            buffer[i] = 32;
                                        }
                                        buffer[p] = 0;
                                        buffer[++p] = 0;
                                    } else if (prevopen == lastopen - 1) { //simple internal reference
                                        buffer[prevopen] = 32;
                                        buffer[lastopen] = 32;
                                        buffer[p] = 0;
                                        buffer[++p] = 0;
                                    }
                                }
                            }
                            while (colon.size() > 0 && colon.getFirst() > prevopen) {
                                colon.pollFirst();
                            }
                            while (bar.size() > 0 && bar.getFirst() > prevopen) {
                                bar.pollFirst();
                            }
                        } else {
                            for (int i = p; i >= lastopen; i--) {
                                buffer[i] = 32;
                            }
                            for (; p + 1 < section.innerend && suffix[buffer[p + 1] & 0xFF]; p++) {
                                buffer[p + 1] = 32; // remove any suffix, as in "[[train]]s"
                            }
                            while (colon.size() > 0 && colon.getFirst() > lastopen) {
                                colon.pollFirst();
                            }
                            while (bar.size() > 0 && bar.getFirst() > lastopen) {
                                bar.pollFirst();
                            }
                        }
                    }
                    break;
            }
        }
    }
}
