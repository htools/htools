package io.github.repir.tools.Content;

public interface StructuredFileKeyValueRecord<F extends StructuredFileKeyInterface> {
   
   @Override
   public boolean equals(Object r);
   
   @Override
   public int hashCode();
   
   public void write( F file );
   
   public void read( F file );
   
   public void convert( StructuredFileKeyValueRecord record );
}
