package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.BoolTools;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 * This processor converts tag names to lowercase, for easy processing Note:
 * this has to be done in the raw byte array instead of using regular
 * expressions as the byte array may contain non-ASCII.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertSentenceTags extends ExtractorProcessor {

    public static Log log = new Log(ConvertSentenceTags.class);
    public ByteRegex tagname = new ByteRegex("</?(p|blockquote|div|br|hr|h\\d)[/\\s>]");

    public ConvertSentenceTags(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        byte buffer[] = entity.content;
        ArrayList<ByteSearchPosition> findAll = tagname.findAllPos(buffer, section.innerstart, section.innerend);
        for (ByteSearchPosition p : findAll) {
            int endtag = this.findQuoteSafeTagEnd(entity, p.start, section.innerend);
            if (endtag > 0) {
                //log.info("convertsentencetag %d %d %s", p.start, endtag, new String(entity.content, Math.max(p.start-10, 0), Math.min(section.end - p.start, endtag - p.start + 1 + 40)));
                for (int i = p.start; i < endtag + 1; i++) {
                    buffer[i] = '\n';
                }
            }
        }
    }
}
