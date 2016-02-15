package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

import java.util.ArrayList;

/**
 * Allows extracting pieces of content, that can be restored after erasing an entire
 * section. For example, for HTML Pages the head section may be erased because
 * it contains much noise, but the title of the page must be kept. In such case,
 * an extension of ExtractRestore can be used to {@link #add(io.github.htools.extract.Content, int, int)  }
 * pieces of content that must be preserved, and after erasing the section
 * use "+extractor.sectionprocess = all ExtractRestore" to restore.
 * <p>
 * @author jbpvuurens
 */
public class ExtractRestore extends ExtractorProcessor {

   public static Log log = new Log(ExtractRestore.class);
   public static ArrayList<StoredContent> content = new ArrayList<StoredContent>();

   public ExtractRestore(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (StoredContent sc : content) {
         sc.restore(entity.content);
      }
      content = new ArrayList<StoredContent>();
   }

   public void add(Content entity, int start, int end) {
      content.add(new StoredContent(entity.content, start, end));
   }

   static class StoredContent {

      int pos;
      byte content[];

      public StoredContent(byte buffer[], int start, int end) {
         content = new byte[end - start];
         System.arraycopy(buffer, start, content, 0, end - start);
         pos = start;
      }

      public void restore(byte buffer[]) {
         System.arraycopy(content, 0, buffer, pos, content.length);
         if (buffer[pos - 1] == 0) {
            buffer[pos - 1] = 32;
         }
         if (buffer[ pos + content.length] == 0) {
            buffer[pos + content.length] = 32;
         }
      }
   }
}
