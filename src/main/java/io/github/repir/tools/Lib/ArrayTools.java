package io.github.repir.tools.Lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jeroen
 */
public enum ArrayTools {;

   public static Log log = new Log(ArrayTools.class);

   public static Object[] group(Object... obj) {
      return obj;
   }

   public static Object[] addArr(Object[] obj, Object... o) {
      Object n[] = resize( obj, obj.length + o.length);
      System.arraycopy(o, 0, n, obj.length, o.length);
      return n;
   }

   public static Object[] resize(Object[] obj, int newsize) {
       if (newsize == obj.length)
           return obj;
      Object n[] = createArray( obj, newsize);
      if (newsize > obj.length)
         System.arraycopy(obj, 0, n, 0, obj.length);
      else 
         System.arraycopy(obj, 0, n, 0, newsize);
      return n;
   }

   public static Object[] addObjectToArr(Object[] obj, Object o) {
      Object n[] = resize( obj, obj.length + 1);
      n[obj.length] = o;
      return n;
   }

   public static void copy(Object[] obj, Object[] dest) {
      System.arraycopy(obj, 0, dest, 0, Math.min(obj.length, dest.length));
   }

   public static Object[] union(Object[] ... morearrays) {
      int length = 0;
      for (Object[] oo : morearrays) {
         length += oo.length;
      }
      Object dest[] = createArray( morearrays[0], length );
      int pos = 0;
      for (Object[] oo : morearrays) {
         System.arraycopy(oo, 0, dest, pos, oo.length);
         pos += oo.length;
      }
      return dest;
   }
   
   public static Object[] union(Object[] a, Object [] b) {
      Object c[] = resize( a, a.length + b.length );
      System.arraycopy(b, 0, c, a.length, b.length);
      return c;
   }
   
   public static Object[] createArray( Object other[], int size) {
      Class stringArrayClass = other.getClass();
      return createArray(stringArrayClass.getComponentType(), size);
   }

   public static Object[] createArray( Class datatype, int size) {
      return (Object[]) java.lang.reflect.Array.newInstance(datatype, size);
   }

   public static int indexOf(Object array[], Object needle) {
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
   public static Object[] arrayOfOthers( Collection c, Object t ) {
      ArrayList<Object> list = new ArrayList<Object>();
      for (Object o : c) {
         if (o != t) {
            list.add(o);
         }
      }
      return list.toArray(createArray( t.getClass(), list.size()));
   }

   public static int[] clone(int[] a) {
      int r[] = new int[a.length];
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

   public static String[] toStringArray(Collection<String> strings) {
      return strings.toArray(new String[ strings.size()] );
   }

   public static double[] toDoubleArray(Collection<Double> integers) {
      double[] ret = new double[integers.size()];
      Iterator<Double> iterator = integers.iterator();
      for (int i = 0; i < ret.length; i++) {
         ret[i] = iterator.next().doubleValue();
      }
      return ret;
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

   public static String concat(Object[] b) {
      return ArrayTools.concat(b, 0, b.length, ", ");
   }

   public static String concat(byte[] b) {
      return ArrayTools.concat(b, 0, b.length, ", ");
   }

   public static String concat(int[] b) {
      return (b == null) ? "[ null ]" : ArrayTools.concat(b, 0, b.length, ", ");
   }

   public static String concat(int[][] b) {
      if (b == null)
         return null;
      if (b.length == 0)
         return "{}";
      StringBuilder sb = new StringBuilder();
      sb.append("{ { ").append(ArrayTools.concat(b[0])).append((" }"));
      for (int i = 1; i < b.length; i++)
         sb.append(",\n{ ").append(ArrayTools.concat(b[i])).append(" }");
      return sb.append(" }").toString();
   }

   public static String concat(int[][][] b) {
      if (b == null)
         return null;
      if (b.length == 0)
         return "{}";
      StringBuilder sb = new StringBuilder();
      sb.append("{ { ").append(ArrayTools.concat(b[0])).append((" }"));
      for (int i = 1; i < b.length; i++)
         sb.append(",\n{ ").append(ArrayTools.concat(b[i])).append(" }");
      return sb.append(" }").toString();
   }

   public static String concat(long[] b) {
      return (b == null) ? "[ null ]" : ArrayTools.concat(b, 0, b.length, ", ");
   }

   public static String concat(double[] b) {
      return ArrayTools.concat(b, 0, b.length, ", ");
   }

   public static String concat(byte[] b, int pos, int length, String separator) {
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

   public static String concat(Object[] b, int pos, int length, String separator) {
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

   public static String concat(List list, String separator) {
      StringBuilder sb = new StringBuilder();
      for (Object i : list) {
         sb.append(separator).append(i);
      }
      sb.delete(0, separator.length()).insert(0, "[ ").append(" ]");
      return sb.toString();
   }

   public static String concat(List list) {
      return concat(list, ", ");
   }

   public static String concatStr(List<String> list) {
      if (list.size() == 0) {
         return "[ ]";
      }
      StringBuilder sb = new StringBuilder();
      for (String i : list) {
         sb.append(", ").append(i);
      }
      sb.setCharAt(0, '[');
      return sb.append(" ]").toString();
   }

   public static String concatStr(Collection<String> list, String separator) {
      if (list.size() < 1)
         return "";
      StringBuilder sb = new StringBuilder();
      for (String i : list) {
         sb.append(separator).append(i);
      }
      return sb.delete(0, separator.length()).toString();
   }

   public static String concat(long[] b, int pos, int length, String separator) {
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

   public static String concat(int[] b, int pos, int length, String separator) {
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

   public static String concat(double[] b, int pos, int length, String separator) {
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
   
   public static boolean contains(Object needle, Object... objects) {
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
