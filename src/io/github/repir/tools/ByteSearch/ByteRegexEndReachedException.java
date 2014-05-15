package io.github.repir.tools.ByteSearch; 

/**
 * Signals that if more bytes were in the buffer, a larger match would have been
 * possible. The caller should respond by checking if the stream contains more data,
 * if not the exception contains the end position matched, if so it should fill
 * the buffer en retry matching.
 * @author Jeroen Vuurens
 */
public class ByteRegexEndReachedException extends Exception {
   int endpos;
   
   public ByteRegexEndReachedException( int pos ) {
      this.endpos = pos;
   }
}
