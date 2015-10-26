package io.github.htools.lib;

import java.util.Random;
import java.util.UUID;

/**
 *
 * @author jeroen
 */
public enum RandomTools {
    ;

   // global randomizer
   private static RandomGenerator randomizer;
    public static Log log = new Log(RandomTools.class);
   
    public static RandomGenerator createGenerator(int seed) {
        return new RandomGenerator(seed);
    }

    private static RandomGenerator getRandomizer() {
        if (randomizer == null)
            randomizer = new RandomGenerator();
        return randomizer;
    }
    
    /**
     *
     * @return
     */
    public static double getStdNormal() {
        return getRandomizer().getStdNormal();
    }

    /**
     *
     * @return
     */
    public static int getSign() {
        return getRandomizer().getSign();
    }

    /**
     *
     * @return
     */
    public static boolean getBoolean() {
        return getRandomizer().getBoolean();
    }

    /**
     *
     * @param bias
     * @return
     */
    public static int getBiasRandomSign(double bias) {
        return getRandomizer().getBiasRandomSign(bias);
    }

    /**
     *
     * @return pseudo-random double between 0.0 and 1.0 (inclusive)
     */
    public static double getDouble() { // double between 0.0 and 1.0
        return getRandomizer().getDouble();
    }

    /**
     *
     * @return
     */
    public static int getInt() {
        return getRandomizer().getInt();
    }

    public static int[] getRandomList(int upperlimit) {
        return getRandomizer().getRandomList(upperlimit);
    }

    /**
     *
     * @param UpperLimit
     * @return
     */
    public static int getInt(int UpperLimit) {
        return getRandomizer().getInt(UpperLimit);
    }

    /**
     *
     * @param bits
     * @return
     */
    public static int getBits(int bits) {
        return getRandomizer().getBits(bits);
    }

    /**
     *
     * @param classes
     * @return
     */
    public static int chooseClass(double[] classes) {
        return getRandomizer().chooseClass(classes);
    }

    /**
     *
     * @param p
     * @return
     */
    public static int StdNormalOneTailed(double p) {
        return getRandomizer().StdNormalOneTailed(p);
    }

    /**
     *
     * @param p
     * @return
     */
    public static int StdNormalOneTailedComp(double p) {
        return getRandomizer().StdNormalOneTailedComp(p);
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Implementation of a Mersenne Twister to generate pseudorandom numbers.
     */
    public static class RandomGenerator {

        private boolean haveNextNextGaussian = false;
        private double nextNextGaussian;
        private int mt_index;
        private int[] mt_buffer = new int[624];

        protected RandomGenerator(Random random) {
            for (int i = 0; i < 624; i++) {
                mt_buffer[i] = random.nextInt();
            }
            Log.out("Random seeds %s", ArrayTools.toString(mt_buffer));
            Log.printStackTrace();
            mt_index = 0;
        }
        
        protected RandomGenerator(int seed) {
            this(new Random(seed));
        }

        /**
         * Constructor only used for global calls
         */
        protected RandomGenerator() {
            this(new Random());
        }

        public int getInt() {
            if (mt_index == 624) {
                mt_index = 0;
                int i = 0;
                int s;
                for (; i < 624 - 397; i++) {
                    s = (mt_buffer[i] & 0x80000000) | (mt_buffer[i + 1] & 0x7FFFFFFF);
                    mt_buffer[i] = mt_buffer[i + 397] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
                }
                for (; i < 623; i++) {
                    s = (mt_buffer[i] & 0x80000000) | (mt_buffer[i + 1] & 0x7FFFFFFF);
                    mt_buffer[i] = mt_buffer[i - (624 - 397)] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
                }

                s = (mt_buffer[623] & 0x80000000) | (mt_buffer[0] & 0x7FFFFFFF);
                mt_buffer[623] = mt_buffer[396] ^ (s >> 1) ^ ((s & 1) * 0x9908B0DF);
            }
            return mt_buffer[mt_index++];
        }

        /**
         * @param upperLimit
         * @return pseudo-random integer between 0 (inlcusive) and upperLimit
         * (exclusive)
         */
        public int getInt(int upperLimit) {
            return java.lang.Math.abs(getInt() % (upperLimit));
        }

        /**
         * @param classes weight distribution over classes
         * @return the index of the class randomly chosen, taking the weight
         * distribution into account
         */
        public int chooseClass(double[] classes) {
            double[] norm = DoubleTools.normalize(classes);
            double random = getDouble();
            int classIndex;
            for (classIndex = 0; classIndex < norm.length - 1 && random >= norm[classIndex]; classIndex++) {
                random -= norm[classIndex];
            }
            return classIndex;
        }

        /**
         *
         * @param bits
         * @return pseudo-random integer of at most #bits
         */
        public int getBits(int bits) {
            return (int) (getInt() >>> (32 - bits));
        }

        /**
         * @param upperlimit
         * @return a random shuffled list that contains the numbers
         * 0..upperlimit (exclusive)
         */
        public int[] getRandomList(int upperlimit) {
            int result[] = new int[upperlimit];
            for (int i = 0; i < upperlimit; i++) {
                result[i] = i;
            }
            for (int i = 0; i < upperlimit; i++) {
                int pos = getInt(upperlimit);
                int a = result[i];
                result[i] = result[pos];
                result[pos] = a;
            }
            return result;
        }

        /**
         *
         * @param p
         * @return
         */
        public int StdNormalOneTailed(double p) {
            double z = MathTools.stdNormalZ(0.5 + p / 2);
            double r = getStdNormal();
            r = (r < 0) ? -r : r;
            int dev = (int) java.lang.Math.floor(r / z);
            return dev;
        }
        static double StdNormalOneTailedCompCorrection = 0.79 / (0.282 / (0.282 + 0.115));

        /**
         *
         * @param p
         * @return
         */
        public int StdNormalOneTailedComp(double p) {
            double z = MathTools.stdNormalZ(0.5 + p / 2);
            double r = getStdNormal();
            r = (r < 0) ? -r : r;
            double v = r / z;
            int dev = (int) java.lang.Math.floor(v);
            if (dev > 0) {
                dev = (int) java.lang.Math.floor(v * StdNormalOneTailedCompCorrection);
            }
            return dev;
        }

        /**
         * @return randomly a -1 or +1
         */
        public int getSign() {
            return (getBoolean() ? 1 : -1);
        }

        /**
         * @return randomly a true or false
         */
        public boolean getBoolean() {
            return getInt() > 0;
        }

        public int getBiasRandomSign(double bias) {
            int sign = (int) java.lang.Math.signum(bias);
            bias *= sign;
            if (getDouble() <= bias) {
                return sign;
            } else {
                return getSign();
            }
        }

        /**
         * @return pseudo-random double between 0.0 and 1.0 (inclusive)
         */
        public double getDouble() { // double between 0.0 and 1.0
            return (((long) (getBits(26)) << 27) + getBits(27))
                    / (double) (1L << 53);
        }

        /**
         * @return random gaussian, using Box-Muller
         */
        public double getStdNormal() {
            if (haveNextNextGaussian) {
                haveNextNextGaussian = false;
                return nextNextGaussian;
            } else {
                double v1, v2, s;
                do {
                    v1 = 2 * getDouble() - 1; // between -1 and 1
                    v2 = 2 * getDouble() - 1; // between -1 and 1
                    s = v1 * v1 + v2 * v2;
                } while (s >= 1 || s == 0);
                double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
                nextNextGaussian = v2 * multiplier;
                haveNextNextGaussian = true;
                return v1 * multiplier;
            }
        }
    }
}
