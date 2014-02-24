package io.github.repir.tools.Content;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class RecordTagUnordered extends RecordTag {

   public Log log = new Log(RecordTagUnordered.class);

   public RecordTagUnordered(Datafile datafile) {
      super(datafile);
   }

   public RecordTagUnordered(byte[] bytes) {
      super(bytes);
   }

   public RecordTagUnordered(BytesOut bytes) {
      super(bytes);
   }

   @Override
   protected void startRead(Field f) throws EOFException {
   }

   @Override
   protected void endRead(Field f) throws EOFException {
   }
   ByteRegex startrecord = createStartRecordRegex(null);
   ByteRegex endrecord = createEndRecordRegex(null);
   ByteRegex endfield = createEndFieldRegex(null);

   @Override
   public boolean next() {
      //log.info("next() %s", this.toString());
      if (reader.hasMore()) {
         try {
            readStartRecord(startrecord);
            ArrayList<StructuredTagStream2.Field> fields = new ArrayList<StructuredTagStream2.Field>();
            for (StructuredTagStream2.Field first = (StructuredTagStream2.Field) start.next(); first != null; first = (StructuredTagStream2.Field) first.next) {
               fields.add(first);
            }
            try {
               while (fields.size() > 0 && this.peekStringNotExists(endrecord)) {
                  boolean remove = false;
                  Iterator<StructuredTagStream2.Field> iter = fields.iterator();
                  while (iter.hasNext()) {
                     StructuredTagStream2.Field f = iter.next();
                     if (this.peekStringExists(f.startfieldregex)) {
                        f.readNoReturn();
                        remove = true;
                        break;
                     }
                  }
                  if (remove) {
                     iter.remove();
                     //log.info("remove field size %d endreached %b", fields.size(), this.peekStringNotExists(endrecord));
                  } else {
                     //log.info("advancepos %d", reader.getOffset());
                     reader.setOffset(reader.getOffset() + 1);
                     this.readString(endfield);
                  }
               }
               readEndRecord(endrecord);
            } catch (EOFException ex) {
            }
            return true;
         } catch (Exception ex) {
            log.exception(ex, "next()");
         }
      }
      return false;
   }
}
