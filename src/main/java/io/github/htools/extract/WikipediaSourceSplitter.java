package io.github.htools.extract;

import io.github.htools.extract.modules.*;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.search.*;

import java.util.ArrayList;

/**
 *
 * @author jeroen
 */
public class WikipediaSourceSplitter extends ExtractorConf {

    public static final Log log = new Log(WikipediaSourceSplitter.class);
    ByteSearch colon = ByteSearch.create(":");
    ByteSearch bar = ByteSearch.create("\\|");
    ByteSearch block = ByteSearch.create("[\\[\\]]");
    ByteSearch startLink = ByteSearch.create("\\[\\[");
    ByteSearch startHyperlink = ByteSearch.create("\\[([^\\s\\]])+(\\s|\\])");
    ByteSearch startCategory = ByteSearch.create("\\[\\[Category:");
    ByteRegex seeAlso = ByteRegex.create("==\\s*see\\s*also\\s*==");
    ByteRegex references = ByteRegex.create("==\\s*references\\s*==");
    ByteRegex furtherReading = ByteRegex.create("==\\s*further\\s*reading\\s*==");
    ByteRegex externalLinks = ByteRegex.create("==\\s*further\\s*reading\\s*==");
    ByteRegex bottomStart = ByteRegex.combine(seeAlso, furtherReading, externalLinks, references);
    ByteSection ref = new ByteSection("<ref(\\s|>)", "</ref>");

    public WikipediaSourceSplitter() {
        super();
        this.addPreProcessor(ConvertHtmlASCIICodes.class);
        this.addPreProcessor(ConvertHtmlSpecialCodes.class);

        this.addSectionMarker(MarkText.class, "all", "text");
        this.addSectionMarker(MarkTitle.class, "all", "title");
        this.addSectionMarker(MarkWikipediaMLMacro.class, "all", "macro");
        this.addSectionMarker(MarkWikipediaReferences.class, "all", "link");
        this.addSectionMarker(MarkWikipediaTable.class, "all", "table");

        this.addProcess("tokenize", ConvertHtmlAmpersand.class);
        this.addProcess("tokenize", RemoveURL.class);
    }

    public Result tokenize(byte content[]) {
        Content entity = process(content);
        Result result = new Result();
        //log.info("title %d", entity.getSectionPos("title").size());
        if (entity.getSectionPos("title").size() == 1) {
            result.title = entity.getSectionPos("title").get(0).toFullTrimmedString();
            //log.info("%s %b", result.title, !colon.exists(entity.getSectionPos("title").get(0)));
            if (!colon.exists(entity.getSectionPos("title").get(0))) {
                //log.info("%d", entity.getSectionPos("text").size());
                result.text = entity.getSectionPos("text");
                result.table = entity.getSectionPos("table");
                result.macro = entity.getSectionPos("macro");
                result.link = entity.getSectionPos("link");
            }
        }
        return result;
    }

    public Result tokenize(String text) {
        return tokenize(ByteTools.toBytes(text));
    }

    @Override
    protected void processSectionMarkers(Content entity) {
        super.processSectionMarkers(entity);
        //this.createUnmarkedSection(entity, "all", allsections, "text");
    }

    public class Result {
        public String title;
        public ArrayList<ByteSearchSection> text = new ArrayList();
        public ArrayList<ByteSearchSection> table = new ArrayList();
        public ArrayList<ByteSearchSection> macro = new ArrayList();
        public ArrayList<ByteSearchSection> link = new ArrayList();

        public void removeTables() {
            for (ByteSearchSection section: table) {
                section.erase();
            }
        }

        public void removeMacros() {
            for (ByteSearchSection section: macro) {
                section.erase();
            }
        }

        public void removeLinks() {
            for (ByteSearchSection section: link) {
                section.erase();
            }
        }

        public void trimText() {
            //log.info("trimtext");
            for (int i = 0; i < text.size(); i++) {
                ByteSearchSection t = text.get(i);
                ByteSearchPosition pos = bottomStart.findPos(t);
                //log.info("bottom %d %d %d %b", t.start, pos.start, pos.end, pos.found());
                if (pos.found()) {
                    t = new ByteSearchSection(pos.haystack, t.start, t.innerstart, pos.start, pos.end);
                    text.set(i, t);
                }
                for (ByteSearchSection section : ref.findAllSections(t)) {
                    //log.info("erase %s\n", section.toOuterString());
                    section.erase();
                }
            }
        }

        public void convertLinks() {
            for (ByteSearchSection section: link) {
                if (startLink.match(section.haystack, section.start, section.innerend)) {
                    ByteSearchPosition pos = startCategory.matchPos(section, section.start);
                    if (pos.found()) {
                        pos.erase();
                    }

                    pos = bar.findLastPos(section);
                    if (pos.found()) {
                        for (int i = section.start; i < pos.end; i++)
                            section.haystack[i] = 0;
                    }

                    // replace [] by empty string, so that [hurricane]s is converted
                    // to hurricanes
                    block.replaceAll(section.haystack, section.start, section.end, "");

                } else {
                    ByteSearchPosition pos = startHyperlink.matchPos(section, section.start, section.end);
                    //log.info("hlink: %d %d %d %b %s", section.start, pos.start, pos.end, pos.found(), ByteTools.toString(section.haystack, section.start, section.end));
                    if (pos.found()) {
                        pos.erase();
                    }
                }
            }
        }
    }
    
}
