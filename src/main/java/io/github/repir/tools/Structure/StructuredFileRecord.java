package io.github.repir.tools.Structure;

import io.github.repir.tools.Lib.Log;

public interface StructuredFileRecord<F extends StructuredRecordFile> {
   public static Log log = new Log(StructuredFileRecord.class);

   public abstract void read( F file );
}
