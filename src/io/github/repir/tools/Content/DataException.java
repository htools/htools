package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.PrintTools;

/**
 * Indicates an exception while accessing a Buffer/Datafile
 * @author Jeroen Vuurens
 */
public abstract class DataException extends RuntimeException {

    public DataException(String s) {
        super(s);
    }
}
