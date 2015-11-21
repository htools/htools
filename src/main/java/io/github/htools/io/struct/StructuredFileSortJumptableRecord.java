package io.github.htools.io.struct;
import java.io.IOException;
import java.lang.reflect.Method;

public abstract class StructuredFileSortJumptableRecord extends StructuredFileSortRecord {

   public StructuredFileSortJumptableRecord( StructuredFileSortedByteJumptable t) {
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
      ((StructuredFileSortedByteJumptable)file).idfile.write(((StructuredFileSortedByteJumptable)file).id++, file);
      //log.info("id %d", tt.id);
   }

   protected void writeTempRecordData() {
      writeRecordData();
   }

   protected abstract void writeRecordData();

   public StructuredFileSortJumptableRecord clone(StructuredFile t) {
      StructuredFileSortJumptableRecord record = null;
      try {
         Method declaredConstructor = ((StructuredFileSortedByteJumptable)file).getClass().getDeclaredMethod("createRecord");
         record = (StructuredFileSortJumptableRecord) declaredConstructor.invoke(file);
      } catch (Exception ex) {
         StructuredFileSortedByteJumptable.log.exception(ex, "clone( %s )", t);
      }
      return record;
   }
}
