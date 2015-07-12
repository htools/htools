package io.github.repir.tools.lib;

import static io.github.repir.tools.lib.PrintTools.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 *
 * @author jeroen
 */
public enum MathTools {
    ;
    static double Z_MAX = 6;                    // Maximum ±z value
    static int ROUND_FLOAT = 6;              // Decimal places to round numbers

    /**
     *
     * @param z
     * @return
     */
    public static double stdNormalP(double z) { // compute P given Z
        double y, x, w;

        if (z == 0.0) {
            x = 0.0;
        } else {
            y = 0.5 * java.lang.Math.abs(z);
            if (y > (Z_MAX * 0.5)) {
                x = 1.0;
            } else if (y < 1.0) {
                w = y * y;
                x = ((((((((0.000124818987 * w
                        - 0.001075204047) * w + 0.005198775019) * w
                        - 0.019198292004) * w + 0.059054035642) * w
                        - 0.151968751364) * w + 0.319152932694) * w
                        - 0.531923007300) * w + 0.797884560593) * y * 2.0;
            } else {
                y -= 2.0;
                x = (((((((((((((-0.000045255659 * y
                        + 0.000152529290) * y - 0.000019538132) * y
                        - 0.000676904986) * y + 0.001390604284) * y
                        - 0.000794620820) * y - 0.002034254874) * y
                        + 0.006549791214) * y - 0.010557625006) * y
                        + 0.011630447319) * y - 0.009279453341) * y
                        + 0.005353579108) * y - 0.002141268741) * y
                        + 0.000535310849) * y + 0.999936657524;
            }
        }
        return z > 0.0 ? ((x + 1.0) * 0.5) : ((1.0 - x) * 0.5);
    }

    /**
     *
     * @param p
     * @return
     */
    public static double stdNormalZ(double p) { // compute Z given P
        double Z_EPSILON = 0.000001;     /* Accuracy of z approximation */

        double minz = -Z_MAX;
        double maxz = Z_MAX;
        double zval = 0.0;
        double pval;

        if (p < 0.0 || p > 1.0) {
            return -1;
        }

        while ((maxz - minz) > Z_EPSILON) {
            pval = stdNormalP(zval);
            if (pval > p) {
                maxz = zval;
            } else {
                minz = zval;
            }
            zval = (maxz + minz) * 0.5;
        }
        return (zval);
    }

    /**
     *
     * @param d
     * @param precision
     * @return
     */
    public static String roundPrecisionStr(double d, int precision) {
        char decimal = new DecimalFormatSymbols().getDecimalSeparator();
        d = roundPrecision(d, precision);
        String s = sprintf("%f", d);
        for (int i = 0; i < s.length()
                && (s.charAt(i) == '0' || s.charAt(i) == decimal); i++) {
            if (s.charAt(i) == '0') {
                precision++;
            }
        }
        if (precision > s.length()) {
            precision = 1;
        }
        if (precision < s.length() - 1 && s.indexOf(decimal) < precision) {
            return s.substring(0, precision + 1);
        } else {
            return s.substring(0, precision);
        }
    }

    public static String sigLatex(double d, int precision) {
        boolean inp = false;
        double ep = Math.log10(d);
        if (ep > -precision) {
            String s = new DecimalFormat("0." + StrTools.concat("0", precision)).format(d);
            return s;
        }
        int p = (int) Math.ceil(-ep);
        String s = new DecimalFormat("0." + StrTools.concat("0", p)).format(d);
        log.info("s %s", s);
        char c = s.charAt(s.length() - 1);
        char c1 = s.charAt(s.length() - 2);
        if (c1 > '0' && c1 <= '9') {
            return sprintf("%s \\cdot 10^{-%d}", c1, p - 1);
        } else {
            return sprintf("%s \\cdot 10^{-%d}", c, p);
        }
    }

    public final static int sign(int i) {
        return (i > 0)?1:(i < 0)?-1:0;
    }
    
    public final static int sign(double i) {
        return (i > 0)?1:(i < 0)?-1:0;
    }
    
    public final static double EPSILON = 0.0000001;

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    /**
     *
     * @param d
     * @param precision
     * @return
     */
    public static double roundPrecision(double d, int precision) {
        char decimal = new DecimalFormatSymbols().getDecimalSeparator();
        if (d == 0) {
            return 0;
        }
        String format = "";
        String s = String.format("%f", d);
        int leadingzeros = 0, trailingzeros = 0;
        int leadingprecision = 0;
        int pos = 0;
        boolean seendecimal = false;
        for (; pos < s.length() && s.charAt(pos) == '0'; pos++) {
            format += "0";
            leadingzeros++;
        }
        if (pos < s.length() && leadingzeros != 0 && s.charAt(pos) == decimal) {
            format += ".";
            seendecimal = true;
            pos++;
        }
        for (; pos < s.length() && leadingzeros != 0 && seendecimal
                && s.charAt(pos) == '0'; pos++) {
            format += "0";
            trailingzeros++;
        }
        for (; pos < s.length() && precision > 0; pos++) {
            if (s.charAt(pos) != decimal) {
                format += s.charAt(pos);
                precision--;
            } else {
                seendecimal = true;
                format += ".";
            }
        }
        String add = format.replaceAll("[1-9]", "0");
        if (pos < s.length() && s.charAt(pos) >= '5' && s.charAt(pos) <= '9') {
            add = add.substring(0, add.length() - 1) + "1";
        }
        for (; pos < s.length() && !seendecimal && s.charAt(pos) != decimal; pos++) {
            format += "0";
            add += "0";
        }
        printf("%s %s %f\n", format, add, d);
        return Double.parseDouble(format) + Double.parseDouble(add);
    }



    /**
     *
     * @param matrix
     * @return
     */
    public static Double cramersV(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double rowtotal[] = new double[rows];
        double coltotal[] = new double[cols];
        double total = 0;
        for (int row = 0; row < rows; row++) {
            rowtotal[row] = 0;
            for (int col = 0; col < cols; col++) {
                rowtotal[row] += matrix[row][col];
                total += matrix[row][col];
            }
        }
        for (int col = 0; col < cols; col++) {
            coltotal[col] = 0;
            for (int row = 0; row < rows; row++) {
                coltotal[col] += matrix[row][col];
            }
        }
        for (int row = 0; row < rows; row++) {
            rowtotal[row] /= total;
        }
        for (int col = 0; col < cols; col++) {
            coltotal[col] /= total;
        }
        double V = -1;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (matrix[row][col] > 0) {
                    V += java.lang.Math.pow(matrix[row][col] / total, 2) / (rowtotal[row] * coltotal[col]);
                }
            }
        }
        V = V / (java.lang.Math.min(rows, cols) - 1);
        return V;
    }

    public static double chiSquared(int a, int b, int c, int d) {
        return Math.pow(a * d - b * c, 2) * (a + b + c + d) / (double) ((a + b) * (c + d) * (a + c) * (b + d));
    }

    public static int mod(int x, int y) {
        int result = x % y;
        if (result < 0) {
            result += y;
        }
        return result;
    }

    public static int mod(long x, int y) {
        int result = (int)(x % y);
        if (result < 0) {
            result += y;
        }
        return result;
    }

    public static int min(int x, int y, int z) {
        return (x < y)?Math.min(x, z):Math.min(y,z);
    }

    public static int max(int x, int y, int z) {
        return (x > y)?Math.max(x, z):Math.max(y,z);
    }

    static final double REC_LOG_2_OF_E = 1.0D / java.lang.Math.log(2.0D);

    public static double log2(double d) {
        return (java.lang.Math.log(d) * REC_LOG_2_OF_E);
    }

    public static int fac(int i) {
        int r = 1;
        for (int j = 2; j <= i; j++) {
            r *= j;
        }
        return r;
    }

   // Calculate Binomial Coefficient 
    // Jeroen B.P. Vuurens
    public static long binomialCoefficient(int n, int k) {
        // take the lowest possible k to reduce computing using: n over k = n over (n-k)
        if (n == k) {
            return 1;
        }
        k = java.lang.Math.min(k, n - k);

        // holds the high number: fi. (1000 over 990) holds 991..1000
        long highnumber[] = new long[k];
        for (int i = 0; i < k; i++) {
            highnumber[i] = n - i; // the order is important, high to low
        }        // holds the dividers: fi. (1000 over 990) holds 2..10
        int dividers[] = new int[k - 1];
        for (int i = 0; i < k - 1; i++) {
            dividers[i] = k - i;
        }

      // for every divider there is always exists a highnumber that can be divided by this, 
        // the number of highnumbers being a sequence that is equal to the number of dividers.
        // The only trick is to divide in reverse order, so divide the highest divider first
        // trying it on the highest highnumber first. That way you do not need to do any tricks with
        // primes.
        for (int divider : dividers) {
            for (int i = 0; i < k; i++) {
                if (highnumber[i] % divider == 0) {
                    highnumber[i] /= divider;
                    break;
                }
            }
        }

        // multiply remainder of highnumbers
        long result = 1;
        for (long high : highnumber) {
            result *= high;
        }
        return result;
    }

    /**
     * Lanczos gamma approximation, from numerical recipes
     *
     * @param x
     * @return
     */
    static double logGamma(double x) {
        double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
        double ser = 1.0 + 76.18009173 / (x + 0) - 86.50532033 / (x + 1)
                + 24.01409822 / (x + 2) - 1.231739516 / (x + 3)
                + 0.00120858003 / (x + 4) - 0.00000536382 / (x + 5);
        return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
    }

    static double gamma(double x) {
        return Math.exp(logGamma(x));
    }

    public static int numberCoverBits(int v) {
        int l = Integer.lowestOneBit(v);
        int h = Integer.highestOneBit(v);
        return Integer.numberOfTrailingZeros(h / l) + 1;
    }

    /**
     * @return the number of bits set to 1 in the given number
     */
    public static int numberOfSetBits(int number) {
        number = number - ((number >> 1) & 0x55555555);
        number = (number & 0x33333333) + ((number >> 2) & 0x33333333);
        return (((number + (number >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
    }

    /**
     * @return the number of bits set to 1 in the given number
     */
    public static int numberOfSetBits(long number) {
        number = number - ((number >> 1) & 0x5555555555555555l);
        number = (number & 0x3333333333333333l) + ((number >> 2) & 0x3333333333333333l);
        return (int) ((((number + (number >> 4)) & 0xF0F0F0F0F0F0F0Fl) * 0x101010101010101l) >> 56);
    }

    public static int numberLeadingZeroBits(int x) {
        if (x == 0) {
            return (32);
        }
        int n = 0;
        if (x <= 0x0000FFFF) {
            n = n + 16;
            x = x << 16;
        }
        if (x <= 0x00FFFFFF) {
            n = n + 8;
            x = x << 8;
        }
        if (x <= 0x0FFFFFFF) {
            n = n + 4;
            x = x << 4;
        }
        if (x <= 0x3FFFFFFF) {
            n = n + 2;
            x = x << 2;
        }
        if (x <= 0x7FFFFFFF) {
            n = n + 1;
        }
        return n;
    }

    public static int nextHighestPower2(int n) {
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return ++n;
    }
    
    public static int combineHash(int seed, int... v) {
        if (v != null) {
            for (int i = 0; i < v.length; i++) {
                seed += v[i];
                seed += (seed << 10);
                seed ^= (seed >> 6);
            }
        }
        return seed;
    }

    public static int combineHash(int seed, long... v) {
        if (v != null) {
            for (int i = 0; i < v.length; i++) {
                seed = combineHash(seed, (int)(v[i] >>> 32), (int)v[i]);
            }
        }
        return seed;
    }

    public static int finishHash(int seed) {
        seed += (seed << 3);
        seed ^= (seed >> 11);
        seed += (seed << 15);

        return seed;
    }

    /**
     * Bob Jenkins One-at-a-Time hash function
     * <p/>
     * @return hashcode for the given input;
     */
    public static int hashCode(int... v) {
        return finishHash(combineHash(31, v));
    }

    public static int hashCode(long... v) {
        return finishHash(combineHash(31, v));
    }

    public static int pairCantor(int i, int j) {
        return (i + j) * (i + j + 1)/2 + Math.max(i, j);
    }
    
    public static final double SQRT2 = Math.sqrt(2);

    public static double cumulativeProbability(double x, double mean, double standardDeviation) {
        return 0.5 * (1 + org.apache.commons.math3.special.Erf.erf(x - mean / (standardDeviation * SQRT2)));
    }
    
    public static double welchDegreesOfFreedom(double vara, double varb, int sizea, int sizeb) {
        double A = vara / sizea;
        double B = varb / sizeb;
        double AB2 = (A + B) * (A + B);
        double A2 = A * A / (sizea - 1);
        double B2 = B * B / (sizeb - 1);
        double df = AB2 / (A2 + B2);
        return df;
    }
    
    public static double chiTest(double chi2, int df) {
        org.apache.commons.math3.distribution.ChiSquaredDistribution dist = new org.apache.commons.math3.distribution.ChiSquaredDistribution(df);
        return 1 - dist.cumulativeProbability(chi2);
    }
}
