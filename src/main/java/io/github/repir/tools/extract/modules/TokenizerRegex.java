package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearchSingleClass;
import io.github.repir.tools.lib.BoolTools;
import io.github.repir.tools.lib.ClassTools;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;

/**
 * A configurable tokenizer, that processes a section of byte content, storing
 * the tokens identified in the designated {@link EntityChannel}. This tokenizer
 * can be configured by assigning a TokenProcessor to a possible starting character:
 * <p/>
 * +extractor.<extractor_process_name>.token = <token_name> <char_regex> <token_processor_classname>
 * <p/>
 * e.g. "+extractor.tokenize.token = word TokenWord"
 * <p/>
 * The tokenizer operates on a byte array of raw input. When a byte is found
 * that matches the possible starting char, the TokenProcessor is called. The
 * TokenProcessor is allowed to modify the content in place (e.g. removing 
 * underscores) and returns and end position of the token if accepted, or 
 * an end position equal to the start position if not. While tokens are not accepted
 * sequentially other TokenProcessor are tried that share the same possible starting byte,
 * in order of configuration.
 * <p/>
 * The tokenizer can also be configured from Java, by creating an instance and
 * calling {@link #setupTokenProcessor(java.lang.String, java.lang.String, java.lang.Class)}.
 * 
 * @author jeroen
 */
public class TokenizerRegex extends ExtractorProcessor {

    public static Log log = new Log(TokenizerRegex.class);
    Content current;
    public Extractor extractor;
    public int bufferpos = 0, bufferend = 0;
    ArrayList<TokenProcessor>[] statetransitions = new ArrayList[256];
    public boolean[] skip = BoolTools.allTrue();

    public TokenizerRegex(Extractor extractor, String process) {
        this(extractor, process, new String[0]);
    }
    
    protected TokenizerRegex(Extractor extractor, String process, String states[]) {
        super(extractor, process);
        this.extractor = extractor;

        for (String state : states) {
            String parts[] = state.split("\\s+");
            if (parts.length != 2) {
                log.info("Ignoring invalid token. Need [name][TokenProcessor] but got '%s'", state);
            } else {
                setupTokenProcessor(parts[0], parts[1]);
            }
        }
    }

    void setupTokenProcessor(String name, String processorname) {
        //log.info("%s %s %s", name, regex, processorname);
        Class clazz = ClassTools.toClass(processorname, TokenProcessor.class.getPackage().getName(),
                getClass().getPackage().getName());
        setupTokenProcessor(name, clazz);
    }

    public TokenProcessor setupTokenProcessor(String name, Class processorclazz) {
        //log.info("%s %s %s", name, regex, processorname);
        Constructor cons = ClassTools.tryGetAssignableConstructor(processorclazz, TokenProcessor.class, TokenizerRegex.class, String.class);
        TokenProcessor processor = (TokenProcessor) ClassTools.construct(cons, TokenizerRegex.this, name);
        boolean valid[] = processor.acceptedFirstChars();
        for (int i = 1; i < valid.length; i++) {
            if (valid[i]) {
                if (statetransitions[i] == null)
                    statetransitions[i] = new ArrayList();
                statetransitions[i].add(processor);
                skip[i] = false;
            }
        }
        return processor;
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        //log.info("process %d %d", section.open, section.close);
        current = entity;
        this.bufferpos = section.innerstart;
        this.bufferend = section.innerend;
        if (bufferpos >= bufferend) {
            return;
        }
        ArrayList<String> list = loadTokens(entity.content);
        entity.get(attribute).addAll(list);
    }

    protected ArrayList<String> loadTokens(byte buffer[]) {
        ArrayList<String> chunks = new ArrayList<String>();
        int pos = bufferpos;
        
        LOOP:
        while (pos < bufferend) {
            for (; pos < bufferend && skip[buffer[pos] & 0xff]; pos++);
            if (pos < bufferend) {
               for (TokenProcessor processor : statetransitions[buffer[pos] & 0xff]) {
                   int tokenend = processor.process(buffer, pos, bufferend);
                   if (tokenend > pos) {
                       addToken(buffer, chunks, pos, tokenend);
                       pos = tokenend;
                       continue LOOP;
                   }
               }
            }
            pos++;
        }
        return chunks;
    }

    private void addToken(byte buffer[], ArrayList<String> list, int tokenStart, int tokenend) {
        byte c[];

        //log.info("addToken start %d end %d buffer %d", tokenStart, tokenend, buffer.length);
        if (tokenend > tokenStart) {
            int nullchars = 0;
            for (int p = tokenStart; p < tokenend; p++) {
                if (buffer[p] == 0) {
                    nullchars++;
                }
            }
            if (nullchars == 0) {
                list.add(new String(buffer, tokenStart, tokenend - tokenStart));
            } else {
                c = new byte[tokenend - tokenStart - nullchars];
                for (int cnr = 0, p = tokenStart; p < tokenend; p++) {
                    if (buffer[p] != 0) {
                        // by using tokenchar, characters can be automaticaly mapped, e.g. to lowercase
                        c[cnr++] = buffer[p];
                    }
                }
                list.add(new String(c));
            }
        }
    }
}
