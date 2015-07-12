package io.github.repir.tools.extract;

import io.github.repir.tools.Words.StopWordsMultiLang;
import io.github.repir.tools.extract.modules.ConvertHtmlASCIICodes;
import io.github.repir.tools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.ConvertToLowercase;
import io.github.repir.tools.extract.modules.ConvertUnicodeDiacritics;
import io.github.repir.tools.extract.modules.RemoveFilteredWords;
import io.github.repir.tools.extract.modules.RemoveHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.RemoveNonASCII;
import io.github.repir.tools.extract.modules.TokenWord;
import io.github.repir.tools.lib.Log;
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
    }

    @Override
    protected void buildProcess() {
        this.addProcess(RemoveHtmlSpecialCodes.class);
        //this.addEndPipeline(new RemoveFilteredWords(this, unstemmedFilterSet));
    }
    
    public void removeStopWords() {
        addEndPipeline(new RemoveFilteredWords(this, unstemmedFilterSet));
    }
}
