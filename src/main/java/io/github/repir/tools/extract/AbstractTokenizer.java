package io.github.repir.tools.extract;

import io.github.repir.tools.extract.modules.ExtractorProcessor;
import io.github.repir.tools.extract.modules.TokenChar;
import io.github.repir.tools.extract.modules.TokenizerRegex;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public abstract class AbstractTokenizer extends Extractor {
   public static final Log log = new Log( AbstractTokenizer.class );
   protected TokenizerRegex tokenizer;
   protected TokenChar wordprocessor;

   public AbstractTokenizer(Class tokenClass) {
       super();
       preProcess();
       process();
       this.addSectionProcess("all", "tokenize", "result");
       tokenizer = new TokenizerRegex(this, "tokenize");
       wordprocessor = (TokenChar)tokenizer.setupTokenProcessor("word", tokenClass);
       this.addProcess("tokenize", tokenizer);
       postProcess();
   }
   
   public TokenizerRegex getTokenizer() {
       return tokenizer;
   }
   
   public TokenChar getTokenprocessor() {
       return wordprocessor;
   }
   
   protected abstract void preProcess();
   protected abstract void process();
   protected abstract void postProcess();
   
   protected void addProcess(Class clazz) {
       this.addProcess("tokenize", clazz);
   }
   
   public void addEndPipeline(Class clazz) {
       this.addProcess("tokenize", clazz);
   }
   
   public void addEndPipeline(ExtractorProcessor processor) {
       this.addProcess("tokenize", processor);
   }
   
   public ArrayList<String> tokenize(byte content[]) {
       return process(content).get("result");
   }
   
   public ArrayList<String> tokenize(String text) {
       return tokenize(text.getBytes());
   }
}
