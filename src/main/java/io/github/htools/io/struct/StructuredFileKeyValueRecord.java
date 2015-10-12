package io.github.htools.io.struct;

import java.io.IOException;

public interface StructuredFileKeyValueRecord<F extends StructuredFileKeyInterface> {
   
   @Override
   public boolean equals(Object r);
   
   @Override
   public int hashCode();
   
   public void write( F file ) throws IOException;
   
   public void read( F file ) throws IOException;
   
   public void convert( StructuredFileKeyValueRecord record );
}
