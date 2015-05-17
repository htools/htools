package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import java.util.ArrayList;

/**
 * Marks <script> </script> sections, which mark scripts within HTML pages.
 * <p/>
 * @author jbpvuurens
 */
public class MarkParagraph extends SectionMarker {

    public static Log log = new Log(MarkParagraph.class);
    public ByteSearch endmarker = new ByteSection("</p", ">");

    public MarkParagraph(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<p");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        //log.info("process %d %d %s", section.start, section.innerstart, section.leadSection().toString());
        int tagclose = findQuoteSafeTagEnd(section);
        if (tagclose > -1) {
            ByteSearchPosition end = endmarker.findPos(section, tagclose);
            if (end.found()) {
                //log.info("SectionParagraph %d %d %d %d\n%s", section.start, tagclose, end.start, end.end, PrintTools.memoryDump(content.content, section.start, end.end - section.start));
                return content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, end.end);
            }
        }
        return null;
    }
}
