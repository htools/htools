package io.github.repir.tools.extract;

import io.github.repir.tools.collection.HashMapList;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.struct.StructureReader;
import io.github.repir.tools.io.struct.StructureWriter;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

/**
 * An Entity is the result of an {@link EntityReader} that reads the entities
 * from a collection's source. The Entity is a data container class, containing
 * both the original byte content and extracted {@link Section}s, which is
 * converted into a Map of {@link ExtractChannel}s. These {EntityChannel}s are
 * used by the {@link StoredFeature}s to directly derive their value.
 * <p\>
 * DUring extraction, the content may be modified (e.g. removing junk,
 * lowercasing ). By definition, \0 bytes are ignored, e.g. "Ein\0stein" ==
 * "Einstein". This is used to erase bytes, without having to shift content or
 * adding whitespace which would cause words to break.
 */
public class Content extends HashMap<String, ExtractChannel> {

   public static Log log = new Log(Content.class);
   public byte[] content;
   public TreeSet<ByteSearchSection> positions = new TreeSet();
   private HashMapList<String, ByteSearchSection> sectionpositions = new HashMapList();
   public long offset; //  currently not send over MR, could be used for debugging

   public Content() {
   }

   public void setContent(byte[] content) {
      this.content = content;
   }

   public ExtractChannel get(String channelname) {
      if (channelname == null) {
         return null;
      }
      ExtractChannel d = super.get(channelname);
      if (d == null) {
         d = new ExtractChannel(this, (String) channelname);
         put(channelname, d);
      }
      return d;
   }

   public ByteSearchSection addSectionPos(String sectionlabel, byte[] haystack, int openlead, int open, int close, int closetrail) {
      ByteSearchSection s = new ByteSearchSection(haystack, openlead, open, close, closetrail);
      addSectionPos(sectionlabel, s);
      return s;
   }
   
   protected HashMapList<String, ByteSearchSection> getSectionPositions() {
       return sectionpositions;
   }

   public void addSectionPos(String sectionlabel, ByteSearchSection section) {
      sectionpositions.getList(sectionlabel).add(section);
   }

   public ArrayList<ByteSearchSection> getSectionPos(String section) {
      ArrayList<ByteSearchSection> list = sectionpositions.get(section);
      return (list != null) ? list : new ArrayList();
   }
}
