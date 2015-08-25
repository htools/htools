package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p>
 * @author jeroen
 */
public class RemoveHtmlScript extends RemoveNestedTagsQuoteSafe {

    public static Log log = new Log(RemoveHtmlScript.class);
    private ByteSearch javascript = ByteSearch.create("javascript");
    private ByteSearch src = ByteSearch.create("\\ssrc\\s*=");

    private RemoveHtmlScript(Extractor extractor, String process) {
        super(extractor, process, "<script[^A-Za-z]", "<\\\\?\\s*/\\s*script\\s*>");
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        int closetag = 0;
        int start = section.innerstart;
        for (ByteSearchPosition p = open.findPos(section, start);
                p.found();
                p = open.findPos(section, start)) {
            closetag = this.findQuoteSafeTagEnd(entity, p.start, section.innerend);
            boolean js = javascript.exists(section.haystack, p.end, closetag);
            boolean src = this.src.exists(section.haystack, p.end, closetag);
            if (src && js) {
                for (int i = p.start; i <= closetag; i++) {
                    section.haystack[i] = 32;
                }
            } else {
                boolean quotes = false;
                int nextoffset = p.end;
                //log.info("removeScript %d %d", p.start, section.innerend);
                ByteSearchPosition nextclose;
                do {
                    quotes = false;
                    nextclose = close.findPos(section.haystack, nextoffset, section.innerend);
                    //log.info("removeScript %d %b", nextclose.start, nextclose.found());
                    if (nextclose.found()) {
                        if (js) {
                            for (int i = closetag; i < nextclose.start; i++) {
                                if (section.haystack[i] == '\\') {
                                    i++;
                                } else if (section.haystack[i] == '/') {
                                    if (++i < nextclose.start) {
                                        if (section.haystack[i] == '/') {
                                            for (i++; i < section.innerend && section.haystack[i] != '\n'; i++) {
                                                section.haystack[i] = '\0';
                                            }
                                            //if (i > nextclose.start)
                                            //    quotes = true;
                                        } else if (section.haystack[i] == '*') {
                                            section.haystack[i - 1] = '\0';
                                            section.haystack[i] = '\0';
                                            for (i++; i < nextclose.start; i++) {
                                                if (section.haystack[i] == '*') {
                                                    if (i < section.innerend && section.haystack[i + 1] == '/') {
                                                        section.haystack[i + 1] = '\0';
                                                        section.haystack[i] = '\0';
                                                        break;
                                                    }
                                                }
                                                section.haystack[i] = '\0';
                                            }
                                            if (i > nextclose.start) {
                                                quotes = true;
                                            }
                                        }
                                    }
                                } else if (section.haystack[i] == '"') {
                                    quotes = true;
                                    for (i++; quotes && i < nextclose.start; i++) {
                                        if (section.haystack[i] == '\\') {
                                            i++;
                                        } else if (section.haystack[i] == '"') {
                                            quotes = false;
                                        }
                                    }
                                } else if (section.haystack[i] == '\'') {
                                    quotes = true;
                                    for (i++; quotes && i < nextclose.start; i++) {
                                        if (section.haystack[i] == '\\') {
                                            i++;
                                        } else if (section.haystack[i] == '\'') {
                                            quotes = false;
                                        }
                                    }
                                }
                            }
                            if (quotes) {
                                nextoffset = nextclose.end;
                            }
                        }
                    }
                } while (quotes && nextclose.found());
                if (!nextclose.found()) {
                    nextclose = close.findPos(section.haystack, p.end, section.innerend);
                }
                if (nextclose.found()) {
                    ByteSearchPosition nextopen = open.findPos(section.haystack, p.end, nextclose.end);
                    String text = ByteTools.toString(section.haystack, closetag, Math.min(closetag + 100, section.end));
                    //log.info("removeScript %d %d %d %b %b %s", p.start, nextclose.end, p.start, nextclose.found(), nextopen.found(), text);
                    if (!nextopen.found() && nextclose.found()) {
                        for (int i = p.start; i < nextclose.end; i++) {
                            entity.content[i] = 32;
                        }
                        start = nextclose.end;
                    } else {
                        start = p.end;
                    }
                } else if (js) {
                    for (int i = p.start; i <= closetag; i++) {
                        section.haystack[i] = 32;
                    }
                } else {
                    start++;
                }
            }
        }
    }

}
