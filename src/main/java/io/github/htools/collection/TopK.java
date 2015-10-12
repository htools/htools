package io.github.htools.collection;

import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.RandomTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * This collection retains the TopK objects that are added. Instantiate with any
 * int k, where a negative value means to retain the BottomK (-k) objects, where
 * k is the maximum number of objects to keep, and a inverse Comparator, i.e.
 * the smallest element should return -1. TopK does not sort the objects.
 *
 * @author Jeroen Vuurens
 */
public class TopK<T> extends PriorityQueue<T> {

    public static Log log = new Log(TopK.class);
    final Comparator<? super T> comparator;
    final int k;
    T lowest;
    T highest;

    public TopK(int k, Comparator<? super T> comparator) {
        super(k, comparator);
        this.comparator = comparator;
        this.k = k;
    }

    public TopK(int k, Collection<T> collection, Comparator<? super T> comparator) {
        this(k, comparator);
        this.addAll(collection);
    }

    public TopK(int k, Collection<T> collection) {
        this(k);
        this.addAll(collection);
    }

    public TopK(int k) {
        this(Math.abs(k), (k > 0) ? new StdComparator() : Collections.reverseOrder(new StdComparator()));
    }

    @Override
    public boolean add(T t) {
        boolean r = false;
        if (size() < k) {
            r = super.add(t);
            if (lowest == null || comparator.compare(t, lowest) < 0) {
                lowest = t;
            }
            if (highest == null || comparator.compare(t, highest) > 0) {
                highest = t;
            }
        } else if (comparator.compare(t, lowest) > 0) {
            this.poll();
            r = super.add(t);
            lowest = this.peek();
            if (comparator.compare(t, highest) > 0) {
                highest = t;
            }
        }
        return r;
    }

    public boolean wouldBeAdded(T t) {
        return (size() < k || comparator.compare(t, lowest) > 0);
    }

    public T first() {
        return highest;
    }

    public ArrayList<T> sorted() {
        ArrayList<T> list = new ArrayList(this);
        Collections.sort(list, Collections.reverseOrder(comparator));
        return list;
    }

    public static class StdComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }
    }

    public static void main(String[] args) {
        String l[] = new String[1000000];
        for (int i = 0; i < l.length; i++) {
            l[i] = RandomTools.uuid();
        }
        TopK<String> topk = new TopK<String>(10000);
        for (String s : l) {
            topk.add(s);
        }
      //for (String s : topk)
        //   log.reportTime("%s", s);
        log.info("topk lowst %s highest %s", topk.lowest, topk.highest);
        String t = topk.lowest;
        for (String s : l) {
            if (s.compareTo(t) > 0 && !topk.contains(s)) {
                log.info("FAULT %s", s);
            }
        }

    }
}
