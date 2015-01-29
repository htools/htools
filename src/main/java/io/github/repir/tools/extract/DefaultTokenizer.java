package io.github.repir.tools.extract;

import io.github.repir.tools.extract.modules.ConvertHtmlASCIICodes;
import io.github.repir.tools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.ConvertToLowercase;
import io.github.repir.tools.extract.modules.ConvertUnicodeDiacritics;
import io.github.repir.tools.extract.modules.RemoveHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.RemoveNonASCII;
import io.github.repir.tools.extract.modules.TokenWord;
import io.github.repir.tools.lib.Log;

/**
 *
 * @author jeroen
 */
public class DefaultTokenizer extends AbstractTokenizer {

    public static final Log log = new Log(DefaultTokenizer.class);

    public DefaultTokenizer() {
        super(TokenWord.class);
    }

    @Override
    protected void preProcess() {
        this.addPreProcessor(ConvertHtmlASCIICodes.class);
        this.addPreProcessor(ConvertHtmlSpecialCodes.class);
        this.addPreProcessor(ConvertUnicodeDiacritics.class);
        this.addPreProcessor(new RemoveNonASCII(this, true));
        this.addPreProcessor(ConvertToLowercase.class);
    }

    @Override
    protected void process() {
        this.addProcess(RemoveHtmlSpecialCodes.class);
    }

    @Override
    protected void postProcess() {
    }
}
