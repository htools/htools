package io.github.repir.tools.Extractor;

import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Tools.ConvertHtmlASCIICodes;
import io.github.repir.tools.Extractor.Tools.ConvertHtmlAmpersand;
import io.github.repir.tools.Extractor.Tools.ConvertHtmlSpecialCodes;
import io.github.repir.tools.Extractor.Tools.MarkWikipediaMLMacro;
import io.github.repir.tools.Extractor.Tools.MarkWikipediaTable;
import io.github.repir.tools.Extractor.Tools.TokenInvertedWord;
import io.github.repir.tools.Extractor.Tools.TokenWord;
import io.github.repir.tools.Extractor.Tools.TokenizerRegex;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 *
 * @author jeroen
 */
public class WikipediaSourceTokenizer extends Extractor {

    public static final Log log = new Log(WikipediaSourceTokenizer.class);
    TokenizerRegex tokenizer = getTokenizer();
    
    public WikipediaSourceTokenizer() {
        super();
        this.addPreProcessor(ConvertHtmlASCIICodes.class);
        this.addPreProcessor(ConvertHtmlSpecialCodes.class);

        this.addSectionMarker(MarkWikipediaMLMacro.class, "all", "macro");
        this.addSectionMarker(MarkWikipediaTable.class, "all", "table");
        this.addSectionProcess("text", "tokenize", "dummy");

        this.addProcess("tokenize", ConvertHtmlAmpersand.class);
    }

    private TokenizerRegex getTokenizer() {
        TokenizerRegex tokenizerRegex = new TokenizerRegex(this, "tokenize");
        tokenizerRegex.setupTokenProcessor("word", TokenWord.class);
        tokenizerRegex.setupTokenProcessor("space", TokenInvertedWord.class);
        return tokenizerRegex;
    }

    public Result tokenize(byte content[]) {
        Entity entity = new Entity();
        entity.setContent(content);
        process(entity);
        
        Result result = new Result();
        for (Section section : entity.getSectionPos("text")) {
            tokenizer.process(entity, section, "tokenized");
            result.text.add(entity.put("tokenized", new EntityChannel(entity, "tokenized")));
        }
        for (Section section : entity.getSectionPos("table")) {
            byte[] table = new byte[section.closetrail - section.openlead];
            System.arraycopy(entity.content, section.openlead, table, 0, table.length);
            result.table.add(table);
        }
        for (Section section : entity.getSectionPos("macro")) {
            byte[] macro = new byte[section.closetrail - section.openlead];
            System.arraycopy(entity.content, section.openlead, macro, 0, macro.length);
            result.macro.add(macro);
        }
        
        return result;
    }

    public Result tokenize(String text) {
        return tokenize(text.getBytes());
    }

    @Override
    void processSectionMarkers(Entity entity, int bufferpos, int bufferend) {
        super.processSectionMarkers(entity, bufferpos, bufferend);
        this.createUnmarkedSection(entity, "all", allsections, "text");
    }

    public class Result {
        public ArrayList<ArrayList<String>> text = new ArrayList();
        public ArrayList<byte[]> table = new ArrayList();
        public ArrayList<byte[]> macro = new ArrayList();
    }
    
}
