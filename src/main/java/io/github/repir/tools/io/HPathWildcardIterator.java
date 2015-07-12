package io.github.repir.tools.io;

import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.lib.IteratorIterable;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Dir class represents a wildcarded directory, through which you can
 * iterate using the iterator().
 * <p/>
 * @author jbpvuurens
 */
public class HPathWildcardIterator implements IteratorIterable<DirComponent> {

    public static Log log = new Log(HPathWildcardIterator.class);
    HPath hpath;
    private ArrayDeque<String> components;
    private ArrayList<ByteSearch> regex = new ArrayList();
    private ArrayList<Iterator<DirComponent>> contents = new ArrayList();
    DirComponent lastcomponent;

    protected HPathWildcardIterator(HPath path) {
        set(path);
    }

    protected void set(HPath path) {
        components = new ArrayDeque();
        components.addFirst(path.getName());
        path = path.getParentPath();
        while (!path.exists()) {
            components.addFirst(path.getName());
            path = path.getParentPath();
        }
        hpath = path;
    }

    @Override
    public IteratorIterable<DirComponent> iterator() {
        for (String component : components) {
            regex.add(ByteSearch.createFilePattern(component));
        }
        setupIterator(hpath, 0);

        return this;
    }

    public boolean setupIterator(HPath p, int i) {
        try {
            ByteSearch r = regex.get(i);
            Iterator<DirComponent> iter;
            if (i < regex.size() - 1) {
                iter = p.iteratorDirs(r);
            } else {
                iter = p.iterator(r);
            }
            contents.add(iter);
            if (iter.hasNext()) {
                DirComponent next = iter.next();
                if (next instanceof HPath) {
                    p = (HPath) next;
                    if (i < regex.size() - 1) {
                        return setupIterator(p, i + 1);
                    }
                }
                if (i == regex.size() - 1) {
                    lastcomponent = next;
                    return true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HPathWildcardIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        return lastcomponent != null;
    }

    @Override
    public DirComponent next() {
        DirComponent result = lastcomponent;
        lastcomponent = null;
        Iterator<DirComponent> iter = contents.get(contents.size() - 1);
        if (iter.hasNext()) {
            lastcomponent = iter.next();
        } else {
            for (int i = regex.size() - 1; i >= 0; i--) {
                iter = contents.get(i);
                if (iter.hasNext()) {
                    HPath next = (HPath) iter.next();
                    setupIterator(next, i + 1);
                    break;
                } else {
                    contents.remove(i);
                }
            }
        }
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
