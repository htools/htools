package io.github.htools.search;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredTextCSV;
import io.github.htools.lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class QRELfile extends StructuredTextCSV {
  public static Log log = new Log( QRELfile.class ); 
  public IntField number = this.addInt("topic", "", "\\s", "", " ");
  public IntField system = this.addInt("system", "", "\\s", "", " ");
  public StringField docid = this.addString("docid", "", "\\s", "", " ");
  public IntField relevance = this.addInt("relevance", "", "($|\\s)", "", "");
  
  public QRELfile(BufferReaderWriter r) {
     super(r);
  }
  
  public QRELfile(Datafile r) {
     super(r);
  }
  
  public static QRELfile setup() {
     StringBuilder sb = new StringBuilder();
     sb.append("101 0 AP880212-0047 1\n" +
"101 0 AP880219-0139 0\n" +
"101 0 AP880219-0166 0\n" +
"101 0 AP880222-0172 0\n" +
"101 0 AP880223-0104 0\n" +
"101 0 AP880229-0146 0\n" +
"101 0 AP880314-0113 0\n" +
"101 0 AP880314-0121 0\n" +
"101 0 AP880314-0145 0\n" +
"101 0 AP880320-0041 0");
     return new QRELfile(new BufferReaderWriter(sb.toString().getBytes()));
  }
  
   public static void main(String[] args) {
      QRELfile f = setup();
     while (f.nextRecord()) {
        log.info("%s, %d", f.docid.get(), f.relevance.get());
     }
     f = new QRELfile(new Datafile("/home/jer/Desktop/aap.txt"));
     f.openWrite();
     f.number.set(101);
     f.system.set(0);
     f.docid.set("APPP");
     f.relevance.set(1);
     f.write();
     f.number.set(102);
     f.system.set(1);
     f.docid.set("BPPP");
     f.relevance.set(2);
     f.write();
     f.closeWrite();
  }
}
