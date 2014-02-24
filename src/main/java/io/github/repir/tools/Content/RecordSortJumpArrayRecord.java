package io.github.repir.tools.Content;
import java.lang.reflect.Method;
import io.github.repir.tools.Lib.Log;

public abstract class RecordSortJumpArrayRecord extends RecordSortRecord {

   public RecordSortJumpArrayRecord( RecordSortJumpArray t) {
      super(t);
   }

   public abstract void read();

   @Override
   public void write() {
      writeTempRecordData();
   }

   @Override
   protected void writeFinal() {
      //setHashCode();
      //RecordSortJumpArray tt = (RecordSortJumpArray) file;
      //int recordstart = (int)t.getOffset();
      writeRecordData();
      //log.info("WriteFinal id %d offset %d", tt.id, oldoffset );
      ((RecordSortJumpArray)file).idfile.write(((RecordSortJumpArray)file).id++, file);
      //log.info("id %d", tt.id);
   }

   protected void writeTempRecordData() {
      writeRecordData();
   }

   protected abstract void writeRecordData();

   public RecordSortJumpArrayRecord clone(RecordBinary t) {
      RecordSortJumpArrayRecord record = null;
      try {
         Method declaredConstructor = ((RecordSortJumpArray)file).getClass().getDeclaredMethod("createRecord");
         record = (RecordSortJumpArrayRecord) declaredConstructor.invoke(file);
      } catch (Exception ex) {
         RecordSortJumpArray.log.exception(ex, "clone( %s )", t);
      }
      return record;
   }
}
