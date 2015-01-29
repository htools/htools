package io.github.repir.tools.search;

import io.github.repir.tools.lib.Log;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 *
 * @author Jeroen Vuurens
 */
public class DescriptiveErrorListener extends BaseErrorListener {
   public static Log log = new Log(DescriptiveErrorListener.class);
    public static DescriptiveErrorListener INSTANCE = new DescriptiveErrorListener();

   @Override
   public void syntaxError(Recognizer<?, ?> recognizer,
           Object offendingSymbol,
           int line,
           int charPositionInLine,
           String msg,
           RecognitionException e) {
      String sourceName = recognizer.getInputStream().getSourceName();
      log.fatalexception(e, "Syntax Error ByteRegex source %s line %d pos %d msg %s", 
              sourceName, line, charPositionInLine, msg);
   }
}
