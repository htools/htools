package io.github.htools.extract;

import io.github.htools.words.StopWordsMultiLang;
import io.github.htools.extract.modules.ConvertHtmlASCIICodes;
import io.github.htools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.htools.extract.modules.ConvertToLowercase;
import io.github.htools.extract.modules.ConvertUnicodeDiacritics;
import io.github.htools.extract.modules.RemoveFilteredWords;
import io.github.htools.extract.modules.RemoveHtmlSpecialCodes;
import io.github.htools.extract.modules.RemoveNonASCII;
import io.github.htools.extract.modules.TokenWord;
import io.github.htools.lib.Log;
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
