package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Collection;

/**
 *
 * @author iloen
 */
public class FHashSetLong extends LongOpenHashSet {
    public static Log log = new Log(FHashSetLong.class);

    public FHashSetLong() {
        super();
    }
    
    public FHashSetLong(Collection<Long> collection) {
        super(collection);
    }
    
    public FHashSetLong(Collection<Long> collection, float loadfactor) {
        super(collection, loadfactor);
    }
    
    public FHashSetLong(int initialsize, float loadfactor) {
        super(initialsize, loadfactor);
    }
    
    public FHashSetLong(int initialsize) {
        super(initialsize);
    }
}
