package io.github.htools.extract;

import io.github.htools.io.EOCException;
import io.github.htools.io.struct.StructureReader;
import io.github.htools.io.struct.StructureWriter;
import io.github.htools.lib.Log;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Data class to hold the chunks that belong to one tokenized channel of a
 * document.
 */
public class ExtractChannel implements io.github.htools.io.buffer.BufferSerializable,
        Iterable<String> {
   public static Log log = new Log( ExtractChannel.class );
   ArrayList<String> terms = new ArrayList();
   public String channel;
   public String contentstring;
   public int tokenized[];
   public Content entity;

   public ExtractChannel(Content entity, String name) {
      this.entity = entity;
      channel = name;
   }

   @Override
   public ExtractChannel clone() {
      ExtractChannel c = new ExtractChannel( entity, channel );
      c.addAll(this.terms);
      return c;
   }
   
   public void addAll(ArrayList<String> terms) {
       this.terms.addAll(terms);
   }
   
   public void add(String term) {
       terms.add(term);
   }
   
   public int size() {
       return terms.size();
   }
   
   public String get(int i) {
       return terms.get(i);
   }
   
   public String set(int i, String s) {
       return terms.set(i, s);
   }
   
   public void clear() {
       terms = new ArrayList();
   }
   
   public void set(ArrayList<String> set) {
       terms = set;
   }
   
   public ArrayList<String> getTerms() {
       return terms;
   }
   
   /**
    * Add the separated chunks of content to a StringBuilder
    * <p>
    * @param r
    * @param seperator
    * @return
    */
   public StringBuilder getContent(StringBuilder r, String seperator) {
      boolean first = true;
      for (String chunk : this) {
         if (first) {
            first = false;
         } else {
            r.append(" ");
         }
         r.append(chunk.toString());
      }
      //log.info("getCOntent %s", r.toString());
      return r;
   }

   /**
    * Returns a space seperated String of all content Tokenized for the channel.
    * <p>
    * @return
    */
   public String getContentStr() {
      if (contentstring == null) {
         StringBuilder r = new StringBuilder();
         getContent(r, " ");
         contentstring = r.toString();
      }
      return contentstring;
   }

   @Override
   public void write(StructureWriter writer) {
      writer.write(size());
      for (String token : this) {
         writer.write(token);
      }
   }

   @Override
   public void read(StructureReader reader) throws EOCException {
      int size = reader.readInt();
      for (int i = 0; i < size; i++) {
         add(reader.readString());
      }
   }

   
    @Override
    public Iterator<String> iterator() {
       return terms.iterator();
    }
}
