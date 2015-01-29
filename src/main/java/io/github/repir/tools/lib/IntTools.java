package io.github.repir.tools.lib;

import static io.github.repir.tools.lib.MathTools.welchDegreesOfFreedom;
import java.util.Collection;
import org.apache.commons.math3.distribution.TDistribution;

/**
 *
 * @author jeroen
 */
public enum IntTools {
    ;

    /**
     *
     * @param x
     * @return
     */
    public static int min(int ... x) {
        int min = (x.length > 0) ? x[0] : null;
        for (int i = 1; i < x.length; i++) {
            min = java.lang.Math.min(min, x[i]);
        }
        return min;
    }

    public static int min3(int x, int y, int z) {
        return java.lang.Math.min(x, java.lang.Math.min(y, z));
    }

    public static int min(Collection<Integer> x) {
        if (x.size() == 0) {
            return io.github.repir.tools.lib.Const.NULLINT;
        }
        int min = Integer.MAX_VALUE;
        for (int i : x) {
            min = java.lang.Math.min(min, i);
        }
        return min;
    }

    public static int max(Collection<Integer> x) {
        if (x.size() == 0) {
            return io.github.repir.tools.lib.Const.NULLINT;
        }
        Integer max = Integer.MIN_VALUE;
        for (Integer i : x) {
            max = java.lang.Math.max(max, i);
        }
        return max;
    }

    /**
     *
     * @param x
     * @return
     */
    public static int sum(int x[]) {
        int sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return sum;
    }

    public static int sum(int x[], int start, int end) {
        int sum = 0;
        for (int i = start; i < end; i++) {
            sum += x[i];
        }
        return sum;
    }

    /**
     *
     * @param x
     * @return
     */
    public static double mean(int x[]) {
        int total = 0;
        for (int i = x.length - 1; i >= 0; i--) {
            total += x[i];
        }
        return (x.length == 0) ? Double.NaN : (total / (double)x.length);
    }

    public static double mean(Collection<Integer> x) {
        if (x.size() == 0) {
            return Double.NaN;
        }
        int total = 0;
        for (Integer i : x) {
            total += i;
        }
        return (total / (double)x.size());
    }

    public static double mean(int x[], int xstart, int xend) {
        if (xend - xstart < 1) {
            return Double.NaN;
        }
        int total = 0;
        for (int i = xstart; i < xend; i++) {
            total += x[i];
        }
        return (total / (double)(xend - xstart));
    }

    public static int[] subtract(int x[], int subtraction) {
        int result[] = new int[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i] - subtraction;
        }
        return result;
    }

    /**
     *
     * @param x
     * @return
     */
    public static int[] toPrimitive(Integer[] x) {
        int ret[] = new int[x.length];
        for (int i = 0; i < x.length; i++) {
            ret[i] = x[i];
        }
        return ret;
    }

    /**
     *
     * @param x
     * @return
     */
    public static double variance(int x[], double mean) {
        double dev = 0;
        if (x.length > 1) {
            for (int i = x.length - 1; i >= 0; i--) {
                dev += (x[i] - mean) * (x[i] - mean);
            }
            dev /= (x.length - 1);
        }
        return dev;
    }

    public static double standardDeviation(int x[]) {
        double avg = mean(x);
        double variance = variance(x, avg);
        return java.lang.Math.sqrt(variance); 
    }
    
    public static double variance(Collection<Integer> x, double avg) {
        if (x.size() == 0) {
            return Double.NaN;
        }
        double dev = 0;
        for (Integer i : x) {
            double diff = i - avg;
            dev += diff * diff;
        }
        dev /= (x.size() - 1);
        return dev;
    }
        
    public static double standardDeviation(Collection<Integer> x) {
        double avg = mean(x);
        double variance = variance(x, avg);
        return java.lang.Math.sqrt(variance);
    }

    public static Double standardDeviation(int x[], int xstart, int xend, double mean) {
        double dev = 0;
        int size = xend - xstart;
        if (size > 1) {
            for (int i = xstart; i < xend; i++) {
                double d = x[i] - mean;
                dev += d * d;
            }
            dev /= (size);
        }
        return java.lang.Math.sqrt(dev);
    }

    public static double[] subtractMean(int x[]) {
        double mean = mean(x);
        double y[] = new double[x.length];
        for (int i = x.length - 1; i >= 0; i--) {
            y[i] = (x[i] - mean);
        }
        return y;
    }
    
    public static double welchTTestOneSided(Collection<Integer> a, Collection<Integer> b) {
        double meana = mean(a);
        double meanb = mean(b);
        double vara = variance(a, meana);
        double varb = variance(b, meanb);
        double rvara = vara / a.size();
        double rvarb = varb / b.size();
        
        double t = (meana - meanb)/Math.sqrt(vara / a.size() + varb / b.size());
        double df = welchDegreesOfFreedom(vara, varb, a.size(), b.size());
        TDistribution tdistribution = new TDistribution(df);
        double p = 1 - tdistribution.cumulativeProbability(t);
        //log.info("welchTTest Xa %f Xb %f Sa %f Sb %f df %f t %f p %f", meana, meanb, vara, varb, df, t, p);
        return p;
    }
}
