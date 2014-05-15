package io.github.repir.tools.ByteSearch;
import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextFile;
import io.github.repir.tools.Lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class TRECfile extends StructuredTextFile {
  public static Log log = new Log( TRECfile.class ); 
  public FolderNode top = this.addRoot("top", "<top>", "</top>", "<top>\n", "</top>");
  public IntField number = this.addInt(top, "number", "<num>\\s*Number\\s*:", "(?=<)|$", "  <num> Number : ", "\n");
  public StringField title = this.addString(top, "title", "<title>\\s*Topic\\s*:", "(?=<)|$", "  <title> Topic : \n    ", "\n");
  public StringField description = this.addString(top, "description", "<desc>\\s*Description\\s*:", "(?=<)|$", "  <desc> Description : \n    ", "\n");
  public StringField summary = this.addString(top, "summary", "<smry>\\s*Summary\\s*:", "(?=<)|$", "  <smry> Summary : \n    ", "\n");
  public StringField narrative = this.addString(top, "narrative", "<narr>\\s*Narrative\\s*:", "(?=<)|$", "  <narr> Narrative : \n    ", "\n");
  
  public TRECfile(BufferReaderWriter r) {
     super(r);
  }
  
  public TRECfile(Datafile r) {
     super(r);
  }
  
  public static TRECfile setup() {
     StringBuilder sb = new StringBuilder();
     sb.append("  <top>  \n").append("  <num> Number : 01\n");
     sb.append("  <title> topic: aap noot ");
     sb.append("</top>\n");
     sb.append("  <top>  \n").append("  <num> Number : 02\n");
     sb.append("  <title> topic: mies wim ");
     sb.append("  <desc> description: zus jet ");
     sb.append("</top>\n");
     return new TRECfile(new BufferReaderWriter(sb.toString().getBytes()));
  }
  
   public static void main(String[] args) {
      TRECfile f = setup();
      while (f.next()) {
        log.info("%s %s", f.title.get(), f.description.get());
     }
      f = new TRECfile(new Datafile("/home/jer/Desktop/aap.txt"));
      f.openWrite();
      f.number.set(10);
      f.title.set("aap");
      f.write();
      f.closeWrite();
  }
}
