package io.github.repir.tools.extract;

import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.struct.StructureReader;
import io.github.repir.tools.io.struct.StructureWriter;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 * Data class to hold the chunks that belong to one tokenized channel of a
 * document.
 */
public class ExtractChannel extends ArrayList<String> implements io.github.repir.tools.io.buffer.BufferSerializable {
   public static Log log = new Log( ExtractChannel.class );
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
      c.addAll(this);
      return c;
   }
   
   /**
    * Add the separated chunks of content to a StringBuilder
    * <p/>
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
    * <p/>
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
}
