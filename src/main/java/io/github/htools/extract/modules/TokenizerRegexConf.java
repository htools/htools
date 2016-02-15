package io.github.htools.extract.modules;

import io.github.htools.extract.ExtractorConf;
import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;

/**
 * A configurable tokenizer, that processes a section of byte content, storing
 * the tokens identified in the designated channel. This tokenizer
 * can be configured by assigning a TokenProcessor to a possible starting character:
 * <p>
 * +extractor.extractor_process_name.token = token_name char_regex token_processor_classname
 * <p>
 * e.g. "+extractor.tokenize.token = word TokenWord"
 * <p>
 * The tokenizer operates on a byte array of raw input. When a byte is found
 * that matches the possible starting char, the TokenProcessor is called. The
 * TokenProcessor is allowed to modify the content in place (e.g. removing 
 * underscores) and returns and end position of the token if accepted, or 
 * an end position equal to the start position if not. While tokens are not accepted
 * sequentially other TokenProcessor are tried that share the same possible starting byte,
 * in order of configuration.
 * <p>
 * The tokenizer can also be configured from Java, by creating an instance and
 * calling {@link #setupTokenProcessor(java.lang.String, java.lang.Class) }.
 * 
 * @author jeroen
 */
public class TokenizerRegexConf extends TokenizerRegex {

    public static Log log = new Log(TokenizerRegexConf.class);
    
    public TokenizerRegexConf(ExtractorConf extractor, String process) {
        this(extractor, process,
                extractor.getConfigurationStrings(process, "token", new String[0]));
    }

    private TokenizerRegexConf(ExtractorConf extractor, String process, String states[]) {
        super(extractor, process, states);
        Configuration conf = extractor.conf;
    }
}
