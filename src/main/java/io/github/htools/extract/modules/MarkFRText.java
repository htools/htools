package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Marks &lt;ITAG tagnum=56&gt; sections, which is used in some news wires to
 * tag a title.
 * <p>
 * @author jbpvuurens
 */
public class MarkFRText extends SectionMarker {

    public static Log log = new Log(MarkFRText.class);
    public ByteSearch endmarker = ByteSearch.create("<!--\\s+PJG\\s+/ITAG\\s+-->");

    public MarkFRText(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<!--\\s+PJG\\s+ITAG\\s+l=10\\s+g=1\\s+f=2\\s+-->\\s+"
                + "SUMMARY:\\s+<!--\\s+PJG\\s+/ITAG\\s+-->\\s+" +
                    "<!--\\sPJG ITAG\\sl=10\\sg=1\\sf=1\\s-->");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        ByteSearchPosition end = endmarker.findPos(section);
        if (end.found() && end.start > section.innerstart) {
            return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, end.start, end.end);
        }
        return null;
    }
}
