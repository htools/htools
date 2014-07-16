package io.github.repir.tools.Structure;

import io.github.repir.tools.Lib.Log;

public abstract class StructuredFileKeyRecord<F extends StructuredFileKey, D extends StructuredFile> implements StructuredFileKeyValueRecord<F> {
   public static Log log = new Log(StructuredFileKeyRecord.class);
   public long offset;
   public int length;
   
   @Override
   public abstract boolean equals(Object r);
   
   @Override
   public abstract int hashCode();
   
   public abstract void writeKeys( F file );
   
   public abstract void writeData( D file );
   
   protected abstract void getKeys( F file );
   
   public abstract void getData( D file );
   
   @Override
   public void read( F file ) {
      offset = file.offset.value;
      length = file.length.value;
      getKeys(file);
   }
   
   @Override
   public void write( F file ) {
      file.offset.write(offset);
      file.length.write(length);
      writeKeys( file );
   }
   
   public void write( D datastorage ) {
      writeData( datastorage );
      offset = datastorage.getOffsetTupleStart();
      length = (int)(datastorage.getOffetTupleEnd() - datastorage.getOffsetTupleStart());
   }
}
