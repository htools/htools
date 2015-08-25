package io.github.htools.type;

import io.github.htools.collection.HashMapInt;
import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructureReader;
import io.github.htools.io.struct.StructureWriter;
import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;
import io.github.htools.type.TermVectorInt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public TermVectorEntropy(ArrayList<? extends HashMapInt> collections) {
        for (HashMapInt<String> map : collections) {
            super.add(map);
        }
        recompute();
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

    public TermVectorEntropy retainAll(Set<String> keys) {
        TermVectorEntropy result = new TermVectorEntropy();
        for (Map.Entry<String, Integer> entry : entrySet()) {
            if (keys.contains(entry.getKey())) {
                result.add(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public void add(String term, int value) {
        Integer oldvalue = get(term);
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
        for (Map.Entry<String, Integer> entry : entrySet()) {
            if (entry.getValue() > 1) {
                sumflog += getPLogP(entry.getValue());
            }
            total += entry.getValue();
        }
    }

    @Override
    public void add(Collection<String> terms) {
        for (String t : terms) {
            int count = get(t);
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

    public TermVectorInt add(HashMapInt<String> v) {
        for (Map.Entry<String, Integer> entry : v.entrySet()) {
            Integer oldfreq = get(entry.getKey());
            Integer newfreq = entry.getValue() + oldfreq;
            if (oldfreq > 1) {
                sumflog -= getPLogP(oldfreq);
            }
            put(entry.getKey(), newfreq);
            sumflog += getPLogP(newfreq);
            total += entry.getValue();
        }
        magnitude = null;
        return this;
    }

    @Override
    public void remove(HashMapInt<String> v) {
        for (Map.Entry<String, Integer> entry : v.entrySet()) {
            Integer current = get(entry.getKey());
            sumflog -= getPLogP(current);
            if (entry.getValue() > current) {
                log.fatal("remove cannot remove value greater than current key %s current %d remove %d", entry.getKey(), current, entry.getValue());
            } else if (entry.getValue() == current) {
                remove(entry.getKey());
            } else {
                put(entry.getKey(), current - entry.getValue());
                sumflog += getPLogP(current - entry.getValue());
                total -= entry.getValue();
            }
        }
        magnitude = null;
    }

    public Integer remove(String key) {
        Integer value = super.remove(key);
        if (value != null) {
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
        for (Map.Entry<String, Integer> o : omit.entrySet()) {
            Integer f = get(o.getKey());
            sl -= getPLogP(f);
            f -= o.getValue();
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
        for (Map.Entry<String, Integer> entry : entrySet()) {
            int freq = entry.getValue() + vector2.get(entry.getKey());
            sumflog += getPLogP(freq);
        }
        for (Map.Entry<String, Integer> entry : vector2.entrySet()) {
            if (!containsKey(entry.getKey())) {
                sumflog += getPLogP(entry.getValue());
            }
        }
        return sumflog / total + MathTools.log2(total);
    }

    public double combinedEntropy3(TermVectorEntropy vector2, int total) {
        if (vector2.size() > size()) {
            return vector2.combinedEntropy3(this, total);
        }
        double sumflog = this.getsumflog() + vector2.getsumflog();
        for (Map.Entry<String, Integer> entry : vector2.entrySet()) {
            if (containsKey(entry.getKey())) {
                int f = get(entry.getKey());
                sumflog += getPLogP(f + entry.getValue()) - getPLogP(f) - getPLogP(entry.getValue());
            }
        }
        return sumflog / total + MathTools.log2(total);
    }

    public double combinedSumFLog(TermVectorEntropy vector2) {
        double sumflog = this.getsumflog() + vector2.getsumflog();
        if (size() < vector2.size()) {
            for (Map.Entry<String, Integer> entry : entrySet()) {
                if (vector2.containsKey(entry.getKey())) {
                    int f = get(entry.getKey());
                    sumflog += getPLogP(f + entry.getValue()) - getPLogP(f) - getPLogP(entry.getValue());
                }
            }
        } else {
            for (Map.Entry<String, Integer> entry : vector2.entrySet()) {
                if (containsKey(entry.getKey())) {
                    int f = get(entry.getKey());
                    sumflog += getPLogP(f + entry.getValue()) - getPLogP(f) - getPLogP(entry.getValue());
                }
            }
        }
        return sumflog;
    }

    public double updateCombinedSumFLog(double sumflog, TermVectorEntropy vector2, TermVectorEntropy newterms) {
        for (Map.Entry<String, Integer> entry : newterms.entrySet()) {
            int newf = entry.getValue();
            int updf = get(entry.getKey()) + vector2.get(entry.getKey());
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

    public static HashSet<String> purity(Collection<? extends HashMapInt<String>> vectors, double threshold) {
        HashSet<String> result = new HashSet();
        HashMap<String, TermVectorEntropy> terms = new HashMap();
        for (HashMapInt<String> vector : vectors) {
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
