package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.RandomTools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * An ArrayList that can more easily be sorted.
 * <p/>
 * @author jeroen
 */
public class SortableList<K extends Comparable> extends ArrayList<K> {

    public static Log log = new Log(SortableList.class);

    public SortableList() {
        super();
    }

    public SortableList(Collection<? extends K> c) {
        super(c);
    }

    public SortableList(int initialsize) {
        super(initialsize);
    }

    public SortableList<K> sort() {
        Collections.sort(this);
        return this;
    }

    public SortableList<K> sort(Comparator<K> comparator) {
        Collections.sort(this, comparator);
        return this;
    }

    public SortableList<K> sortDesc() {
        Collections.sort(this, getDescComparator());
        return this;
    }

    protected Comparator<K> getDescComparator() {
        return new DescComparator();
    }

    protected class DescComparator implements Comparator<K> {
        @Override
        public int compare(K o1, K o2) {
            return o2.compareTo(o1);
        }
    }

    public static void main(String[] args) {
        SortableList<Integer> map = new SortableList();
        for (int i = 0; i < 100; i++) {
            map.add(RandomTools.getInt(100));
        }
        for (Integer entry : map) {
            log.printf("%d", entry);
        }
    }
}
