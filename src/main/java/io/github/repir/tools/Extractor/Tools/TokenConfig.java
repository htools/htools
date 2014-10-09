package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSearchSingle;
import io.github.repir.tools.ByteSearch.ByteSearchSingleClass;
import io.github.repir.tools.Lib.Log;

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

    public TokenConfig(TokenizerRegex tokenizer, String name) {
        super(tokenizer, name);
    }

    @Override
    public boolean[] setValidChars() {
        String pattern = tokenizer.extractor.getConfigurationString(name, "regex", "");
        return ByteSearchSingleClass.create(pattern).getValid();
    }

    @Override
    public int preprocess(byte[] buffer, int pos, int endpos) {
        for (; pos < endpos && valid[buffer[pos] & 0xff]; pos++);
        return pos;
    }
}
