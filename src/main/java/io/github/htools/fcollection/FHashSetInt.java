package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;

/**
 *
 * @author iloen
 */
public class FHashSetInt extends IntOpenHashSet {
    public static Log log = new Log(FHashSetInt.class);

    public FHashSetInt() {
        super();
    }
    
    public FHashSetInt(Collection<Integer> collection) {
        super(collection);
    }
    
    public FHashSetInt(Collection<Integer> collection, float loadfactor) {
        super(collection, loadfactor);
    }
    
    public FHashSetInt(int initialsize, float loadfactor) {
        super(initialsize, loadfactor);
    }
    
    public FHashSetInt(int initialsize) {
        super(initialsize);
    }
}
