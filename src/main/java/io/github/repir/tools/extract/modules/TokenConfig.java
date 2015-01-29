package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSingle;
import io.github.repir.tools.search.ByteSearchSingleClass;
import io.github.repir.tools.extract.ExtractorConf;
import io.github.repir.tools.lib.Log;

/**
 * By configuring extractor.<tokenname>.regex, you can give the rule to match
 * each character. Different from TokenRegex, this can only be a regular
 * expression that describes a single character, that is used to match every
 * character in the token. e.g. [\w\.] will match any tokens consisting of
 * letters, numbers and dots. but \w+ is not allowed (no multichars). This Token
 * is translated into a simple decision array for much faster matching than
 * TokenRegex.
 *
 * @author jeroen
 */
public class TokenConfig extends TokenChar {

    public static final Log log = new Log(TokenConfig.class);
    String confchars;

    public TokenConfig(TokenizerRegexConf tokenizer, String name) throws ClassNotFoundException {
        this(tokenizer, name, ((ExtractorConf)tokenizer.extractor).getConfigurationString(name, "regex", ""));
    }

    public TokenConfig(TokenizerRegex tokenizer, String name, String confchar) throws ClassNotFoundException {
        super(tokenizer, name);
        this.confchars = confchar;
    }

    @Override
    public boolean[] setValidChars() {
        return ByteSearchSingleClass.create(confchars).getValid();
    }

    @Override
    public int preprocess(byte[] buffer, int pos, int endpos) {
        for (; pos < endpos && valid[buffer[pos] & 0xff]; pos++);
        return pos;
    }
}
