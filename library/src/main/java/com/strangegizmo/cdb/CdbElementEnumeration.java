package com.strangegizmo.cdb;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;

public class CdbElementEnumeration implements Enumeration<CdbElement>, Closeable {
    private final ReadableByteChannel in;
    private final int eod;
    /**
     * A buffer for key/data length values
     */
    private final ByteBuffer lengthBuffer = ByteBuffer.allocateDirect(8).order(ByteOrder.LITTLE_ENDIAN);
    /**
     * Current data pointer.
     */
    private int pos;

    public CdbElementEnumeration(ReadableByteChannel in, int eod) {
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
            lengthBuffer.clear();
            pos += in.read(lengthBuffer);
            lengthBuffer.flip();
            int klen = lengthBuffer.getInt();
            int dlen = lengthBuffer.getInt();

            // Read the key.
            ByteBuffer keyBuffer = readBuffer(klen);

            // Read the data.
            ByteBuffer dataBuffer = readBuffer(dlen);

            // Return a CdbElement with the key and data.
            return new CdbElement(keyBuffer.asReadOnlyBuffer(), dataBuffer.asReadOnlyBuffer());
        } catch (IOException ioException) {
            throw new IllegalArgumentException(
                    "Invalid cdb format", ioException);
        }
    }

    private ByteBuffer readBuffer(int len) throws IOException {
        byte[] bytes = new byte[len];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int off = 0; off < len; /* below */) {
            int count = in.read(buffer);
            if (count == -1)
                throw new IllegalArgumentException(
                        "Invalid cdb format");
            off += count;
        }
        pos += len;
        return (ByteBuffer) buffer.flip();
    }


}
