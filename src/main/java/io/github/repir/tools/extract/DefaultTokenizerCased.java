package io.github.repir.tools.extract;

import io.github.repir.tools.extract.modules.ConvertHtmlASCIICodes;
import io.github.repir.tools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.ConvertUnicodeDiacritics;
import io.github.repir.tools.extract.modules.RemoveHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.RemoveNonASCII;
import io.github.repir.tools.extract.modules.ShowContent;
import io.github.repir.tools.lib.Log;

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
