package io.github.htools.lib;

import java.util.ArrayList;

public class MemoryManager {

   public static Log log = new Log(MemoryManager.class);
   public static MemoryManager instance = new MemoryManager();
   ArrayList<Client> clients = new ArrayList<Client>();
   Runtime runtime;
   long freememory, totalmemory, usedmemory;
   double THRESHOLDMIN = 0.85, THRESHOLDMAX = 0.90;

   private MemoryManager() {
      runtime = Runtime.getRuntime();
      poll();
   }

   public void addClient(MemoryClient mc, int min, int max) {
      Client client = new Client();
      client.client = mc;
      client.min = min;
      client.max = max;
      assignMemory();
   }

   public void poll() {
      freememory = runtime.freeMemory();
      totalmemory = runtime.totalMemory();
      usedmemory = totalmemory - freememory;
   }

   public void assignMemory() {
      long maxmemory = 0, minmemory = 0, currentmemory = 0;
      for (Client c : clients) {
         maxmemory += c.max;
         minmemory += c.min;
         currentmemory += c.current;
      }
      if (THRESHOLDMIN * totalmemory - usedmemory > maxmemory - currentmemory) {
         for (Client c : clients) {
            c.suggest = c.max;
         }
      } else if (THRESHOLDMAX * totalmemory < usedmemory - (currentmemory - minmemory)) {
         for (Client c : clients) {
            c.suggest = c.min;
         }
      } else if (usedmemory < THRESHOLDMIN * totalmemory) {
         Client max = clients.get(0);
         long total = 0;
         for (Client c : clients) {
            if (c.priority * java.lang.Math.log(c.max - c.current)
                    > max.priority * java.lang.Math.log(max.max - max.current)) {
               max = c;
            }
            total += c.priority * java.lang.Math.log(c.max - c.current);
         }
         max.suggest = java.lang.Math.min(max.max,
                 max.current + (int) (freememory * max.priority * java.lang.Math.log(max.max - max.current) / total));
      } else if (usedmemory > THRESHOLDMAX * totalmemory) {
         Client max = clients.get(0);
         long total = 0;
         for (Client c : clients) {
            if ((c.current - c.min) / c.priority
                    > (max.current - max.min) / c.priority) {
               max = c;
            }
            total += (c.current - c.min) / c.priority;
         }
         max.suggest = java.lang.Math.max(max.min,
                 max.current - (int) (max.current * (max.current - max.min) / (max.priority) / total));
      }
      for (Client c : clients) {
         if (c.current < c.min && c.suggest < c.min) {
            c.suggest = c.min;
         }
      }
      for (Client c : clients) {
         double diff = (c.suggest - c.current) / c.current;
         if (diff < -0.1 || diff > 0.1 || (c.current < c.min && c.suggest > c.current)) {
            c.current = c.client.changeMemory(c.suggest);
         }
      }
   }

   class Client {

      MemoryClient client;
      int min;
      int current;
      int suggest;
      int max;
      int priority;
   }
}
