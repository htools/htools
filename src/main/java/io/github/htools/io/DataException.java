package io.github.htools.io;

/**
 * Indicates an exception while accessing a Buffer/Datafile
 * @author Jeroen Vuurens
 */
public abstract class DataException extends RuntimeException {

    public DataException(String s) {
        super(s);
    }
}
