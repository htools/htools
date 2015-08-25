package io.github.htools.io.struct;

import io.github.htools.lib.Log;

public interface StructuredFileRecord<F extends StructuredRecordFile> {
   public static Log log = new Log(StructuredFileRecord.class);

   public abstract void read( F file );
   
   public abstract void write( F file );
}
