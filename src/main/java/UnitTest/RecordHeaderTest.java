package UnitTest;
import java.util.UUID;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordBinary;
import io.github.repir.tools.Content.RecordHeader;
import io.github.repir.tools.Content.RecordHeaderDataRecord;
import io.github.repir.tools.Content.RecordHeaderInterface;
import io.github.repir.tools.Content.RecordHeaderRecord;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class RecordHeaderTest {
  public static Log log = new Log( RecordHeaderTest.class ); 

   public static void main(String[] args) {
     String keys1[] = { "aap", "noot", "mies", "wim", "zus", "jet" };
     String values[] = new String[keys1.length];
     for (int i = 0; i < keys1.length; i++) {
        values[i] = UUID.randomUUID().toString();
        log.info("%d %s %s", i, keys1[i], values[i]);  
     }
     File f = new File(new Datafile(HDTools.getFS(), "/user/jeroenv/testfile"));
     for (int p = 0; p < keys1.length; p++) {
       f.openWriteAppend();
       Record r = f.newRecord();
       r.uuid = values[p];
       r.id = p;
       r.term = keys1[p];
       f.write(r);
       f.closeWrite();
       
       f.openRead();
       for (int i = 0; i <= p; i++) {
          r = f.newRecord();
          r.term = keys1[i];
          r = f.find(r);
          log.info("find %s %d %d", r.term, r.offset, r.length);
          log.info("%s %d", r.uuid, r.id);
       }
     }
     
  }
  
  static class File extends RecordHeader<Record, DataFile> {
      StringField term = this.addString("term");
     
      public File( Datafile df ) {
         super(df);
      }

      @Override
      public DataFile createDatafile(Datafile df) {
         return new DataFile(df);
      }
      
      @Override
      public Record newRecord() {
         return new Record();
      }
     
  }

  static class Record extends RecordHeaderRecord<File, DataFile> {
      String term;
      String uuid;
      int id;

      @Override
      public boolean equals(Object r) {
         return (r instanceof Record && ((Record)r).term.equals(term));
      }

      @Override
      public int hashCode() {
         int hash = 7;
         hash = 79 * hash + (this.term != null ? this.term.hashCode() : 0);
         return hash;
      }

      @Override
      public void writeKeys(File file) {
         ((File)file).term.write(term);
      }

      @Override
      public void writeData2(DataFile file) {
         file.uuid.write(uuid);
         file.id.write(id);
      }

      @Override
      public void getKeys(File file) {
         term = ((File)file).term.value;
      }
     
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("term=").append(term).append(" offset=").append(offset).append(" length=").append(length);
         return sb.toString();
      }

      @Override
      public void getData(DataFile file) {
         if (file.next()) {
            uuid = file.uuid.value;
            id = file.id.value;
         }
      }

      @Override
      public void convert(RecordHeaderDataRecord rec) {
         Record r = (Record)rec;
         r.id = id;
         r.term = term;
         r.uuid = uuid;
      }
  }
  
  static class DataFile extends RecordBinary {
     StringField uuid = this.addString("uuid");
     IntField id = this.addInt("id");
     
     public DataFile( Datafile df ) {
        super(df);
     }
  }
  
}
