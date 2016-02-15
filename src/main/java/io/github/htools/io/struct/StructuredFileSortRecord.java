package io.github.htools.io.struct;

public abstract class StructuredFileSortRecord implements Comparable<StructuredFileSortRecord> {
   protected StructuredFileSort file;

   public StructuredFileSortRecord( StructuredFileSort file) {
      setFile( file );
   }

   public void setFile( StructuredFileSort file ) {
      this.file = file;
   }
   
   @Override
   public final int compareTo(StructuredFileSortRecord o) {
      return file.compareKeys(this, o);
   }

   public abstract void write();

   protected abstract void writeFinal();
}
