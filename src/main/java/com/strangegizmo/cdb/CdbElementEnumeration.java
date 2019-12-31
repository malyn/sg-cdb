package com.strangegizmo.cdb;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class CdbElementEnumeration implements Enumeration<CdbElement>, Closeable {
    private final InputStream in;
    private final int eod;
    /** Current data pointer. */
    private int pos;

    public CdbElementEnumeration(InputStream in, int eod) {
        this.in = in;
        this.eod = eod;
        pos = 2048;
    }


    public void close() throws IOException {
        in.close();
    }

    /**
     * Returns <code>true</code> if there are more elements in
     * the constant database (pos < eod); <code>false</code>
     * otherwise.
     */
    public synchronized boolean hasMoreElements() {
        return pos < eod;
    }

    /**
     * Returns the next data element in the CdbRunner file.
     */
    public synchronized CdbElement nextElement() {
        try {
// Read the key and value lengths.
            int klen = readLeInt();
            pos += 4;
            int dlen = readLeInt();
            pos += 4;

// Read the key.
            byte[] key = new byte[klen];
            for (int off = 0; off < klen; /* below */) {
                int count = in.read(key, off, klen - off);
                if (count == -1)
                    throw new IllegalArgumentException(
                            "invalid cdb format");
                off += count;
            }
            pos += klen;

// Read the data.
            byte[] data = new byte[dlen];
            for (int off = 0; off < dlen; /* below */) {
                int count = in.read(data, off, dlen - off);
                if (count == -1)
                    throw new IllegalArgumentException(
                            "invalid cdb format");
                off += count;
            }
            pos += dlen;

// Return a CdbElement with the key and data.
            return new CdbElement(key, data);
        } catch (IOException ioException) {
            throw new IllegalArgumentException(
                    "invalid cdb format", ioException);
        }
    }


    /**
     * Reads a little-endian integer from <code>in</code>.
     */
    private int readLeInt() throws IOException {
        return (in.read() & 0xff)
                | ((in.read() & 0xff) << 8)
                | ((in.read() & 0xff) << 16)
                | ((in.read() & 0xff) << 24);
    }
}
