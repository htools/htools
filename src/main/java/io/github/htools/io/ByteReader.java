package io.github.htools.io;

import java.io.IOException;

/**
 *
 * @author jbpvuurens
 */
public interface ByteReader {

    public void closeRead() throws IOException;

    public boolean hasMore();

    public long getOffset();
    
    /**
     * @return the next byte read, or -1 when and EOF is encountered in an archive 
     * @throws java.io.IOException 
     */
    public int readByte() throws IOException, EOCException ;
}
