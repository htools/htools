package io.github.repir.tools.lib;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author jeroen
 */
public enum CollectionTools {;

   public static Log log = new Log(CollectionTools.class);

   /**
    * Clusters collections in the list that have shared members
    * @param list 
    */
   public static void cluster(ArrayList<? extends Collection> list) {
       for (int i = 1; i < list.size(); i++) {
           Collection c = list.get(i);
           Collection match = null;
           for (int j = 0; j < i; j++) {
               Collection s = list.get(j);
               for (Object o : c) {
                   if (s.contains(o)) {
                      if (match == null) {
                          match = s;
                      } else {
                          match.addAll(s);
                          list.remove(j);
                          i--;
                          j--;
                      }
                      break;
                   }
               }
           }
           if (match != null) {
               match.addAll(c);
               list.remove(i--);
           }
       }
   }
}
