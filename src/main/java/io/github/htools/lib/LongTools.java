package io.github.htools.lib;

import static io.github.htools.lib.MathTools.welchDegreesOfFreedom;
import java.util.Collection;
import org.apache.commons.math3.distribution.TDistribution;

/**
 *
 * @author jeroen
 */
public enum LongTools {
    ;

    public static Log log = new Log(LongTools.class);
    /**
     *
     * @param x
     * @return
     */
    public static long min(long ... x) {
        long min = (x.length > 0) ? x[0] : null;
        for (int i = 1; i < x.length; i++) {
            min = java.lang.Math.min(min, x[i]);
        }
        return min;
    }

    public static long min3(long x, long y, long z) {
        return java.lang.Math.min(x, java.lang.Math.min(y, z));
    }

    public static long min(Collection<Long> x) {
        if (x.size() == 0) {
            return io.github.htools.lib.Const.NULLINT;
        }
        Long min = Long.MAX_VALUE;
        for (Long i : x) {
            min = java.lang.Math.min(min, i);
        }
        return min;
    }

    public static long max(Collection<Long> x) {
        if (x.size() == 0) {
            return io.github.htools.lib.Const.NULLINT;
        }
        Long max = Long.MIN_VALUE;
        for (Long i : x) {
            max = java.lang.Math.max(max, i);
        }
        return max;
    }

    /**
     *
     * @param x
     * @return
     */
    public static long sum(long x[]) {
        long sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return sum;
    }

    public static long sum(long x[], int start, int end) {
        long sum = 0;
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
    public static double mean(long x[]) {
        long total = 0;
        for (int i = x.length - 1; i >= 0; i--) {
            total += x[i];
        }
        return (x.length == 0) ? Double.NaN : (total / (double)x.length);
    }

    public static double mean(Collection<Long> x) {
        if (x.size() == 0) {
            return Double.NaN;
        }
        long total = 0;
        for (Long i : x) {
            total += i;
        }
        //log.info("mean %d %f %d %f", total, (double)total, total / x.size(), total / (double)x.size());
        return (((double)total) / (double)x.size());
    }

    public static double mean(long x[], int xstart, int xend) {
        if (xend - xstart < 1) {
            return Double.NaN;
        }
        long total = 0;
        for (int i = xstart; i < xend; i++) {
            total += x[i];
        }
        return (total / (double)(xend - xstart));
    }

    public static long[] subtract(long x[], long subtraction) {
        long result[] = new long[x.length];
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
    public static long[] toPrimitive(Long[] x) {
        long ret[] = new long[x.length];
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
    public static double variance(long x[], double mean) {
        double dev = 0;
        if (x.length > 1) {
            for (int i = x.length - 1; i >= 0; i--) {
                dev += (x[i] - mean) * (x[i] - mean);
            }
            dev /= (x.length - 1);
        }
        return dev;
    }

    public static double standardDeviation(long x[]) {
        double mean = mean(x);
        double variance = variance(x, mean);
        return java.lang.Math.sqrt(variance); 
    }
    
    public static double variance(Collection<Long> x, double mean) {
        if (x.size() == 0) {
            return Double.NaN;
        }
        double dev = 0;
        for (Long i : x) {
            double diff = i - mean;
            //log.info("variance diff %d %f %f %f", i, mean, diff, diff * diff);
            dev += diff * diff;
        }
        dev /= (x.size() - 1);
        return dev;
    }
        
    public static double variance(Collection<Long> x) {
        if (x.size() == 0) {
            return Double.NaN;
        }
        double mean = mean(x);
        double dev = 0;
        for (Long i : x) {
            double diff = i - mean;
            //log.info("variance diff %d %f %f %f", i, mean, diff, diff * diff);
            dev += diff * diff;
        }
        dev /= (x.size() - 1);
        return dev;
    }
        
    public static double standardDeviation(Collection<Long> x) {
        double avg = mean(x);
        double variance = variance(x, avg);
        return java.lang.Math.sqrt(variance);
    }

    public static Double standardDeviation(long x[], int xstart, int xend, double mean) {
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

    public static double[] subtractMean(long x[]) {
        double mean = mean(x);
        double y[] = new double[x.length];
        for (int i = x.length - 1; i >= 0; i--) {
            y[i] = (x[i] - mean);
        }
        return y;
    }
    
    public static double welchTTestOneSided(Collection<Long> a, Collection<Long> b) {
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
