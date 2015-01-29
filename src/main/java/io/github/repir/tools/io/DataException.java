package io.github.repir.tools.io;

import io.github.repir.tools.lib.PrintTools;

/**
 * Indicates an exception while accessing a Buffer/Datafile
 * @author Jeroen Vuurens
 */
public abstract class DataException extends RuntimeException {

    public DataException(String s) {
        super(s);
    }
}
