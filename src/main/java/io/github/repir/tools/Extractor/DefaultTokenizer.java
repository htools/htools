package io.github.repir.tools.Extractor;

import io.github.repir.tools.Extractor.Tools.ConvertHtmlASCIICodes;
import io.github.repir.tools.Extractor.Tools.ConvertHtmlAmpersand;
import io.github.repir.tools.Extractor.Tools.ConvertHtmlSpecialCodes;
import io.github.repir.tools.Extractor.Tools.ConvertUnicodeDiacritics;
import io.github.repir.tools.Extractor.Tools.RemoveHtmlComment;
import io.github.repir.tools.Extractor.Tools.RemoveHtmlSpecialCodes;
import io.github.repir.tools.Extractor.Tools.RemoveHtmlTags;
import io.github.repir.tools.Extractor.Tools.RemoveNonASCII;
import io.github.repir.tools.Extractor.Tools.TokenInvertedWord;
import io.github.repir.tools.Extractor.Tools.TokenProcessor;
import io.github.repir.tools.Extractor.Tools.TokenWord;
import io.github.repir.tools.Extractor.Tools.TokenizerRegex;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class DefaultTokenizer extends Extractor {
   public static final Log log = new Log( DefaultTokenizer.class );
   private TokenizerRegex tokenizer;
   private TokenWord wordprocessor;

   public DefaultTokenizer() {
       super();
       this.addPreProcessor(RemoveHtmlComment.class);
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
       this.addPreProcessor(ConvertUnicodeDiacritics.class);
       this.addPreProcessor(new RemoveNonASCII(this, true));

       this.addSectionProcess("all", "tokenize", "result");

       this.addProcess("tokenize", ConvertHtmlAmpersand.class);
       this.addProcess("tokenize", RemoveHtmlTags.class);
       this.addProcess("tokenize", RemoveHtmlSpecialCodes.class);
       createTokenizer();
       this.addProcess("tokenize", tokenizer);
   }
   
   public TokenizerRegex getTokenizer() {
       return tokenizer;
   }
   
   public TokenWord getTokenprocessor() {
       return wordprocessor;
   }
   
   public void addEndPipeline(Class clazz) {
       this.addProcess("tokenize", clazz);
   }
   
   private void createTokenizer() {
       tokenizer = new TokenizerRegex(this, "tokenize");
       wordprocessor = (TokenWord)tokenizer.setupTokenProcessor("word", TokenWord.class);
   }
   
   public ArrayList<String> tokenize(byte content[]) {
       Entity entity = new Entity();
       entity.setContent(content);
       this.process(entity);
       return entity.get("result");
   }
   
   public ArrayList<String> tokenize(String text) {
       return tokenize(text.getBytes());
   }
}
