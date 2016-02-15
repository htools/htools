package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.*;

/**
 * Marks NYT descriptors
 * <p>
 * @author jbpvuurens
 */
public class MarkDescriptor extends SectionMarker {

    public static Log log = new Log(MarkDescriptor.class);
    public ByteSearch endmarker = new ByteSection("</classifier", ">");

    public MarkDescriptor(Extractor extractor, String inputsection, String outputsection) {
        super(extractor, inputsection, outputsection);
    }

    @Override
    public ByteRegex getStartMarker() {
        return new ByteRegex("<classifier[^>]+type=\"descriptor\"");
    }

    @Override
    public ByteSearchSection process(Content content, ByteSearchSection section) {
        int tagclose = findQuoteSafeTagEnd(section);
        if (tagclose > -1) {
            ByteSearchPosition end = endmarker.findPos(section, tagclose);
            if (end.found()) {
                ByteSearchSection s = content.addSectionPos(outputsection, content.content, section.start, tagclose, end.start, end.end);
                //log.info("descriptor %s", s);
                return s;
            }
        }
        return null;
    }
}
