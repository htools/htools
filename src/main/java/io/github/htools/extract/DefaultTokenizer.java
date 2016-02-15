package io.github.htools.extract;

import io.github.htools.extract.modules.*;
import io.github.htools.lib.Log;
import io.github.htools.words.StopWordsMultiLang;

import java.util.HashSet;

/**
 *
 * @author jeroen
 */
public class DefaultTokenizer extends AbstractTokenizer {

    public static final Log log = new Log(DefaultTokenizer.class);
    static HashSet<String> unstemmedFilterSet = StopWordsMultiLang.get().getUnstemmedFilterSet();

    public DefaultTokenizer() {
        super();
    }

    @Override
    public Class getTokenMarker() {
        return TokenWord.class;
    }

    @Override
    protected void buildPreProcess() {
        this.addPreProcessor(ConvertHtmlASCIICodes.class);
        this.addPreProcessor(ConvertHtmlSpecialCodes.class);
        this.addPreProcessor(ConvertUnicodeDiacritics.class);
        this.addPreProcessor(new RemoveNonASCII(this, true));
        this.addPreProcessor(ConvertToLowercase.class);
        this.addPreProcessor(RemoveHtmlSpecialCodes.class);
    }

    @Override
    protected void buildProcess() {
    }
    
    public DefaultTokenizer removeStopWords() {
        addEndPipeline(new RemoveFilteredWords(this, unstemmedFilterSet));
        return this;
    }
}
