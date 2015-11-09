package io.github.htools.type;

import io.github.htools.collection.HashMapInt;
import io.github.htools.fcollection.FHashMapObjectInt;
import io.github.htools.io.EOCException;
import io.github.htools.io.buffer.BufferSerializable;
import io.github.htools.io.struct.StructureReader;
import io.github.htools.io.struct.StructureWriter;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author jeroen
 */
public class TermVectorInt extends FHashMapObjectInt<String> implements BufferSerializable, TermVector {

    protected Integer total = null;
    protected Double magnitude = null;

    public TermVectorInt() {
    }

    public TermVectorInt(int size) {
        super(size);
    }

    public TermVectorInt(FHashMapObjectInt<String> map) {
        super(map);
    }

    public TermVectorInt(String [] keys, int [] values) {
        super(keys, values);
    }
    
    public TermVectorInt(Collection<String> terms) {
        add(terms);
    }

    protected TermVectorInt create() {
        return new TermVectorInt();
    }

    @Override
    public TermVectorInt clone() {
        TermVectorInt clone = new TermVectorInt(size());
        clone.add(this);
        return clone();
    }

    public void toBinary() {
        for (int i = 0; i < value.length; i++) {
            if (value[i] > 1) {
                value[i] = 1;
            }
        }
    }

    public void add(Collection<String> terms) {
        for (String t : terms) {
            super.add(t, 1);
        }
        magnitude = null;
        total = null;
    }

    @Override
    public TermVectorInt add(FHashMapObjectInt<String> v) {
        for (Object2IntMap.Entry<String> entry : v.object2IntEntrySet()) {
            super.add(entry.getKey(), entry.getIntValue());
        }
        magnitude = null;
        total = null;
        return this;
    }

    public void remove(HashMapInt<String> v) {
        super.remove(v);
        magnitude = null;
        total = null;
    }

    public double magnitude() {
        if (magnitude == null) {
            long sum = 0;
            for (int freq : values()) {
                sum += freq * freq;
            }
            magnitude = Math.sqrt(sum);
        }
        return magnitude;
    }

    public double magnitudeOmit(TermVectorInt v) {
        double magnitude = 0;
        int sum = 0;
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            int omitf = v.getInt(entry.getKey());
            int freq = entry.getIntValue() - omitf;
            sum += freq * freq;
        }
        magnitude = Math.sqrt(sum);
        return magnitude;
    }

    public double cossim(TermVectorInt v) {
        if (this.size() > v.size()) {
            return v.cossim(this);
        }
        double dotproduct = 0;
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            dotproduct += v.getInt(entry.getKey()) * entry.getIntValue();
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public double cossim(TermVectorDouble v) {
        if (this.size() > v.size()) {
            return v.cossim(this);
        }
        double dotproduct = 0;
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            dotproduct += v.getDouble(entry.getKey()) * entry.getIntValue();
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }
    
    
    public double cossimDebug(TermVectorInt v) {
        if (this.size() > v.size()) {
            return v.cossimDebug(this);
        }
        double dotproduct = 0;
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            dotproduct += v.getInt(entry.getKey()) * entry.getIntValue();
            log.info("%s %d %d %s", entry.getKey(), v.getInt(entry.getKey()), entry.getIntValue(), dotproduct);
        }
        double magnitude = magnitude() * v.magnitude();
        log.info("magnitude %s %s %s", magnitude(), v.magnitude(), dotproduct / magnitude);
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public double cossimOmit(TermVectorInt v) {
        double dotproduct = 0;
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            dotproduct += v.getInt(entry.getKey()) * entry.getIntValue();
        }
        double magnitude = magnitudeOmit(v) * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public static double dice(TermVectorInt... v) {
        HashSet<String> intersect = new HashSet(v[0].keySet());
        int count = v[0].size();
        LOOP:
        for (int i = 1; i < v.length; i++) {
            intersect.retainAll(v[i].keySet());
            count += v[i].size();
        }
        return v.length * intersect.size() / (double) count;
    }

    public double dice(TermVectorInt v) {
        HashSet<String> intersect = new HashSet(v.keySet());
        return intersect.size() / (double) (size() + v.size());
    }

    public double cossimDouble(TermVectorDouble v) {
        double dotproduct = 0;
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Double freq = v.get(entry.getKey());
            if (freq != null) {
                dotproduct += freq * entry.getValue();
            }
        }
        double magnitude = magnitude() * v.magnitude();
        return (magnitude == 0) ? 0 : dotproduct / magnitude;
    }

    public TermVectorDouble multiplyDouble(TermVectorDouble v) {
        TermVectorDouble result = new TermVectorDouble();
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Double d = v.get(entry.getKey());
            if (d != null) {
                result.put(entry.getKey(), entry.getValue() * d);
            }
        }
        return result;
    }

    public TermVectorDouble divide(double div) {
        return super.divide(new TermVectorDouble(), div);
    }

    public TermVectorInt multiply(TermVectorInt v) {
        TermVectorInt result = new TermVectorInt();
        for (Map.Entry<String, Integer> entry : entrySet()) {
            Integer d = v.get(entry.getKey());
            if (d != null) {
                result.put(entry.getKey(), entry.getValue() * d);
            }
        }
        return result;
    }

    public TermVectorInt getTop(int k) {
        return super.getTop(new TermVectorInt(), k);
    }

    public Integer total() {
        if (total == null) {
            total = 0;
            for (int i : values()) {
                total += i;
            }
        }
        return total;
    }

    public TermVectorDouble normalize() {
        TermVectorDouble result = new TermVectorDouble(size());
        double total = total();
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            result.put(entry.getKey(), entry.getIntValue() / total);
        }
        return result;
    }

    @Override
    public void read(StructureReader reader) throws EOCException {
        int size = reader.readInt();
        int needed = (int)Math.min( 1 << 30, Math.max( 2, HashCommon.nextPowerOfTwo( (long)Math.ceil( (size() + size) / f ) ) ) );
        rehash(needed);
        for (int i = 0; i < size; i++) {
            put(reader.readString(), reader.readInt());
        }
    }

    @Override
    public void write(StructureWriter writer) {
        writer.write(size());
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            writer.write(entry.getKey());
            writer.write(entry.getIntValue());
        }
    }

    @Override
    public TermVectorDouble multiply(Map<String, Double> v) {
        if (size() > v.size() && v instanceof TermVectorDouble)
            return ((TermVectorDouble)v).multiply(this);
        TermVectorDouble result = new TermVectorDouble();
        for (Object2IntMap.Entry<String> entry : object2IntEntrySet()) {
            Double d = v.get(entry.getKey());
            if (d != null) {
                result.put(entry.getKey(), entry.getValue() * d);
            }
        }
        return result;
        
    }

    @Override
    public double cossim(TermVector v) {
        if (v instanceof TermVectorDouble)
            return cossim((TermVectorDouble)v);
        else if (v instanceof TermVectorInt)
            return cossim((TermVectorInt)v);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
