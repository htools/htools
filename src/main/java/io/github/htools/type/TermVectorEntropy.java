package io.github.htools.type;

import io.github.htools.fcollection.FHashMapObjectInt;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructureReader;
import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.*;

/**
 *
 * @author jeroen
 */
public class TermVectorEntropy extends TermVectorInt {

    public static final Log log = new Log(TermVectorEntropy.class);
    public static double plogp[] = plogpTable();

    double sumflog = 0;

    public TermVectorEntropy() {
        total = 0;
    }

    public TermVectorEntropy(int size) {
        super(size);
        total = 0;
    }

    public TermVectorEntropy(Collection<String> terms) {
        total = 0;
        add(terms);
    }

    public TermVectorEntropy(String... terms) {
        total = 0;
        for (String term : terms) {
            add(term);
        }
    }

    public TermVectorEntropy(ArrayList<? extends FHashMapObjectInt> collections) {
        for (FHashMapObjectInt<String> map : collections) {
            super.add(map);
        }
        recompute();
    }

    public TermVectorEntropy(FHashMapObjectInt<String> terms) {
        total = 0;
        add(terms);
    }

    @Override
    public TermVectorEntropy clone() {
        TermVectorInt clone = new TermVectorInt(size());
        clone.add(this);
        return clone();
    }

    protected TermVectorEntropy create() {
        return new TermVectorEntropy();
    }

    private static double[] plogpTable() {
        double table[] = new double[1000];
        for (int i = 1; i < table.length; i++) {
            table[i] = -i * MathTools.log2(i);
        }
        return table;
    }

    private static double getPLogP(int i) {
        if (i < plogp.length) {
            return plogp[i];
        }
        return -i * MathTools.log2(i);
    }

    public void retainAll(Set<String> keys) {
        ObjectIterator<Object2IntMap.Entry<String>> fastIterator = this.object2IntEntrySet().fastIterator();
        while (fastIterator.hasNext()) {
            Object2IntMap.Entry<String> next = fastIterator.next();
            if (!keys.contains(next.getKey())) {
                fastIterator.remove();
            }
        }
    }

    @Override
    public void add(String term, int value) {
        int oldvalue = getInt(term);
        if (oldvalue > 1) {
            sumflog -= getPLogP(oldvalue);
        }
        int newvalue = oldvalue + value;
        if (newvalue == 0) {
            remove(term);
        } else {
            put(term, newvalue);
        }
        if (newvalue > 1) {
            sumflog += getPLogP(newvalue);
        }
        total += value;
    }

    public void recompute() {
        total = 0;
        sumflog = 0;
        magnitude = null;
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            if (entry.getIntValue() > 1) {
                sumflog += getPLogP(entry.getIntValue());
            }
            total += entry.getIntValue();
        }
    }

    @Override
    public void add(Collection<String> terms) {
        for (String t : terms) {
            int count = getInt(t);
            if (count == 0) {
                put(t, 1);
            } else {
                if (count > 1) {
                    sumflog -= getPLogP(count);
                }
                put(t, ++count);
                sumflog += getPLogP(count);
            }
            total++;
        }
        magnitude = null;
    }

    public TermVectorInt add(FHashMapObjectInt<String> v) {
        for (Object2IntMap.Entry<String> entry : v.object2IntEntrySet()) {
            int oldfreq = getInt(entry.getKey());
            int newfreq = entry.getIntValue() + oldfreq;
            if (oldfreq > 1) {
                sumflog -= getPLogP(oldfreq);
            }
            put(entry.getKey(), newfreq);
            sumflog += getPLogP(newfreq);
            total += entry.getIntValue();
        }
        magnitude = null;
        return this;
    }

    @Override
    public void remove(FHashMapObjectInt<String> v) {
        for (Object2IntMap.Entry<String> entry : v.object2IntEntrySet()) {
            int current = getInt(entry.getKey());
            sumflog -= getPLogP(current);
            if (entry.getIntValue() > current) {
                log.fatal("remove cannot remove value greater than current key %s current %d remove %d", entry.getKey(), current, entry.getValue());
            } else if (entry.getIntValue() == current) {
                remove(entry.getKey());
            } else {
                put(entry.getKey(), current - entry.getIntValue());
                sumflog += getPLogP(current - entry.getIntValue());
                total -= entry.getIntValue();
            }
        }
        magnitude = null;
    }

    public Integer remove(String key) {
        int value = super.removeInt(key);
        if (value > 0) {
            sumflog -= getPLogP(value);
            total -= value;
        }
        return value;
    }

    public double getsumflog() {
        return sumflog;
    }

    public double entropy() {
        return entropy(total());
    }

    public double entropynorm() {
        return entropy(total()) / MathTools.log2(total());
    }

    public double entropy(int total) {
        return getsumflog() / total + total() * MathTools.log2(total) / total;
    }

    public double getSumFLogOmit(TermVectorEntropy omit) {
        double sl = getsumflog();
        for (Object2IntMap.Entry<String> o : omit.object2IntEntrySet()) {
            int f = getInt(o.getKey());
            sl -= getPLogP(f);
            f -= o.getIntValue();
            sl += getPLogP(f);
        }
        return sl;
    }

    public double entropy(double sumflog, double total) {
        return sumflog / total + (total) * MathTools.log2(total) / total;
    }

    public double maxentropy(TermVectorEntropy vector2) {
        int total = total() + vector2.total();
        return entropy(total) + vector2.entropy(total);
    }

    public static double maxentropy(ArrayList<TermVectorEntropy> vectors, int total) {
        double entropy = 0; //entropy(total);
        for (TermVectorEntropy v : vectors) {
            entropy += v.entropy(total);
        }
        return entropy;
    }

    public double partialentropy(int total) {
        return entropy() * total() / (double) (total);
    }

    public static int total(ArrayList<TermVectorEntropy> vectors) {
        int total = 0;
        for (TermVectorEntropy v : vectors) {
            total += v.total();
        }
        return total;
    }

    public double combinedEntropy(TermVectorEntropy vector2, int total) {
        double sumflog = 0;
        for (Object2IntMap.Entry<String> entry : this.object2IntEntrySet()) {
            int freq = entry.getIntValue() + vector2.getInt(entry.getKey());
            sumflog += getPLogP(freq);
        }
        for (Object2IntMap.Entry<String> entry : vector2.object2IntEntrySet()) {
            if (!containsKey(entry.getKey())) {
                sumflog += getPLogP(entry.getIntValue());
            }
        }
        return sumflog / total + MathTools.log2(total);
    }

    public double combinedEntropy3(TermVectorEntropy vector2, int total) {
        if (vector2.size() > size()) {
            return vector2.combinedEntropy3(this, total);
        }
        double sumflog = this.getsumflog() + vector2.getsumflog();
        for (Object2IntMap.Entry<String> entry : vector2.object2IntEntrySet()) {
            if (containsKey(entry.getKey())) {
                int f = getInt(entry.getKey());
                sumflog += getPLogP(f + entry.getIntValue()) - getPLogP(f) - getPLogP(entry.getIntValue());
            }
        }
        return sumflog / total + MathTools.log2(total);
    }

    public double combinedSumFLog(TermVectorEntropy vector2) {
        double sumflog = this.getsumflog() + vector2.getsumflog();
        if (size() < vector2.size()) {
            for (Object2IntMap.Entry<String> entry : this.object2IntEntrySet()) {
                if (vector2.containsKey(entry.getKey())) {
                    int f = getInt(entry.getKey());
                    sumflog += getPLogP(f + entry.getIntValue()) - getPLogP(f) - getPLogP(entry.getIntValue());
                }
            }
        } else {
            for (Object2IntMap.Entry<String> entry : vector2.object2IntEntrySet()) {
                if (containsKey(entry.getKey())) {
                    int f = getInt(entry.getKey());
                    sumflog += getPLogP(f + entry.getIntValue()) - getPLogP(f) - getPLogP(entry.getIntValue());
                }
            }
        }
        return sumflog;
    }

    public double updateCombinedSumFLog(double sumflog, TermVectorEntropy vector2, TermVectorEntropy newterms) {
        for (Object2IntMap.Entry<String> entry : newterms.object2IntEntrySet()) {
            int newf = entry.getIntValue();
            int updf = getInt(entry.getKey()) + vector2.getInt(entry.getKey());
            int oldf = updf - newf;
            if (oldf > 1)
                sumflog -= getPLogP(oldf);
            sumflog += getPLogP(updf);
        }
        return sumflog;
    }

    public double combinedEntropy2(TermVectorEntropy vector2) {
        TermVectorEntropy t = new TermVectorEntropy();
        t.add(this);
        t.add(vector2);
        return t.entropy();
    }

    public static double combinedEntropy(ArrayList<TermVectorEntropy> vectors, int total) {
        TermVectorEntropy c = new TermVectorEntropy(vectors);
        return c.partialentropy(total);
    }

    public double ignorm(TermVectorEntropy vector2) {
        int total = total() + vector2.total();
        double p1 = partialentropy(total);
        double p2 = vector2.partialentropy(total);
        double split = p1 + p2;
        //double combined = combinedEntropy2(vector2);
        double combined = combinedEntropy(vector2, total);
        double ig = combined - split;
        double entropymax = maxentropy(vector2);
        return (entropymax > split) ? ig / (entropymax - split) : 1;
    }

    public double ignorm2(TermVectorEntropy vector2) {
        int total = total() + vector2.total();
        double p1 = partialentropy(total);
        double p2 = vector2.partialentropy(total);
        double split = p1 + p2;
        double entropymax = maxentropy(vector2);
        if (entropymax > split) {
            double combined = combinedEntropy3(vector2, total);
            return (combined - split) / (entropymax - split);
        }
        return 1;
    }

    public double ig(TermVectorEntropy vector2) {
        int total = total() + vector2.total();
        double p1 = partialentropy(total);
        double p2 = vector2.partialentropy(total);
        double split = p1 + p2;
        //double combined = combinedEntropy2(vector2);
        double combined = combinedEntropy(vector2, total);
        return combined - split;
    }

    public double ignormOmitting(TermVectorEntropy vector2) {
        double sumflogomit = this.getSumFLogOmit(vector2);
        double totalomit = total() - vector2.total();
        double p1 = entropy(sumflogomit, totalomit) * (totalomit) / total();
        double p2 = vector2.partialentropy(total());
        double split = p1 + p2;
        double combined = entropy();
        double ig = combined - split;
        double entropymax = entropy(sumflogomit, total()) + vector2.entropy(total());
        return (entropymax > split) ? ig / (entropymax - split) : 1;
    }

    public static double ignorm(ArrayList<TermVectorEntropy> vectors) {
        int total = total(vectors);
        double lowentropy = 0; //partialentropy(total);
        for (TermVectorEntropy t : vectors) {
            lowentropy += t.partialentropy(total);
        }
        double highentropy = combinedEntropy(vectors, total); // + partialentropy(total);
        double ig = highentropy - lowentropy;
        double entropymax = maxentropy(vectors, total);
        return (entropymax > lowentropy) ? ig / (entropymax - lowentropy) : 1;
    }

    public static HashSet<String> purity(Collection<? extends FHashMapObjectInt<String>> vectors, double threshold) {
        HashSet<String> result = new HashSet();
        HashMap<String, TermVectorEntropy> terms = new HashMap();
        for (FHashMapObjectInt<String> vector : vectors) {
            for (String t : vector.keySet()) {
                TermVectorEntropy vectort = terms.get(t);
                if (vectort == null) {
                    vectort = new TermVectorEntropy();
                    terms.put(t, vectort);
                    result.add(t);
                } else {
                    result.remove(t);
                }
                vectort.add(vector);
            }
        }
        HashSet<String> remove = null;
        while (remove == null || remove.size() > 0) {
            log.info("vectors %s terms %s remove %s", vectors.size(), terms.size(), remove);
            if (remove != null) {
                for (String term : remove) {
                    terms.remove(term);
                }
                for (TermVectorEntropy vector : terms.values()) {
                    for (String term : remove) {
                        vector.remove(term);
                    }
                }
            }
            remove = new HashSet();
            for (Map.Entry<String, TermVectorEntropy> entry : terms.entrySet()) {
                entry.getValue().remove(entry.getKey());
                if (!result.contains(entry.getKey())) {
                    double entropy = entry.getValue().entropynorm();
                    if (entropy > threshold) {
                        remove.add(entry.getKey());
                    }
                }
            }
        }
        result.addAll(terms.keySet());
        return result;
    }

    @Override
    public void read(StructureReader reader) throws EOCException {
        super.read(reader);
        recompute();
    }

    public static void main2(String[] args) {
        TermVectorEntropy t = new TermVectorEntropy("preview", "guerrera", "street", "street", "street", "book", "mean", "wsj",
                "marketwatch", "bank", "bank", "claim", "everi", "everi", "video", "wall", "wall", "francesco");
        //t.add("1");
        //t.add("2");
        //t.add("3");
        //t.add("4");
        TermVectorEntropy s = new TermVectorEntropy("cbc", "news", "bay", "nfld", "nfld", "fishplant", "st", "labrador", "mari", "fire");
        //s.add("1");
        //s.add("2");
        //s.add("2");
        log.info("ign %d %d %f %f %f %f %f %f %f %f", t.total(), s.total(), t.getsumflog(),
                s.getsumflog(), t.entropy(), s.entropy(), t.combinedEntropy(s, t.total() + s.total()),
                t.combinedEntropy3(s, t.total() + s.total()), t.ignorm(s), t.ignorm2(s));
    }

    public static void main(String[] args) {
        TermVectorEntropy t = new TermVectorEntropy();
        for (int i = 1; i < 4; i++) {
            t.add("" + i, i);
        }
        TermVectorEntropy s = new TermVectorEntropy();

        log.info("ign %d %d %f %f %f %f %f %f %f %f", t.total(), s.total(), t.getsumflog(),
                s.getsumflog(), t.entropy(), s.entropy(), t.combinedEntropy(s, t.total() + s.total()),
                t.combinedEntropy3(s, t.total() + s.total()), t.ignorm(s), t.ignorm2(s));
    }
}
