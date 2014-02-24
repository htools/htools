package io.github.repir.tools.Content;
import io.github.repir.tools.Lib.Log;

public abstract class RecordSortRecord implements Comparable<RecordSortRecord> {
   protected RecordSort file;

   public RecordSortRecord( RecordSort file) {
      setFile( file );
   }

   public void setFile( RecordSort file ) {
      this.file = file;
   }
   
   @Override
   public final int compareTo(RecordSortRecord o) {
      return file.compareKeys(this, o);
   }

   public abstract void write();

   protected abstract void writeFinal();
}
