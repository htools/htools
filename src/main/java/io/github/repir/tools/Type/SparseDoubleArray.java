package io.github.repir.tools.Type;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.Log;

public class SparseDoubleArray {

   public static Log log = new Log(SparseDoubleArray.class);
   public boolean indexchanged;
   public int maxindex = -1;
   public double value[] = new double[0];
   public int index[] = new int[0];

   public SparseDoubleArray(double values[]) {
      super();
      int length = 0;
      for (double i : values) {
         if (i != 0) {
            length++;
         }
      }
      index = new int[length];
      value = new double[length];
      int currentindex = 0;
      maxindex = 0;
      if (length > 0) {
         for (double v : values) {
            if (v == 0) {
               index[currentindex]++;
            } else {
               value[currentindex] = v;
               if (++currentindex < length) {
                  index[currentindex] = 1;
               } else {
                  break;
               }
            }
            maxindex++;
         }
      }
   }

   public SparseDoubleArray(int keys[]) {
      super();
      HashMap<Integer, Double> map = new HashMap<Integer, Double>();
      for (int key : keys) {
         Double f = map.get(key);
         if (f == null) {
            map.put(key, 1.0);
         } else {
            map.put(key, f + 1);
         }
      }
      set(new TreeMap<Integer, Double>(map));
   }

   public SparseDoubleArray(TreeMap<Integer, Double> sortedmap) {
      super();
      set(sortedmap);
   }

   public void set(TreeMap<Integer, Double> sortedmap) {
      index = new int[sortedmap.size()];
      value = new double[sortedmap.size()];
      int i = 0;
      maxindex = 0;
      for (Map.Entry<Integer, Double> entry : sortedmap.entrySet()) {
         index[i] = entry.getKey() - maxindex;
         maxindex = entry.getKey();
         value[i++] = entry.getValue();
      }
   }

   public SparseDoubleArray alignSpace(SparseDoubleArray... arrays) {
      HashMap<Integer, Double> map = new HashMap<Integer, Double>();
      DoubleIterator iter = this.getIterator();
      TreeSet<DoubleIterator> set = new TreeSet<DoubleIterator>();
      for (SparseDoubleArray sa : arrays) {
         DoubleIterator i = sa.getIterator();
         if (i.next()) {
            set.add(i);
         }
      }
      if (iter.next()) {
         set.add(iter);
      }
      while (set.size() > 0) {
         int key = set.first().currentIndex();
         double value = 0;
         while (set.size() > 0 && set.first().currentIndex() == key) {
            DoubleIterator i = set.pollFirst();
            if (i == iter) {
               value = i.currentValue();
            }
            if (i.next()) {
               set.add(i);
            }
         }
         map.put(key, value);
      }
      return new SparseDoubleArray(new TreeMap<Integer, Double>(map));
   }

   public void set(int realindex, double value) {
      int pseudoindex = getIndex(realindex, true);
      this.value[ pseudoindex] = value;
   }

   public double get(int realindex) {
      int pseudoindex = getIndex(realindex, false);
      return (pseudoindex >= 0) ? value[pseudoindex] : 0;
   }

   /**
    * @param realindex
    * @return the internal index for a given realindex. If the realindex does
    * not exists and create=true, the index entry is appended or inserted. If
    * the realindex does not exists and create=false, -1 is returned.
    */
   public int getIndex(int realindex, boolean create) {
      int i = 0;
      if (realindex > maxindex) {
         if (create) {
            appendIndex(realindex);
            return index.length - 1;
         } else {
            return -1;
         }
      }
      for (; i < index.length; i++) {
         if (realindex == index[i]) {
            return i;
         }
         if (realindex > index[i]) {
            realindex -= index[i];
         } else {
            if (create) {
               insertIndex(i, realindex);
               return i;
            } else {
               return -1;
            }
         }
      }
      return -1;
   }

   public void appendIndex(int realindex) {
      int newindex[] = new int[index.length + 1];
      double newvalues[] = new double[index.length + 1];
      System.arraycopy(index, 0, newindex, 0, index.length);
      System.arraycopy(this.value, 0, newvalues, 0, index.length);
      newindex[ index.length] = realindex - maxindex;
      this.value = newvalues;
      this.index = newindex;
      this.maxindex = realindex;
      indexchanged = true;
   }

   public void insertIndex(int pos, int realindex) {
      int newindex[] = new int[index.length + 1];
      double newvalues[] = new double[index.length + 1];
      System.arraycopy(index, 0, newindex, 0, pos);
      System.arraycopy(this.value, 0, newvalues, 0, pos);
      newindex[ pos] = realindex;
      newindex[ pos + 1] = index[pos] - realindex;
      System.arraycopy(index, pos + 1, newindex, pos + 2, index.length - pos - 1);
      System.arraycopy(value, pos, newvalues, pos + 1, index.length - pos);
      this.value = newvalues;
      this.index = newindex;
      indexchanged = true;
   }

   public DoubleIterator getIterator() {
      return new SparseArrayIterator();
   }

   public void print() {
      StringBuilder sb = new StringBuilder();
      int realindex = 0;
      DoubleIterator iter = getIterator();
      while (iter.next()) {
         sb.append(iter.currentIndex()).append("=>").append(iter.currentValue()).append(" ");
      }
      log.printf("%s", sb.toString());
   }

   abstract class DoubleIterator implements Comparable<DoubleIterator> {

      public abstract boolean hasNext();

      public abstract boolean next();

      public abstract double currentValue();

      public abstract int currentIndex();

      public abstract void set(double value);

      public abstract void reset();

      @Override
      public int compareTo(DoubleIterator o) {
         return currentIndex() < o.currentIndex() ? -1 : 1;
      }
   }

   class SparseArrayIterator extends DoubleIterator {

      int realindex = 0;
      int pseudoindex = -1;

      public void reset() {
         pseudoindex = -1;
         realindex = 0;
      }

      public boolean hasNext() {
         return pseudoindex < index.length - 1;
      }

      public boolean next() {
         if (hasNext()) {
            realindex += index[++pseudoindex];
            return true;
         }
         return false;
      }

      public double currentValue() {
         return value[ pseudoindex];
      }

      public int currentIndex() {
         return realindex;
      }

      public void set(double newvalue) {
         value[ pseudoindex] = newvalue;
      }
   }

   class ArrayIterator extends DoubleIterator {

      double values[];
      int realindex = -1;

      public ArrayIterator(double array[]) {
         values = array;
      }

      public void reset() {
         realindex = -1;
      }

      public boolean hasNext() {
         while (realindex < index.length - 1 && values[realindex + 1] == 0) {
            realindex++;
         }
         return realindex < index.length - 1;
      }

      public boolean next() {
         if (hasNext()) {
            realindex++;
            return true;
         }
         return false;
      }

      public double currentValue() {
         return value[ realindex];
      }

      public int currentIndex() {
         return realindex;
      }

      public void set(double newvalue) {
         value[ realindex] = newvalue;
      }
   }
}
