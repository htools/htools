package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import io.github.htools.extract.ExtractorConf;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSection;
import java.util.ArrayList;

/**
 * Removes a marked section in the {@link Content}'s content.
 *
 * @author jer
 */
public class RemoveSectionMatching extends ExtractorProcessor {

    public static Log log = new Log(RemoveSectionMatching.class);
    ByteSearch pattern;

    public RemoveSectionMatching(Extractor extractor, String process) {
        this(extractor, process, getConfiguration(process, extractor));
    }

    public RemoveSectionMatching(Extractor extractor, String process, String regex) {
        super(extractor, process);
        pattern = ByteSearch.create("regex");
    }

    public static String getConfiguration(String process, Extractor extractor) {
        if (extractor instanceof ExtractorConf) {
            return ((ExtractorConf) extractor).getConfigurationString(process, "removepatternsmatching", "^$");
        }
        log.fatal("Must specify the pattern in te constructor or the Configuration");
        return null;
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {

        for (int i = section.start; i < section.end; i++) {
            entity.content[i] = 32;
        }
    }

    public static class RemoveTrecMetadata extends ExtractorProcessor {

        public static Log log = new Log(RemoveSection.class);
        ByteSearch text2 = ByteSearch.create("\\[text\\]");

        public RemoveTrecMetadata(Extractor extractor, String process) {
            super(extractor, process);
        }

        @Override
        public void process(Content entity, ByteSearchSection section, String attribute) {
            ArrayList<ByteSearchSection> textPos = entity.getSectionPos("text");
            if (textPos.size() > 0) {
                ByteSearchSection tpos = textPos.get(0);
                ByteSearchPosition text2Pos = text2.findPos(tpos);
                if (text2Pos.found()) {
                    tpos = new ByteSearchSection(tpos.haystack, tpos.start, text2Pos.end,
                    tpos.innerend, tpos.end);
                }
                ArrayList<ByteSearchSection> sectionPos = entity.getSectionPos("titlesection");
                if (sectionPos.size() > 0 && sectionPos.get(0).innerend < tpos.innerstart) {
                    ByteSearchSection tsection = sectionPos.get(0);
                    for (int i = section.start; i < tsection.innerstart; i++) {
                        section.haystack[i] = 0;
                    }
                    for (int i = tsection.innerend; i < tpos.innerstart; i++) {
                        section.haystack[i] = 32;
                    }
                    for (int i = tpos.innerend; i < section.end; i++) {
                        section.haystack[i] = 0;
                    }
                } else {
                    for (int i = section.start; i < tpos.innerstart; i++) {
                        section.haystack[i] = 0;
                    }
                    for (int i = tpos.innerend; i < section.end; i++) {
                        section.haystack[i] = 0;
                    }
                }
            }
        }
    }
}

