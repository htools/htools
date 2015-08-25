package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;

/**
 * Matches a sequence of letters/digits in the buffer.
 *
 * @author jeroen
 */
public class TokenPDF extends TokenChar {

    public static final Log log = new Log(TokenPDF.class);
    ByteRegex http = new ByteRegex("http:");
    ByteRegex ftp = new ByteRegex("ftp:");
    ByteRegex https = new ByteRegex("https:");
    ByteRegex www = new ByteRegex("www.");
    ByteRegex mail = new ByteRegex("\\w+@");
    ByteRegex web = ByteRegex.combine(http, ftp, https, www, mail);
    ByteRegex space = new ByteRegex("\\s+");
    ByteSearch start = ByteSearch.create("\\w");

    public TokenPDF(TokenizerRegex tokenizer, String name) throws ClassNotFoundException {
        super(tokenizer, name);
    }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.allTrue();
    }

    @Override
    public int endOfToken(byte[] buffer, int pos, int endpos) {
        ByteSearchPosition startPos = start.findPos(buffer, pos, endpos);
        if (startPos.found()) {
            ByteSearchPosition findPos = space.findPos(buffer, startPos.start, endpos);
            return findPos.found() ? findPos.end : endpos;
        }
        return -1;
    }

}
