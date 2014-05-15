package io.github.repir.tools.Content;

/**
 * Internal helper class for SructuredFileSortCollision.
 * @author jer
 */

class StructuredFileCollisionInternal extends StructuredFile {

   private int currenthash;
   private byte currenttable[];
   private BufferReaderWriter residenttable;
   public int lastnojumphash;
   public int jumptable = 9;
   public int lastoffset;
   private IntField offset = this.addInt("offset");
   private FixedMemField suboffset = this.addFixedMem("suboffset", jumptable);

   public StructuredFileCollisionInternal(Datafile df) {
      super(df);
   }

   @Override
   public void openRead() {
      super.openRead();
      residenttable = new BufferReaderWriter(datafile.readFully());
      super.closeRead();
   }

   @Override
   public void openWrite() {
      super.openWrite();
      currenthash = -1;
      lastoffset = 0;
      lastnojumphash = -jumptable - 1;
      currenttable = new byte[jumptable];
   }

   public void closeWrite(int hashcapacity) {
      while (currenthash < hashcapacity - 1) {
         writeHash(currenthash + 1, lastoffset, lastoffset);
      }
      while (currenthash - lastnojumphash < jumptable) {
         writeJump(lastoffset);
      }
      super.closeWrite();
   }

   public void writeHash(int hash, int oldoffset, int newoffset) {
      //log.info("hash %d offset %d currenthash %d ", hash, oldoffset, currenthash);
      while (currenthash < hash) {
         writeHash(oldoffset);
      }
      perhapsJump(oldoffset, newoffset);
   }

   private void writeHash(int oldoffset) {
      if (currenthash - lastnojumphash == jumptable) {
         writeMarker(oldoffset);
      } else {
         writeJump(oldoffset);
      }
   }

   private void writeMarker(int offset) {
      //log.info("Marker %d %d %d hashfile %d offset %d", currenthash, lastnojumphash, jumptable, this.getOffset(), offset);
      this.offset.write(offset);
      lastoffset = offset;
      lastnojumphash = ++currenthash;
   }

   private void writeJump(int oldoffset) {
      int jumpindex = getJumpIndex(++currenthash);
      currenttable[ jumpindex - 1] = (byte) (oldoffset - lastoffset);
      lastoffset = oldoffset;
      if (jumpindex == jumptable) {
         this.suboffset.write(currenttable);
      }
   }

   private void perhapsJump(int oldoffset, int newoffset) {
      if (newoffset - oldoffset > 255) {
         log.fatal("Record size cannot exceed 255");
      }
      if (newoffset - lastoffset > 255) {
         writeHash(oldoffset);
      }
   }

   public int getJumpIndex(int bucketindex) {
      return bucketindex % (jumptable + 1);
   }

   public int getMarkerOffset(int bucketindex) {
      int markers = bucketindex / (jumptable + 1);
      return markers * (4 + jumptable);
   }

   public long getOffset(int bucketindex) {
      long offset = -1;
      try {
         int marker = getMarkerOffset(bucketindex);
         int jumpindex = getJumpIndex(bucketindex);
         residenttable.setOffset(marker);
         //log.info("marker %d", marker);
         offset = residenttable.readInt();
         for (int i = 0; i < jumpindex; i++) {
            offset += residenttable.readByte();
         }
         //log.info("getOffset() bucketindex %d marker %d jumpindex %d offset %d", bucketindex, marker, jumpindex, offset);
      } catch (EOCException ex) {
         log.fatalexception(ex, "getoffset( %d ) residenttable %s", bucketindex, residenttable);
      }
      return offset;
   }
}
