package io.github.repir.tools.Content;

public interface RecordHeaderDataRecord<F extends RecordHeaderInterface> {
   
   @Override
   public boolean equals(Object r);
   
   @Override
   public int hashCode();
   
   public void write( F file );
   
   public void read( F file );
   
   public void convert( RecordHeaderDataRecord record );
}
