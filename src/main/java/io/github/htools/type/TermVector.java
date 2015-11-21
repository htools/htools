package io.github.htools.type;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jeroen
 */
public interface TermVector {

    public void add(Collection<String> terms);

    public double magnitude();

    public double cossim(TermVector v);

    public TermVectorDouble multiply(Map<String, Double> v);

    public TermVectorDouble divide(double div);

    public TermVectorDouble normalize();
    
    public Set<String> keySet();
    
    public int size();
}
