package io.github.htools.io.struct;

import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public abstract class StructuredFileIndex extends StructuredFile {

   public Log log = new Log(StructuredFileIndex.class);
   StructuredFileIndex secondindex;
   int hashcapacity;
   StructuredFile valuetuple;
   public LongField offset;
   public IntField length;
   public IntField bucketindex;
   ArrayList<IntKey> intkeyarray = new ArrayList<IntKey>();
   ArrayList<StringKey> stringkeyarray = new ArrayList<StringKey>();
   IntKey intkeys[];
   StringKey stringkeys[];

   public StructuredFileIndex(StructuredFile tuple) throws IOException {
      super(tuple.getDatafile());
      valuetuple = tuple;
      initKeys();
      intkeys = intkeyarray.toArray(new IntKey[intkeyarray.size()]);
      stringkeys = stringkeyarray.toArray(new StringKey[stringkeyarray.size()]);
      offset = this.addLong("offset");
      length = this.addInt("length");
      setIndexFile();
   }

   public StructuredFileIndex(StructuredFile tuple, StructuredFileIndex index) throws IOException {
      super(tuple.getDatafile());
      secondindex = index;
      valuetuple = tuple;
      initKeys();
      intkeys = intkeyarray.toArray(new IntKey[intkeyarray.size()]);
      stringkeys = stringkeyarray.toArray(new StringKey[stringkeyarray.size()]);
      offset = this.addLong("offset");
      length = this.addInt("length");
      setIndexFile();
   }

   public abstract String getFilename(String filename);

   public StructuredFile getValueTuple(IndexKey key) {
      StructuredFile t = null;
      try {
         Constructor<? extends StructuredFile> declaredConstructor = valuetuple.getClass().getDeclaredConstructor(Datafile.class);
         t = declaredConstructor.newInstance(valuetuple.getDatafile());
         t.setOffset(key.offset);
         t.setCeiling(key.offset + key.length);
         //log.info( "getValueType %d %d", t.getOffset(), t.getCeiling());
      } catch (Exception ex) {
         log.exception(ex, "getValueTupleKey( %s ) valuetuple %s", key, valuetuple);
      }
      return t;
   }

   public StructuredFile getValueTuple() {
      return valuetuple;
   }

   public void mergeIndexSegments() throws IOException {
      long offsets[] = valuetuple.mergeSegments();
      HDFSPath dir = (HDFSPath) getDatafile().getDir();
      ByteSearch filePattern = ByteSearch.create(getDatafile().getName());
      TreeSet<Datafile> sortedfiles = new TreeSet(dir.getFiles(filePattern));
      StructuredFileIndex in = this.clone();
      for (int i = 0; i < intkeys.length; i++) {
         intkeys[i].value = in.intkeys[i].key;
      }
      for (int i = 0; i < stringkeys.length; i++) {
         stringkeys[i].value = in.stringkeys[i].key;
      }
      int offsetpos = 0;
      this.openWrite();
      for (Datafile df : sortedfiles) {
         in.setDatafile(df);
         in.openRead();
         while (in.nextRecord()) {
            this.writeKeys();
            if (bucketindex != null) {
               bucketindex.write(in.bucketindex.value);
            }
            offset.write(in.offset.value + offsets[ offsetpos]);
            length.write(in.length.value);
            //log.info("%d %d", offset.value, length.value);
         }
         //log.info("%s %d", in.datafile.getFullPath(), offsets[offsetpos]);
         in.closeRead();
         offsetpos++;
      }
      this.closeWrite();
   }

   @Override
   public StructuredFileIndex clone() {
      StructuredFileIndex t = null;
      try {
         Constructor<? extends StructuredFileIndex> declaredConstructor = this.getClass().getDeclaredConstructor(StructuredFile.class);
         t = declaredConstructor.newInstance(valuetuple);
         t.openRead();
      } catch (Exception ex) {
         log.exception(ex, "clone() valuetuple %s", valuetuple);
      }
      return t;
   }

   public void extract() throws IOException {
      openWrite();
      valuetuple.openRead();
      while (valuetuple.hasNext()) {
         readTuple();
         writeTuple();
      }
      valuetuple.closeRead();
      closeWrite();
   }

   public void defineStructure() {
   }

   public abstract void initKeys();

   public void writeTuple() throws IOException {
      IndexKey k = new IndexKey();
      k.intkey = new int[intkeyarray.size()];
      k.stringkey = new String[stringkeyarray.size()];
      for (int i = 0; i < intkeyarray.size(); i++) {
         k.intkey[i] = intkeyarray.get(i).value.value;
      }
      for (int i = 0; i < stringkeyarray.size(); i++) {
         k.stringkey[i] = stringkeyarray.get(i).value.value;
      }
      k.offset = valuetuple.getOffsetTupleStart();
      k.length = (int) (valuetuple.getOffetTupleEnd() - valuetuple.getOffsetTupleStart());
      if (bucketindex != null) {
         k.bucketindex = getBucketIndex();
      }
      flushKey(k);
   }

   public void flushKey(IndexKey k) throws IOException {
      //log.info("flsuhKey %s %d", k.stringkey[0], this.getOffsetTupleStart());
      for (int i = 0; i < intkeyarray.size(); i++) {
         intkeyarray.get(i).key.write(k.intkey[i]);
      }
      for (int i = 0; i < stringkeyarray.size(); i++) {
         stringkeyarray.get(i).key.write(k.stringkey[i]);
      }
      if (bucketindex != null) {
         bucketindex.write(k.bucketindex);
      }
      offset.write(k.offset);
      length.write(k.length);
   }

   private void setIndexFile() throws IOException {
      StringBuilder sb = new StringBuilder();
      String filename = this.getFilename(valuetuple.getDatafile().getName());
      setDatafile(new Datafile(getDatafile().getFileSystem(), getDatafile().getDir().getFilename(filename)));
   }

   public IntField addKey(IntField element) {
      IntKey key = new IntKey();
      key.key = this.addInt("intkey" + intkeyarray.size());
      key.value = element;
      intkeyarray.add(key);
      return key.key;
   }

   public StringField addKey(StringField element) {
      StringKey key = new StringKey();
      key.key = this.addString("stringkey" + stringkeyarray.size());
      key.value = element;
      stringkeyarray.add(key);
      return key.key;
   }

   protected void readTuple() throws IOException {
      if (valuetuple.hasNext()) {
         if (!valuetuple.isAtStart()) {
            log.fatal("Have to make sure value is at start of tuple");
         } else {
            valuetuple.nextRecord();
            valuetuple.setTupleOffset();
         }
      }
   }

   public Iterator<StructuredFile> iterate(IndexKey key) {
      return new TupleIterator(this, key);
   }

   protected void writeKeys() throws IOException {
      //log.info("writeKeys %d %d", intkeyarray.size(), stringkeyarray.size());
      for (IntKey entry : intkeyarray) {
         entry.key.write(entry.value.value);
      }
      for (StringKey entry : stringkeyarray) {
         entry.key.write(entry.value.value);
      }
   }

   public void setCapacity(int tuples) {
      int target = (int) (tuples / 0.75);
      hashcapacity = 2;
      while (hashcapacity < target) {
         hashcapacity <<= 1;
      }
   }

   public void getBucketIndex(IndexKey key) {
      key.bucketindex = getBucketIndex(key.intkey, key.stringkey);
   }

   public int getBucketIndex(int intkeys[], String stringkeys[]) {
      int hash = 0;
      for (int i = 0; i < intkeys.length; i++) {
         hash += intkeys[i];
      }
      for (int i = 0; i < stringkeys.length; i++) {
         hash += stringkeys[i].hashCode();
      }
      return hash(hash) & (hashcapacity - 1);
   }

   public int getBucketIndex() {
      int hash = 0;
      for (IntKey i : intkeys) {
         hash += i.value.value;
      }
      for (StringKey s : stringkeys) {
         hash += s.value.value.hashCode();
      }
      return hash(hash) & (hashcapacity - 1);
   }

   static int hash(int h) {
      h ^= (h >>> 20) ^ (h >>> 12);
      return h ^ (h >>> 7) ^ (h >>> 4);
   }

   class IntKey {

      public IntField value;
      public IntField key;
   }

   class StringKey {

      public StringField value;
      public StringField key;
   }

   public static class IndexKey {

      public int intkey[] = new int[0];
      public String stringkey[] = new String[0];
      public long offset;
      public int length;
      public int bucketindex;

      public void storeInt(ArrayList<IntKey> keys) {
         intkey = new int[keys.size()];
         for (int i = 0; i < keys.size(); i++) {
            intkey[i] = keys.get(i).value.value;
         }
      }

      public void storeStr(ArrayList<StringKey> keys) {
         stringkey = new String[keys.size()];
         for (int i = 0; i < keys.size(); i++) {
            stringkey[i] = keys.get(i).value.value;
         }
      }
   }
   protected IndexKey searchkey;

   protected abstract boolean initSearchKey();

   protected abstract StructuredFile nextSearchKey();

   protected static class TupleIterator implements Iterator<StructuredFile> {

      StructuredFileIndex index;
      StructuredFile current, next;

      public TupleIterator(StructuredFileIndex index, IndexKey key) {
         this.index = index.clone();
         this.index.searchkey = key;
         if (this.index.initSearchKey()) {
            next();
         }
      }

      public boolean hasNext() {
         return next != null;
      }

      public StructuredFile next() {
         current = next;
         next = null;
         if (index != null) {
            next = index.nextSearchKey();
            if (next == null) {
               index.closeRead();
               index = null;
            }
         }
         return current;
      }

      public void remove() {
         throw new UnsupportedOperationException("Not supported yet.");
      }
   }
}
