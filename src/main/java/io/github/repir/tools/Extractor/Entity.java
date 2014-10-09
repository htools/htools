package io.github.repir.tools.Extractor;

import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Structure.StructureReader;
import io.github.repir.tools.Structure.StructureWriter;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

/**
 * An Entity is the result of an {@link EntityReader} that reads the entities
 * from a collection's source. The Entity is a data container class, containing
 * both the original byte content and extracted {@link Section}s, which is
 * converted into a Map of {@link EntityChannel}s. These {EntityChannel}s are
 * used by the {@link StoredFeature}s to directly derive their value.
 * <p\>
 * DUring extraction, the content may be modified (e.g. removing junk,
 * lowercasing ). By definition, \0 bytes are ignored, e.g. "Ein\0stein" ==
 * "Einstein". This is used to erase bytes, without having to shift content or
 * adding whitespace which would cause words to break.
 */
public class Entity extends HashMap<String, EntityChannel> implements io.github.repir.tools.Buffer.BufferSerializable {

   public static Log log = new Log(Entity.class);
   public byte[] content;
   public TreeSet<Section> positions = new TreeSet<Section>();
   private HashMap<String, ArrayList<Section>> sectionpositions = new HashMap<String, ArrayList<Section>>();
   public long offset; //  currently not send over MR, could be used for debugging

   public Entity() {
   }

   public void setContent(byte[] content) {
      this.content = content;
   }

   public EntityChannel get(String channelname) {
      if (channelname == null) {
         return null;
      }
      EntityChannel d = super.get(channelname);
      if (d == null) {
         d = new EntityChannel(this, (String) channelname);
         put(channelname, d);
      }
      return d;
   }

   @Override
   public void write(StructureWriter writer) {
      writer.writeByteBlock(content);
      writer.writeC(this.size());
      for (Map.Entry<String, EntityChannel> entry : this.entrySet()) {
         writer.write(entry.getKey());
         entry.getValue().write(writer);
      }
      writer.writeC(sectionpositions.size());
      for (Map.Entry<String, ArrayList<Section>> entry : sectionpositions.entrySet()) {
         writer.write(entry.getKey());
         writer.writeC(entry.getValue().size());
         for (Section p : entry.getValue()) {
            p.write(writer);
         }
      }
   }

   @Override
   public void read(StructureReader reader) throws EOCException {
      content = reader.readByteBlock();
      int attributes = reader.readCInt();
      for (int i = 0; i < attributes; i++) {
         String attributename = reader.readString();
         EntityChannel attribute = new EntityChannel(this, attributename);
         attribute.read(reader);
         put(attributename, attribute);
      }
      int sections = reader.readCInt();
      for (int i = 0; i < sections; i++) {
         String sectionname = reader.readString();
         int sectionsize = reader.readCInt();
         ArrayList<Section> list = new ArrayList<Section>();
         for (int j = 0; j < sectionsize; j++) {
            Section sectionpos = new Section();
            sectionpos.read(reader);
            list.add(sectionpos);
         }
         sectionpositions.put(sectionname, list);
      }
   }

   public void addSectionPos(String section, int openlead, int open, int close, int closetrail) {
      ArrayList<Section> list = sectionpositions.get(section);
      if (list == null) {
         list = new ArrayList<Section>();
         sectionpositions.put(section, list);
      }
      list.add(new Section(openlead, open, close, closetrail));
   }

   public ArrayList<Section> getSectionPos(String section) {
      ArrayList<Section> list = sectionpositions.get(section);
      return (list != null) ? list : new ArrayList<Section>();
   }

   public static class Section implements Comparable<Section>, io.github.repir.tools.Buffer.BufferSerializable {

      public int openlead;
      public int open;
      public int close;
      public int closetrail;

      public Section() {
      }

      public Section(int openlead, int open, int close, int closetrail) {
         this.openlead = openlead;
         this.open = open;
         this.close = close;
         this.closetrail = closetrail;
      }

      @Override
      public void write(StructureWriter writer) {
         writer.writeC(openlead);
         writer.writeC(open);
         writer.writeC(close);
         writer.writeC(closetrail);
      }

      @Override
      public void read(StructureReader reader) throws EOCException {
         openlead = reader.readCInt();
         open = reader.readCInt();
         close = reader.readCInt();
         closetrail = reader.readCInt();
      }

      public int compareTo(Section o) {
         return (open < o.open) ? -1 : ((open > o.open) ? 1 : 0);
      }
   }
}
