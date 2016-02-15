package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Extract and restore HTML Metadata for the keywords and description field.
 * This data is also added to the 'all' field.
 * <p>
 * @author jbpvuurens
 */
public class ExtractRestoreHtmlMeta extends ExtractRestore {

    public static Log log = new Log(ExtractRestoreHtmlMeta.class);
    private ByteSearch correcttype = ByteSearch.create("\\sname\\s*=\\s*(keywords|description|'keywords'|'description'|\"keywords\"|\"description\")");
    private ByteSearch content = ByteSearch.create("\\scontent\\s*=\\s*\\Q");
    private ByteSearch quote = ByteSearch.create("['\"]");

    public ExtractRestoreHtmlMeta(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        int innerstart = section.innerstart - 1;
        if (correcttype.exists(entity.content, innerstart, section.innerend)) {
            ByteSearchPosition c = content.findPos(entity.content, innerstart, section.innerend);
            if (c.found()) {
                int start = quote.find(entity.content, c.start + 8, c.end) + 1;
                add(entity, start, c.end - 1);
            }
        }
    }
}
