package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.Pair;

import java.util.Iterator;
import java.util.TreeSet;

import static io.github.htools.lib.PrintTools.sprintf;

/**
 * Maintains clusters of type K, of are merged when two elements are said to be
 * in the same cluster.
 *
 * @author jeroen
 */
public class SelectionInterval<K extends Comparable> implements Iterable<Pair<K,K>> {

    public static Log log = new Log(SelectionInterval.class);
    public TreeSet<Interval<K>> set = new TreeSet();
    
    public SelectionInterval() {
    }

    public void addInterval(K start, K end) {
        Interval ni = new Interval(start, end);
        Interval floor = set.floor(ni);
        while (floor != null) {
            if (!floor.touches(ni))
                break;
            ni = ni.merge(floor);
            set.remove(floor);
            floor = set.floor(ni);
        }
        Interval ceiling = set.ceiling(ni);
        while (ceiling != null) {
            if (!ceiling.touches(ni))
                break;
            ni = ni.merge(ceiling);
            set.remove(ceiling);
            ceiling = set.ceiling(ni);
        }
        set.add(ni);
    }

    public void removeInterval(K start, K end) {
        Interval ni = new Interval(start, end);
        Interval floor = set.floor(ni);
        while (floor != null && floor.overlaps(ni)) {
            set.remove(floor);
            if (floor.getKey().compareTo(start) < 0) {
                set.add(new Interval(floor.getKey(), start));
            }
            if (floor.getValue().compareTo(end) > 0) {
                set.add(new Interval(end, floor.getValue()));
            }
            floor = set.floor(ni);
        }
        Interval ceil = set.ceiling(ni);
        while (ceil != null && ceil.overlaps(ni)) {
            set.remove(ceil);
            if (ceil.getValue().compareTo(end) > 0) {
                set.add(new Interval(end, ceil.getValue()));
            }
            ceil = set.ceiling(ni);
        }
    }

    public boolean contains(K start, K end) {
        Interval<K> interval = new Interval(start, end);
        Interval<K> floor = set.floor(interval);
        return (floor != null && floor.end.compareTo(end) >= 0);
    }
    
    public boolean overlaps(K start, K end) {
        Interval<K> interval = new Interval(start, end);
        Interval<K> floor = set.floor(interval);
        if (floor != null && floor.end.compareTo(start) > 0)
            return true;
        Interval<K> ceiling = set.ceiling(interval);
        if (ceiling != null && ceiling.start.compareTo(end) < 0)
            return true;
        return false;
    }
    
    public Iterator<Pair<K,K>> iterator() {
        return new Iterator<Pair<K,K>>(){
            Iterator<Interval<K>> iter = set.iterator();

            @Override
            public boolean hasNext() {
               return iter.hasNext();
            }

            @Override
            public Pair<K, K> next() {
                return iter.next();
            }
            
            public void remove() {
                iter.remove();
            }
        };
    }
    
    public Iterable<Pair<K, K>> missed(K start, K end) {
       return new MissedIterator(new Interval(start, end));
    }

    class MissedIterator implements Iterator<Pair<K, K>>, Iterable<Pair<K, K>> {

        Interval all;
        Interval current;
        Iterator<Interval<K>> iter;

        public MissedIterator(Interval interval) {
            this.iter = set.iterator();
            all = interval;
            if (set.size() > 0) {
                if (all.start.compareTo(set.first().start) < 0)
                    current = new Interval(null, all.start);
                else if (set.first().end.compareTo(all.end) < 0)
                    current = new Interval(null, set.first().end);
            }
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Interval next() {
            if (iter.hasNext()) {
                Interval i = iter.next();
                Interval result = new Interval(current.end, i.start);
                current = (i.end.compareTo(all.end) < 0)?i:null;
                return result;
            } else {
                Interval result = new Interval(current.end, all.end);
                current = null;
                return result;
            }
        }
        
        @Override
        public void remove() {
        }

        @Override
        public Iterator<Pair<K,K>> iterator() {
            return this;
        }
    }

    static class Interval<K extends Comparable> implements Pair<K, K>, Comparable<Interval> {

        K start;
        K end;

        public Interval(K start, K end) {
            this.start = start;
            this.end = end;
        }

        public boolean touches(Interval o) {
            if (start.compareTo(o.start) < 0) {
                return (end.compareTo(o.start) >= 0);
            } else {
                return (o.end.compareTo(start) >= 0);
            }
        }

        public boolean overlaps(Interval o) {
            if (start.compareTo(o.start) < 0) {
                return (end.compareTo(o.start) > 0);
            } else {
                return (o.end.compareTo(start) > 0);
            }
        }

        public boolean contains(K pos) {
            return (start.compareTo(pos) <= 0) && (end.compareTo(pos) > 0);
        }

        public Interval merge(Interval o) {
            return new Interval(start.compareTo(o.start) < 0 ? start : o.start,
                    end.compareTo(o.end) > 0 ? end : o.end
            );
        }
        
        @Override
        public int compareTo(Interval o) {
            return start.compareTo(o.start);
        }

        @Override
        public K getKey() {
            return start;
        }

        @Override
        public K getValue() {
            return end;
        }
        
        public String toString() {
            return sprintf("[%d,%d]", start, end);
        }
    }
    
    public static void main(String[] args) {
        SelectionInterval<Integer> s = new SelectionInterval();
        s.addInterval(10, 30);
        s.addInterval(100, 300);
        s.addInterval(40, 50);
        s.addInterval(60, 70);
        s.addInterval(30, 60);
        s.addInterval(1000, 1010);
        s.addInterval(1020, 1030);
        s.addInterval(1031, 1040);
        s.addInterval(1005, 1025);
        for (Pair<Integer, Integer> i : s.missed(0, 2000)) {
            log.info("%d %d", i.getKey(), i.getValue());
        }
        log.info("%b %b", s.contains(0, 10), s.overlaps(0,10));
        log.info("%b %b", s.contains(0, 11), s.overlaps(0,11));
        log.info("%b %b", s.contains(10,70), s.overlaps(10,70));
        log.info("%b %b", s.contains(11,70), s.overlaps(11,70));
        log.info("%b %b", s.contains(10,69), s.overlaps(10,69));
        log.info("%b %b", s.contains(70,100), s.overlaps(70, 100));
        log.info("%b %b", s.contains(69,100), s.overlaps(69, 100));
        log.info("%b %b", s.contains(70,101), s.overlaps(70, 101));
        log.info("%b %b", s.contains(1030, 1031), s.overlaps(1030, 1031));
        log.info("%b %b", s.contains(1040, 2000), s.overlaps(1040, 2000));
    }
}
