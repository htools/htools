package io.github.htools.extract;

import io.github.htools.extract.modules.ExtractorProcessor;
import io.github.htools.extract.modules.TokenChar;
import io.github.htools.extract.modules.TokenizerRegex;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public abstract class AbstractTokenizer extends Extractor {
   public static final Log log = new Log( AbstractTokenizer.class );
   protected TokenizerRegex tokenizer;
   protected TokenChar wordprocessor;
   protected Class tokenClass;

   public AbstractTokenizer() {
       super();
       this.tokenClass = getTokenMarker();
       buildPreProcess();
       buildProcess();
       createSectionProcess();
       buildPostProcess();
   }
   
   public abstract Class getTokenMarker();
   
   public TokenizerRegex getTokenizer() {
       return tokenizer;
   }
   
   public TokenChar getTokenprocessor() {
       return wordprocessor;
   }
   
   protected void createSectionProcess() {
       this.addSectionProcess("all", "tokenize", "result");
   }
   
   protected abstract void buildPreProcess();
   protected abstract void buildProcess();
   protected void buildPostProcess() {
       tokenizer = new TokenizerRegex(this, "tokenize");
       wordprocessor = (TokenChar)tokenizer.setupTokenProcessor("word", tokenClass);
       this.addProcess("tokenize", tokenizer);
   }
   
   protected void addProcess(Class clazz) {
       this.addProcess("tokenize", clazz);
   }
   
   protected void addProcess(ExtractorProcessor processor) {
       this.addProcess("tokenize", processor);
   }
   
   public void addEndPipeline(Class clazz) {
       this.addProcess("tokenize", clazz);
   }
   
   public void addEndPipeline(ExtractorProcessor processor) {
       this.addProcess("tokenize", processor);
   }
   
   public ArrayList<String> tokenize(byte content[]) {
       return process(content).get("result").getTerms();
   }
   
   public ArrayList<String> tokenize(String text) {
       return tokenize(ByteTools.toBytes(text));
   }
}
