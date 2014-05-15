package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.PrintTools;

/**
 * Indicates an attempt was made to read a Datafile that was not opened for reading.
 * @author Jeroen Vuurens
 */
public class FileClosedOnReadException extends DataException {
    
    public FileClosedOnReadException(Datafile d) {
        super(PrintTools.sprintf("File '%s' must be opened before reading", d.getFullPath()));
    }
}
