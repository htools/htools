package io.github.repir.tools.Content;

import java.io.EOFException;
import io.github.repir.tools.Lib.Log;

public abstract class RecordHeaderRecord<F extends RecordHeader, D extends RecordBinary> implements RecordHeaderDataRecord<F> {
   public static Log log = new Log(RecordHeaderRecord.class);
   public long offset;
   public int length;
   
   @Override
   public abstract boolean equals(Object r);
   
   @Override
   public abstract int hashCode();
   
   public abstract void writeKeys( F file );
   
   public abstract void writeData2( D file );
   
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
      writeData2( datastorage );
      offset = datastorage.getOffsetTupleStart();
      length = (int)(datastorage.getOffetTupleEnd() - datastorage.getOffsetTupleStart());
   }
}
