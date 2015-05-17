package io.github.repir.tools.lib;

import static io.github.repir.tools.lib.MathTools.welchDegreesOfFreedom;
import java.util.Collection;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.TDistribution;

/**
 *
 * @author jeroen
 */
public enum DoubleTools {
    ;

    public static Log log = new Log(DoubleTools.class);
    /**
     *
     * @param x
     * @return
     */
    public static double min(double ... x) {
        double min = (x.length > 0) ? x[0] : null;
        for (int i = 1; i < x.length; i++) {
            min = java.lang.Math.min(min, x[i]);
        }
        return min;
    }

    public static double min3(double x, double y, double z) {
        return java.lang.Math.min(x, java.lang.Math.min(y, z));
    }

    public static double min(Collection<Double> x) {
        if (x.size() == 0) {
            return io.github.repir.tools.lib.Const.NULLINT;
        }
        double min = Integer.MAX_VALUE;
        for (Double i : x) {
            min = java.lang.Math.min(min, i);
        }
        return min;
    }

    public static double max(Collection<Double> x) {
        if (x.size() == 0) {
            return io.github.repir.tools.lib.Const.NULLINT;
        }
        double max = Double.MIN_VALUE;
        for (double i : x) {
            max = java.lang.Math.max(max, i);
        }
        return max;
    }

    /**
     *
     * @param x
     * @return
     */
    public static double sum(double x[]) {
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return sum;
    }

    public static double sum(double x[], int start, int end) {
        double sum = 0;
        for (int i = start; i < end; i++) {
            sum += x[i];
        }
        return sum;
    }

    /**
     * @param x
     * @return normalized array of double that sums to 1
     */
    public static double[] normalize(double x[]) {
        double sum = sum(x);
        if (sum != 1) {
            for (int i = 0; i < x.length; i++) {
                x[i] /= sum;
            }
        }
        return x;
    }

    /**
     *
     * @param x
     * @return
     */
    public static double mean(double x[]) {
        double total = 0;
        for (int i = x.length - 1; i >= 0; i--) {
            total += x[i];
        }
        return (x.length == 0) ? Double.NaN : (total / x.length);
    }

    public static double mean(Collection<Double> x) {
        if (x.size() == 0) {
            return Double.NaN;
        }
        double total = 0;
        for (Double i : x) {
            total += i;
        }
        return (total / x.size());
    }

    public static double mean(double x[], int xstart, int xend) {
        if (xend - xstart < 1) {
            return Double.NaN;
        }
        double total = 0;
        for (int i = xstart; i < xend; i++) {
            total += x[i];
        }
        return (total / (xend - xstart));
    }

    public static double[] subtract(double x[], double subtraction) {
        double result[] = new double[x.length];
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
    public static double[] toPrimitive(Double[] x) {
        double ret[] = new double[x.length];
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
    public static double variance(double x[], double mean) {
        double dev = 0;
        if (x.length > 1) {
            for (int i = x.length - 1; i >= 0; i--) {
                dev += (x[i] - mean) * (x[i] - mean);
            }
            dev /= (x.length - 1);
        }
        return dev;
    }

    public static double standardDeviation(double x[]) {
        double avg = mean(x);
        double variance = variance(x, avg);
        return java.lang.Math.sqrt(variance); 
    }
    
    public static double variance(Collection<Double> x, double mean) {
        if (x.size() == 0) {
            return Double.NaN;
        }
        double dev = 0;
        for (Double i : x) {
            double diff = i - mean;
            dev += diff * diff;
        }
        dev /= (x.size() - 1);
        return dev;
    }
        
    public static double variance(Collection<Double> x) {
        if (x.size() == 0) {
            return Double.NaN;
        }
        double mean = mean(x);
        double dev = 0;
        for (Double i : x) {
            double diff = i - mean;
            dev += diff * diff;
        }
        dev /= (x.size() - 1);
        return dev;
    }
        
    public static double standardDeviation(Collection<Double> x) {
        double avg = DoubleTools.mean(x);
        double variance = variance(x, avg);
        return java.lang.Math.sqrt(variance);
    }

    public static Double standardDeviation(double x[], int xstart, int xend, double mean) {
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

    public static double[] convertToZ(double x[]) {
        double mean = mean(x);
        double variance = variance(x, mean);
        double sd = java.lang.Math.sqrt(variance);
        double y[] = new double[x.length];
        for (int i = x.length - 1; i >= 0; i--) {
            y[i] = (x[i] - mean) / sd;
        }
        return y;
    }

    public static double[] subtractMean(double x[]) {
        double mean = mean(x);
        double y[] = new double[x.length];
        for (int i = x.length - 1; i >= 0; i--) {
            y[i] = (x[i] - mean);
        }
        return y;
    }
    
    public static double welchTTestOneSided(Collection<Double> a, Collection<Double> b) {
        double meana = DoubleTools.mean(a);
        double meanb = DoubleTools.mean(b);
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
