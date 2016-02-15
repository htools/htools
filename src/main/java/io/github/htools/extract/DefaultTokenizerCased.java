package io.github.htools.extract;

import io.github.htools.extract.modules.*;
import io.github.htools.lib.Log;

/**
 *
 * @author jeroen
 */
public class DefaultTokenizerCased extends DefaultTokenizer {

    public static final Log log = new Log(DefaultTokenizerCased.class);

    public DefaultTokenizerCased() {
        super();
    }

    @Override
    protected void buildPreProcess() {
        
        this.addPreProcessor(ConvertHtmlASCIICodes.class);
        this.addPreProcessor(ConvertHtmlSpecialCodes.class);
        this.addPreProcessor(ConvertUnicodeDiacritics.class);
        this.addPreProcessor(new RemoveNonASCII(this, true));
    }

    @Override
    protected void buildProcess() {
        this.addProcess(RemoveHtmlSpecialCodes.class);
    }
}
