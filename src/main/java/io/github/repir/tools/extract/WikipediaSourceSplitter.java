package io.github.repir.tools.extract;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.modules.ConvertHtmlASCIICodes;
import io.github.repir.tools.extract.modules.ConvertHtmlAmpersand;
import io.github.repir.tools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.MarkWikipediaMLMacro;
import io.github.repir.tools.extract.modules.MarkWikipediaTable;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 *
 * @author jeroen
 */
public class WikipediaSourceSplitter extends ExtractorConf {

    public static final Log log = new Log(WikipediaSourceSplitter.class);
    
    public WikipediaSourceSplitter() {
        super();
        this.addPreProcessor(ConvertHtmlASCIICodes.class);
        this.addPreProcessor(ConvertHtmlSpecialCodes.class);

        this.addSectionMarker(MarkWikipediaMLMacro.class, "all", "macro");
        this.addSectionMarker(MarkWikipediaTable.class, "all", "table");

        this.addProcess("tokenize", ConvertHtmlAmpersand.class);
    }

    public Result tokenize(byte content[]) {
        Content entity = process(content);
        
        Result result = new Result();
        for (ByteSearchSection section : entity.getSectionPos("text")) {
            byte[] text = new byte[section.end - section.start];
            System.arraycopy(entity.content, section.start, text, 0, text.length);
            result.text.add(text);
        }
        for (ByteSearchSection section : entity.getSectionPos("table")) {
            byte[] table = new byte[section.end - section.start];
            System.arraycopy(entity.content, section.start, table, 0, table.length);
            result.table.add(table);
        }
        for (ByteSearchSection section : entity.getSectionPos("macro")) {
            byte[] macro = new byte[section.end - section.start];
            System.arraycopy(entity.content, section.start, macro, 0, macro.length);
            result.macro.add(macro);
        }
        
        return result;
    }

    public Result tokenize(String text) {
        return tokenize(text.getBytes());
    }

    @Override
    protected void processSectionMarkers(Content entity, int bufferpos, int bufferend) {
        super.processSectionMarkers(entity, bufferpos, bufferend);
        this.createUnmarkedSection(entity, "all", allsections, "text");
    }

    public class Result {
        public ArrayList<byte[]> text = new ArrayList();
        public ArrayList<byte[]> table = new ArrayList();
        public ArrayList<byte[]> macro = new ArrayList();
    }
    
}
