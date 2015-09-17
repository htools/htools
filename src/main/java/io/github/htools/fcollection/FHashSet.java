package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;

/**
 *
 * @author iloen
 */
public class FHashSet<K> extends ObjectOpenHashSet<K> {
    public static Log log = new Log(FHashSet.class);

    public FHashSet() {
        super();
    }
    
    public FHashSet(Collection<? extends K> collection) {
        super(collection);
    }
    
    public FHashSet(Collection<? extends K> collection, float loadfactor) {
        super(collection, loadfactor);
    }
    
    public FHashSet(int initialsize, float loadfactor) {
        super(initialsize, loadfactor);
    }
    
    public FHashSet(int initialsize ) {
        super(initialsize);
    }
}
