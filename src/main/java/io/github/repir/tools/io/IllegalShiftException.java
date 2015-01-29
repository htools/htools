package io.github.repir.tools.io;

import io.github.repir.tools.lib.PrintTools;

/**
 * Indicates that the buffer needed to be shifted to read more data to read
 * a chunk of some size. The buffer was positioned at 0, indicating that 
 * the data chunk size exceeds the buffer size. This is usually fatal.
 * Special large chunk reading methods should overcome this by aggregating their 
 * data as a stream rather than requiring the whole chunk to be in the buffer.
 * @author Jeroen Vuurens
 */
public class IllegalShiftException extends DataException {
    
    public IllegalShiftException(String s) {
        super(PrintTools.sprintf("Illegal shift from currentpos 0, indicates that the"
                + "size of the buffer is not sufficient for the size to match"
                + "data chunks read %s", s));
    }
}
