package io.github.repir.tools.io;

import io.github.repir.tools.lib.PrintTools;

/**
 * Indicates an attempt was made to read a Datafile that was not opened for reading.
 * @author Jeroen Vuurens
 */
public class FileClosedOnReadException extends DataException {
    
    public FileClosedOnReadException(Datafile d) {
        super(PrintTools.sprintf("File '%s' must be opened before reading", d.getCanonicalPath()));
    }
}
