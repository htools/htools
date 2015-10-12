package io.github.htools.io.compressed;

import java.io.IOException;

/**
 *
 * @author Jeroen
 */
public class ArchiveEntry<E> implements java.util.Map.Entry<E, ArchiveFile> {
    ArchiveFile<E> cafile;
    public E entry;

    ArchiveEntry(ArchiveFile<E> cafile) {
        this.cafile = cafile;
    }

    public String getName() throws IOException {
        return cafile.getName(entry);
    }

    public long getLastModified() throws IOException {
        return cafile.getLastModified(entry);
    }

    public boolean isDirectory() throws IOException {
        return cafile.isDirectory(entry);
    }

    public long size() {
        if (entry != null) {
            try {
                return cafile.getSize(entry);
            } catch (IOException ex) {
            }
        }
        return -1;
    }

    @Override
    public E getKey() {
        return entry;
    }

    @Override
    public ArchiveFile getValue() {
        return cafile;
    }

    public int readByte() throws IOException {
        return cafile.read();
    }

    public byte[] readAll() throws IOException {
        byte[] content = new byte[(int) size()];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (cafile.read() & 0xff);
        }
        return content;
    }

    @Override
    public ArchiveFile setValue(ArchiveFile file) {
        ArchiveFile old = this.cafile;
        this.cafile = file;
        return old;
    }

}
