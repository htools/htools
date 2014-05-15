package io.github.repir.tools.DataTypes;

import java.util.Collection;
import io.github.repir.tools.Lib.Log;
import java.util.Comparator;
import java.util.TreeSet;
import io.github.repir.tools.DataTypes.TreeMapComparable.TYPE;
import io.github.repir.tools.DataTypes.TreeMapComparator.defaultComparator;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class TreeSetComparator<K> extends TreeSet<K> {

   public static Log log = new Log(TreeSetComparator.class);
   
   public TreeSetComparator( Comparator comparator, TYPE t) {
      super(new defaultComparator( comparator, t));
   }
   
   public TreeSetComparator( Comparator comparator, TYPE t, Collection<K> collection) {
      super(new defaultComparator( comparator, t));
      this.addAll(collection);
   }
}
