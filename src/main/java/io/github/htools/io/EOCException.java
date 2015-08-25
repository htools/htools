package io.github.htools.io;

import io.github.htools.lib.PrintTools;

/**
 * Indicates an attempt was made to read past End Of Content, in which to Content
 * can be a file or buffer. EOC can be reached because EOF was encountered or alternatively
 * that the ceiling for a file or buffer was reached. For support of file handling
 * across regular filing system, HDFS, and compressed files, one has to realize that
 * EOF is not always predictable, therefore reading must often be continued until
 * EOC is reached. Therefore EOC is rarely a fatal Exception, but merely an indication 
 * that reading is done.
 * <p>
 * The Exception must be caught when not fatal, otherwise it will be propagated up
 * end end up as a fatal Exception (i.e. if encountering EOC is fatal, don't catch and
 * you get just that).
 * @author Jeroen Vuurens
 */
public class EOCException extends DataException {

    /**
     * Constructs an <code>EOCException</code> with the specified detail
     * message. The string <code>s</code> may later be retrieved by the
     * <code>{@link java.lang.Throwable#getMessage}</code> method of class
     * <code>java.lang.Throwable</code>.
     *
     * @param   s   the detail message.
     */
    public EOCException(String s) {
        super(s);
    }
    
    public EOCException(String s, Object ... p) {
        super(PrintTools.sprintf(s, p));
    }
}
