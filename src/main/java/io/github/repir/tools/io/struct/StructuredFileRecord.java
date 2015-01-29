package io.github.repir.tools.io.struct;

import io.github.repir.tools.lib.Log;

public interface StructuredFileRecord<F extends StructuredRecordFile> {
   public static Log log = new Log(StructuredFileRecord.class);

   public abstract void read( F file );
   
   public abstract void write( F file );
}
