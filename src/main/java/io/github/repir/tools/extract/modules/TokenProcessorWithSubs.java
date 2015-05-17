package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.ExtractorConf;
import io.github.repir.tools.lib.ArrayTools;
import io.github.repir.tools.lib.ByteTools;
import io.github.repir.tools.lib.ClassTools;
import io.github.repir.tools.lib.Log;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * TokenProcessor that is expandible with subprocessors. When the tokenizer
 * matches a token, the subprocessors are instructed to operate on that before
 * it is being converted into a token. This can be used to lowercase only
 * specific tokens, remove tokens that exceed a certain length, etc. If a
 * TokenProcessor or SubTokenprocessor return an endpos equal to the startpos of
 * the tokens, this is an indication of an invalid token. To prevent illegal
 * content from being tokenized from within a token, the bytes should be set to
 * \0 and endpos to startpos.
 *
 * @author jer
 */
public abstract class TokenProcessorWithSubs extends TokenProcessor {

    public static Log log = new Log(TokenProcessorWithSubs.class);
    ArrayList<TokenProcessor> subs = new ArrayList();

    public TokenProcessorWithSubs(TokenizerRegex tokenizer, String name) throws ClassNotFoundException {
        super(tokenizer, name);
        createSubProcessors();
    }

    void createSubProcessors() throws ClassNotFoundException {
        if (tokenizer.extractor instanceof ExtractorConf) {
            String[] subnames = ((ExtractorConf) tokenizer.extractor).getConfigurationStrings(name, "processor", new String[0]);
            for (String subprocessorname : subnames) {
                Class clazz = ClassTools.toClass(subprocessorname, getClass().getPackage().getName(), TokenProcessor.class.getPackage().getName());
                addSubProcessor(clazz);
            }
        }
    }

    public void addSubProcessor(Class<? extends TokenProcessor> clazz) throws ClassNotFoundException {
        Constructor<TokenProcessor> constructor = ClassTools.getAssignableConstructor(clazz, TokenProcessor.class, TokenizerRegex.class, String.class);
        subs.add(ClassTools.construct(constructor, tokenizer, name));
    }

    public abstract int endOfToken(byte[] buffer, int startpos, int endpos);

    @Override
    public int process(byte[] buffer, int pos, int endpos) {
        //log.info("preprocess %d %s", pos, ByteTools.toString(buffer, pos, Math.min(pos+50, endpos)));
        endpos = endOfToken(buffer, pos, endpos);
        //log.info("endpos %d", endpos);
        if (endpos > pos) {
            for (TokenProcessor processor : subs) {
                endpos = processor.process(buffer, pos, endpos);
            }
        }
        return endpos;
    }
}
