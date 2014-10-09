package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
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

    public TokenProcessorWithSubs(TokenizerRegex tokenizer, String name) {
        super(tokenizer, name);
        createSubProcessors();
    }

    void createSubProcessors() {
        String[] subnames = tokenizer.extractor.getConfigurationStrings(name, "processor", new String[0]);
        for (String subprocessorname : subnames) {
            Class clazz = ClassTools.toClass(subprocessorname, getClass().getPackage().getName(), TokenProcessor.class.getPackage().getName());
            addSubProcessor(clazz);
        }
    }

    public void addSubProcessor(Class clazz) {
        Constructor constructor = ClassTools.getAssignableConstructor(clazz, TokenProcessor.class, TokenizerRegex.class, String.class);
        subs.add((TokenProcessor) ClassTools.construct(constructor, tokenizer, name));
    }

    public abstract int preprocess(byte[] buffer, int startpos, int endpos);

    @Override
    public int process(byte[] buffer, int pos, int endpos) {
        endpos = preprocess(buffer, pos, endpos);
        if (endpos > pos) {
            for (TokenProcessor processor : subs) {
                endpos = processor.process(buffer, pos, endpos);
            }
        }
        return endpos;
    }
}
