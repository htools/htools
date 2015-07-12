package io.github.repir.tools.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author jeroen
 */
public enum ArrayTools {;

   public static Log log = new Log(ArrayTools.class);
   public static final int[] emptyIntArray = new int[0];
   public static final long[] emptyLongArray = new long[0];
   public static final double[] emptyDoubleArray = new double[0];
   public static final String[] emptyStringArray = new String[0];
   
   public static <K> K[] group(K... obj) {
      return obj;
   }

   public static <K> K[] addArr(K[] obj, K... o) {
      K n[] = (K[])resize( obj, obj.length + o.length);
      System.arraycopy(o, 0, n, obj.length, o.length);
      return n;
   }

   public static int[] addArr(int[] obj, int... o) {
      int n[] = new int[obj.length + o.length];
      System.arraycopy(obj, 0, n, 0, obj.length);
      System.arraycopy(o, 0, n, obj.length, o.length);
      return n;
   }

   public static long[] addArr(long[] obj, long... o) {
      long n[] = new long[obj.length + o.length];
      System.arraycopy(obj, 0, n, 0, obj.length);
      System.arraycopy(o, 0, n, obj.length, o.length);
      return n;
   }

   public static double[] addArr(double[] obj, double... o) {
      double n[] = new double[obj.length + o.length];
      System.arraycopy(obj, 0, n, 0, obj.length);
      System.arraycopy(o, 0, n, obj.length, o.length);
      return n;
   }

   /**
    * @param <K>
    * @param obj
    * @param start
    * @param end
    * @return An array with the elements start to end (exclusive) removed 
    */
   public static <K> K[] delete(K[] obj, int start, int end) {
      K n[] = createArray( obj, obj.length - (end - start));
      System.arraycopy(obj, 0, n, 0, start);
      System.arraycopy(obj, end, n, start, obj.length - end);
      return n;
   }

   public static int[] delete(int[] obj, int start, int end) {
      int n[] = new int[obj.length - (end - start)];
      System.arraycopy(obj, 0, n, 0, start);
      System.arraycopy(obj, end, n, start, obj.length - end);
      return n;
   }

   public static long[] delete(long[] obj, int start, int end) {
      long n[] = new long[obj.length - (end - start)];
      System.arraycopy(obj, 0, n, 0, start);
      System.arraycopy(obj, end, n, start, obj.length - end);
      return n;
   }

   public static double[] delete(double[] obj, int start, int end) {
      double n[] = new double[obj.length - (end - start)];
      System.arraycopy(obj, 0, n, 0, start);
      System.arraycopy(obj, end, n, start, obj.length - end);
      return n;
   }

   public static <K> K[] resize(K[] obj, int newsize) {
       if (newsize == obj.length)
           return obj;
      K n[] = createArray( obj, newsize);
      if (newsize > obj.length)
         System.arraycopy(obj, 0, n, 0, obj.length);
      else 
         System.arraycopy(obj, 0, n, 0, newsize);
      return n;
   }

   public static <K> K[] addObjectToArr(K[] obj, K o) {
      K n[] = resize( obj, obj.length + 1);
      n[obj.length] = o;
      return n;
   }

   public static void copy(Object[] obj, Object[] dest) {
      System.arraycopy(obj, 0, dest, 0, Math.min(obj.length, dest.length));
   }

   public static <K> K[] union(K[] ... morearrays) {
      int length = 0;
      for (Object[] oo : morearrays) {
         length += oo.length;
      }
      K dest[] = createArray( morearrays[0], length );
      int pos = 0;
      for (Object[] oo : morearrays) {
         System.arraycopy(oo, 0, dest, pos, oo.length);
         pos += oo.length;
      }
      return dest;
   }
   
   public static <K> K[] union(K[] a, K [] b) {
      K c[] = resize( a, a.length + b.length );
      System.arraycopy(b, 0, c, a.length, b.length);
      return c;
   }
   
   public static <K> K[] createArray( K other[], int size) {
       return createArray( (Class<? extends K>)other.getClass().getComponentType(), size );
       //K[] a = Arrays.copyOf(other, 0);
       //return (K[])Arrays.copyOf(a, size, other.getClass());
   }

   public static <K> K[] createArray( Collection<K> coll, int size) {
       return createArray( (Class<? extends K>)coll.getClass().getComponentType(), size );
       //K[] a = Arrays.copyOf(other, 0);
       //return (K[])Arrays.copyOf(a, size, other.getClass());
   }

   private static Object[] nullarray = new Object[0];
   public static <K> K[] createArray( Class<? extends K> datatype, int size) {
      //return (K[])Arrays.copyOf(nullarray, size, datatype);
      return (K[]) java.lang.reflect.Array.newInstance(datatype, size);
   }

   public static <K> int indexOf(K array[], K needle) {
      for (int i = 0; i < array.length; i++) {
         if (array[i].equals(needle)) {
            return i;
         }
      }
      return -1;
   }
   
   /**
    * @param c collection of objects
    * @param t object of same type as used in Collection c, that must be 
    * excluded from the array returned;
    * @return array consisting of all objects in the collection except t
    */
   public static <K> K[] arrayOfOthers( Collection<K> c, K t ) {
      ArrayList<K> list = new ArrayList();
      for (K o : c) {
         if (o != t) {
            list.add(o);
         }
      }
      return list.toArray((K[])createArray( t.getClass(), list.size()));
   }

   public static int[] clone(int[] a) {
      int r[] = new int[a.length];
      System.arraycopy(a, 0, r, 0, r.length);
      return r;
   }

   public static long[] clone(long[] a) {
      long r[] = new long[a.length];
      System.arraycopy(a, 0, r, 0, r.length);
      return r;
   }

   public static boolean[] clone(boolean[] a) {
      boolean r[] = new boolean[a.length];
      System.arraycopy(a, 0, r, 0, r.length);
      return r;
   }

   public static String[] clone(String[] a) {
      String r[] = new String[a.length];
      System.arraycopy(a, 0, r, 0, r.length);
      return r;
   }

   public static long[] flatten(long array[][]) {
      int size = 0;
      for (long l[] : array) {
         size += l.length;
      }
      long r[] = new long[size];
      int pos = 0;
      for (long a[] : array) {
         for (long l : a) {
            r[pos++] = l;
         }
      }
      return r;
   }

   public static int[] flatten(int array[][]) {
      int size = 0;
      for (int l[] : array) {
         size += l.length;
      }
      int r[] = new int[size];
      int pos = 0;
      for (int a[] : array) {
         for (int l : a) {
            r[pos++] = l;
         }
      }
      return r;
   }

   public static int[] flatten(int array[][][]) {
      int size = 0;
      for (int l[][] : array) {
         for (int m[] : l) {
            size += m.length;
         }
      }
      int r[] = new int[size];
      int pos = 0;
      for (int l[][] : array) {
         for (int m[] : l) {
            for (int a : m) {
               r[pos++] = a;
            }
         }
      }
      return r;
   }

   public static int[] toIntArray(Collection<Integer> integers) {
      int[] ret = new int[integers.size()];
      Iterator<Integer> iterator = integers.iterator();
      for (int i = 0; i < ret.length; i++) {
         ret[i] = iterator.next();
      }
      return ret;
   }

   public static long[] toLongArray(Collection<Long> integers) {
      long[] ret = new long[integers.size()];
      Iterator<Long> iterator = integers.iterator();
      for (int i = 0; i < ret.length; i++) {
         ret[i] = iterator.next().longValue();
      }
      return ret;
   }

   public static String[] toArray(Collection<String> strings) {
       return strings.toArray(new String[strings.size()]);
   }

   public static <K> K[] toArray(Collection<K> strings, K[] array) {
       return strings.toArray(array);
   }

   public static double[] toDoubleArray(Collection<Double> integers) {
      double[] ret = new double[integers.size()];
      Iterator<Double> iterator = integers.iterator();
      for (int i = 0; i < ret.length; i++) {
         ret[i] = iterator.next().doubleValue();
      }
      return ret;
   }
   
   public static ArrayList<Double> toList(double array[]) {
       ArrayList<Double> list = new ArrayList();
       for (double d : array)
           list.add(d);
       return list;
   }

   public static ArrayList<Long> toList(long array[]) {
       ArrayList<Long> list = new ArrayList();
       for (long d : array)
           list.add(d);
       return list;
   }

   public static ArrayList<Integer> toList(int array[]) {
       ArrayList<Integer> list = new ArrayList();
       for (int d : array)
           list.add(d);
       return list;
   }

   public static <K> ArrayList<K> toList(K array[]) {
       ArrayList<K> list = new ArrayList();
       for (K d : array)
           list.add(d);
       return list;
   }

   public static <K> HashSet<K> toSet(K array[]) {
       HashSet<K> list = new HashSet();
       for (K d : array)
           list.add(d);
       return list;
   }

   public static String[] intersection(String[] array1, String[] array2) {
      ArrayList<String> result = new ArrayList<String>();
      for (String str1 : array1) {
         for (String str2 : array2) {
            if (str1.equals(str2)) {
               result.add(str1);
            }
         }
      }
      return result.toArray(new String[result.size()]);
   }

   public static int[] unique(int array[]) {
      if (array == null)
         return null;
      HashSet<Integer> result = new HashSet<Integer>();
      for (int str1 : array) {
         result.add(str1);
      }
      return toIntArray(result);
   }

   public static String toString(Object[] b) {
      return ArrayTools.toString(b, 0, b.length, ", ");
   }

   public static String toString(byte[] b) {
      return ArrayTools.toString(b, 0, b.length, ", ");
   }

   public static String toString(int[] b) {
      return (b == null) ? "[ null ]" : ArrayTools.toString(b, 0, b.length, ", ");
   }

   public static String toString(int[][] b) {
      if (b == null)
         return null;
      if (b.length == 0)
         return "{}";
      StringBuilder sb = new StringBuilder();
      sb.append("{ { ").append(ArrayTools.toString(b[0])).append((" }"));
      for (int i = 1; i < b.length; i++)
         sb.append(",\n{ ").append(ArrayTools.toString(b[i])).append(" }");
      return sb.append(" }").toString();
   }

   public static String toString(int[][][] b) {
      if (b == null)
         return null;
      if (b.length == 0)
         return "{}";
      StringBuilder sb = new StringBuilder();
      sb.append("{ { ").append(ArrayTools.toString(b[0])).append((" }"));
      for (int i = 1; i < b.length; i++)
         sb.append(",\n{ ").append(ArrayTools.toString(b[i])).append(" }");
      return sb.append(" }").toString();
   }

   public static String toString(long[] b) {
      return (b == null) ? "[ null ]" : ArrayTools.toString(b, 0, b.length, ", ");
   }

   public static String toString(double[] b) {
      return ArrayTools.toString(b, 0, b.length, ", ");
   }

   public static String toString(boolean[] b) {
      return ArrayTools.toString(b, 0, b.length, ", ");
   }

   public static String toString(byte[] b, int pos, int length, String separator) {
      StringBuilder sb = new StringBuilder();
      if (b.length > 0) {
         sb.append(b[pos]);
      }
      length = Math.min(length, b.length - pos);
      for (int i = 1; i < length; i++) {
         sb.append(separator).append(b[pos + i] & 0xFF);
      }
      return sb.toString();
   }

   public static String toString(Object[] b, int pos, int length, String separator) {
      StringBuilder sb = new StringBuilder();
      if (b.length > 0) {
         sb.append(b[pos]);
      }
      length = Math.min(length, b.length - pos);
      for (int i = 1; i < length; i++) {
         sb.append(separator).append(b[pos + i]);
      }
      return sb.toString();
   }

   public static String toString(Collection list) {
      return toString(list, ", ");
   }

   public static String toString(Collection list, String separator) {
      return "[ " + toStringList(list, separator) + " ]";
   }

   public static String toStringList(Collection list, String separator) {
      if (list.size() < 1)
         return "";
      StringBuilder sb = new StringBuilder();
      for (Object i : list) {
         sb.append(separator).append(i.toString());
      }
      sb.delete(0, separator.length());
      return sb.toString();
   }

   public static String toString(long[] b, int pos, int length, String separator) {
      StringBuilder sb = new StringBuilder();
      if (b.length > 0) {
         sb.append(b[pos]);
      }
      length = Math.min(length, b.length - pos);
      for (int i = 1; i < length; i++) {
         sb.append(separator).append(b[pos + i]);
      }
      return sb.toString();
   }

   public static String toString(boolean[] b, int pos, int length, String separator) {
      StringBuilder sb = new StringBuilder();
      if (b.length > 0) {
         sb.append(b[pos]);
      }
      length = Math.min(length, b.length - pos);
      for (int i = 1; i < length; i++) {
         sb.append(separator).append(b[pos + i]);
      }
      return sb.toString();
   }

   public static String toString(int[] b, int pos, int length, String separator) {
      StringBuilder sb = new StringBuilder();
      if (b.length > 0) {
         sb.append(b[pos]);
      }
      length = Math.min(length, b.length - pos);
      for (int i = 1; i < length; i++) {
         sb.append(separator).append(b[pos + i]);
      }
      return sb.toString();
   }

   public static String toString(double[] b, int pos, int length, String separator) {
      StringBuilder sb = new StringBuilder();
      if (b.length > 0) {
         sb.append(b[pos]);
      }
      length = Math.min(length, b.length - pos);
      for (int i = 1; i < length; i++) {
         sb.append(separator).append(b[pos + i]);
      }
      return sb.toString();
   }

   public static boolean equals(double a[], double b[]) {
      if (a.length != b.length) {
         return false;
      }
      for (int i = 0; i < a.length; i++) {
         if (a[i] != b[i]) {
            return false;
         }
      }
      return true;
   }

   public static boolean equals(int a[], int b[]) {
      if (a.length != b.length) {
         return false;
      }
      for (int i = 0; i < a.length; i++) {
         if (a[i] != b[i]) {
            return false;
         }
      }
      return true;
   }

   public static double[] subArray(double a[], int pos) {
      return subArray(a, pos, a.length - pos);
   }

   public static double[] subArray(double[] a, int pos, int length) {
      double b[] = new double[length];
      System.arraycopy(a, pos, b, 0, length);
      return b;
   }

   public static char[] subArray(char[] a, int pos, int length) {
      char b[] = new char[length];
      System.arraycopy(a, pos, b, 0, length);
      return b;
   }

   public static byte[] subArray(byte[] a, int pos, int length) {
      byte b[] = new byte[length];
      System.arraycopy(a, pos, b, 0, length);
      return b;
   }

   public static String[] subArray(String a[], int pos) {
      return (String[])subArray(a, pos, a.length - pos);
   }
   
   public static Object[] subArray(Object a[], int pos) {
      return subArray(a, pos, a.length - pos);
   }

   public static Object[] subArray(Object[] a, int pos, int length) {
      Object b[] = createArray(a, length);
      System.arraycopy(a, pos, b, 0, length);
      return b;
   }

   public static int[] subArray(int[] a, int pos, int length) {
      int b[] = new int[length];
      System.arraycopy(a, pos, b, 0, length);
      return b;
   }

   public static int[] subArray(int a[], int pos) {
      return subArray(a, pos, a.length - pos);
   }

   public static int[] slice(int a[][], int pos) {
      int r[] = new int[a.length];
      for (int i = 0; i < a.length; i++)
         r[i] = a[i][pos];
      return r;
   }

   public static double[] slice(double a[][], int pos) {
      double r[] = new double[a.length];
      for (int i = 0; i < a.length; i++)
         r[i] = a[i][pos];
      return r;
   }

   public static void swap( Object a[], int p1, int p2) {
      Object p3 = a[p1];
      a[p1] = a[p2];
      a[p2] = p3;
   }
   
   public static <K> boolean contains(K needle, K... objects) {
      for (int i = 0; i < objects.length; i++) {
         if (needle.equals(objects[i])) {
            return true;
         }
      }
      return false;
   }

   public static boolean contains(int needle, int... haystack) {
      for (int i = 0; i < haystack.length; i++) {
         if (needle == haystack[i]) {
            return true;
         }
      }
      return false;
   }

   public static void fill(double[] array, double value) {
      int len = array.length;
      if (len > 0) {
         array[0] = value;
      }
      for (int i = 1; i < len; i += i) {
         System.arraycopy(array, 0, array, i,
                 ((len - i) < i) ? (len - i) : i);
      }
   }
   
   public static void fill(int[] array, int value) {
      int len = array.length;
      if (len > 0) {
         array[0] = value;
      }
      for (int i = 1; i < len; i += i) {
         System.arraycopy(array, 0, array, i,
                 ((len - i) < i) ? (len - i) : i);
      }
   }
}
