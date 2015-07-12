package io.github.repir.tools.hadoop.io.archivereader;

//package io.github.repir.EntityReader;
//
//import io.github.repir.Extractor.Entity;
//import io.github.repir.EntityReader.MapReduce.EntityWritable;
//import io.github.repir.tools.search.ByteSearch;
//import io.github.repir.tools.search.ByteSearchSection;
//import io.github.repir.tools.search.ByteSection;
//import io.github.repir.tools.Content.EOCException;
//import io.github.repir.tools.Content.HDFSIn;
//import io.github.repir.tools.lib.Log;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.mapreduce.lib.input.FileSplit;
//import io.github.repir.tools.lib.ByteTools;
//
///**
// * An implementation of EntityReader that scans the input for Wikipedia XML
// * dumps, that are enclosed in <page></page> tags.
// * <p/>
// * @author jeroen
// */
//public class EntityReaderWPEdits extends EntityReader {
//
//    public static Log log = new Log(EntityReaderWPEdits.class);
//    private byte[] startTag;
//    private byte[] endTag;
//    private ByteSection idTag = new ByteSection("<id>","</id>");
//    private ByteSection revisionTag = new ByteSection("<revision>","</revision>");
//    private ByteSection timestampTag = new ByteSection("<timestamp>","</timestamp>");
//    private ByteSection contributorTag = new ByteSection("<contributor>","</contributor>");
//    private ByteSection usernameTag = new ByteSection("<username>","</username>");
//    private ByteSection namespaceTag = new ByteSection("<ns>","</ns>");
//    private ByteSection textTag = new ByteSection("<text[^>]*>","</text>");
//    private ByteSearch redirect = ByteSearch.create("<redirect ");
//
//    @Override
//    public void initialize(FileSplit fileSplit) {
//        startTag = conf.get("entityreader.entitystart", "<page>").getBytes();
//        endTag = conf.get("entityreader.entityend", "</page>").getBytes();
//        Path file = fileSplit.getPath();
//    }
//
//    @Override
//    public boolean nextKeyValue() {
//        while (foundValidStartBeforeEndOfSplit()) {
//            try {
//                // mark the start of the entity (exclusive of the startTag and endTag)
//                key.set(fsin.getOffset());
//
//                readEntity();
//
//                int starttext = posPastBodyStart();
//                checkRedirectPage(starttext);
//                int endtext = posPastBodyEnd(starttext);
//                checkValidNamespace(starttext);
//
//                String id = getId(starttext);
//                String title = ByteTools.extract(entitywritable.entity.content, revisionStart, revisionEnd, 0, starttext, false, false);
//                //log.info("id %s title %s ns %s", id, title, ns); 
//                entitywritable.entity.addSectionPos("all", starttext + 1, starttext + 1, endtext, endtext);
//                entitywritable.entity.get("literaltitle").add(title);
//                entitywritable.entity.get("collectionid").add(id);
//                return true;
//            } catch (InvalidEntityException ex) {
//                // some reason was found to skip the page, like invalid namespace, redirect page
//            }
//        }
//        return false;
//    }
//
//    /**
//     * @return true if a startTag starts before end of split. Sets the offset
//     * past the record start label.
//     */
//    private boolean foundValidStartBeforeEndOfSplit() {
//        if (fsin.hasMore() && readUntilStart()) { // startTag is found before end of split
//            // redundant double check if offset is really before end of split
//            return fsin.getOffset() - startTag.length < fsin.getCeiling();
//        }
//        return false;
//    }
//
//    /**
//     * @return position past bodyStart and BodyStartEnd tags. Throws an
//     * EOCException when not found.
//     */
//    private int posPastBodyStart() throws InvalidEntityException {
//        int pos = ByteTools.find(entitywritable.entity.content, bodyStart, 0, entitywritable.entity.content.length, false, false);
//        if (pos >= 0) {
//            pos = ByteTools.find(entitywritable.entity.content, bodyStartEnd, pos + bodyStart.length, entitywritable.entity.content.length, false, false);
//            if (pos > 0) {
//                return pos;
//            }
//        }
//        throw invalidEntity;
//    }
//
//    private int posPastBodyEnd(int posAfterBodyStart) throws InvalidEntityException {
//        int endtext = ByteTools.find(entitywritable.entity.content, bodyEnd, posAfterBodyStart + bodyStartEnd.length, entitywritable.entity.content.length, false, false);
//        if (endtext >= posAfterBodyStart) {
//            return endtext;
//        }
//        throw invalidEntity;
//    }
//
//    private void checkRedirectPage(int posAfterBodyStart) throws InvalidEntityException {
//        int redirectpos = ByteTools.find(entitywritable.entity.content, redirect, 0, posAfterBodyStart, false, false);
//        if (redirectpos >= 0) {
//            throw invalidEntity;
//        }
//    }
//
//    // throws an EOCException
//    private void checkValidNamespace(int posAfterBodyStart) throws InvalidEntityException {
//        String ns = ByteTools.extract(entitywritable.entity.content, nsStart, nsEnd, 0, posAfterBodyStart, false, false);
//        if (!ns.trim().equals("0")) {
//            throw invalidEntity;
//        }
//    }
//    
//    private String getId(int posAfterBodyStart) {
//         return ByteTools.extract(entitywritable.entity.content, idStart, idEnd, 0, posAfterBodyStart, false, false);
//    }
//
//    private String getTitle(int posAfterBodyStart) {
//         return ByteTools.extract(entitywritable.entity.content, idStart, idEnd, 0, posAfterBodyStart, false, false);
//    }
//
//    /**
//     * Read an entity from the input, ignoring the end of split that may have
//     * been set to read the last record that crosses the boundary between
//     * splits.
//     *
//     * @return true if successful
//     */
//    private boolean readEntity() throws InvalidEntityException {
//        entitywritable = new EntityWritable();
//        entitywritable.entity = new Entity();
//        int needleposition = 0;
//        try {
//            while (true) {
//                int b = fsin.readByte();
//                if (b != endTag[needleposition]) { // check if we match needle
//                    if (needleposition > 0) {
//                        entitywritable.writeBytes(endTag, 0, needleposition);
//                        needleposition = 0;
//                    }
//                }
//                if (b == endTag[needleposition]) {
//                    needleposition++;
//                    if (needleposition >= endTag.length) {
//                        entitywritable.storeContent();
//                        //new ByteSearchSection(entitywritable.entity.content, 0, 0, entitywritable.entity.);
//                        return true;
//                    }
//                } else {
//                    entitywritable.writeByte(b);
//                }
//
//            }
//        } catch (EOCException ex) {
//            throw invalidEntity;
//        }
//    }
//
//    /**
//     * @return true if a startTag starts before end of split. Sets the offset
//     * past the record start label.
//     */
//    private boolean readUntilStart() {
//        int needleposition = 0;
//        while (true) {
//            try {
//                int b = fsin.readByte();
//                if (b != startTag[needleposition]) { // check if we match needle
//                    needleposition = 0;
//                }
//                if (b == startTag[needleposition]) {
//                    needleposition++;
//                    if (needleposition >= startTag.length) {
//                        return true;
//                    }
//                } else {
//                    if (needleposition == 0 && !fsin.hasMore()) {  // see if we've passed the stop point:
//                        return false;
//                    }
//                }
//            } catch (EOCException ex) {
//                return false;
//            }
//        }
//    }
//
//    static InvalidEntityException invalidEntity = new InvalidEntityException();
//
//    private static class InvalidEntityException extends Exception {
//    }
//}
