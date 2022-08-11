/*
 * Copyright (c) 2000-2001, Michael Alyn Miller &lt;malyn@strangeGizmo.com&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice unmodified, this list of conditions, and the following
 *    disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Michael Alyn Miller nor the names of the
 *    contributors to this software may be used to endorse or promote
 *    products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package io.github.duckasteroid.cdb;

/* Java imports. */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CDB implements a Java interface to D.&nbsp;J.&nbsp;Bernstein's CDB
 * database.
 *
 * @author Michael Alyn Miller &lt;malyn@strangeGizmo.com&gt;
 * @version 1.0.3
 */
public class Cdb implements AutoCloseable {
    public static final int HASHTABLE_LENGTH = 2048;

    private final FileChannel fileChannel;
    /**
     * The slot pointers, cached here for efficiency as we do not have
     * mmap() to do it for us.  These entries are paired as (pos, len)
     * tuples.
     */
    private SlotEntry[] slotTable;
    /**
     * The RandomAccessFile for the CDB file.
     */
    private RandomAccessFile file;
    /**
     * The number of hash slots searched under this key.
     */
    private int loop = 0;
    /**
     * The hash value for the current key.
     */
    private long khash = 0;
    /**
     * The number of hash slots in the hash table for the current key.
     */
    private int hslots = 0;
    /**
     * The position of the hash table for the current key
     */
    private long hpos = 0;
    /**
     * The position of the current key in the slot.
     */
    private long kpos = 0;


    /**
     * Creates an instance of the CDB class and loads the given CDB
     * file.
     *
     * @param filepath The path to the CDB file to open.
     * @throws java.io.IOException if the CDB file could not be
     *                             opened.
     */
    public Cdb(String filepath) throws IOException {
        this(new File(filepath));
    }

    /**
     * Creates an instance of the CDB class and loads the given CDB
     * file.
     *
     * @param cdbFile The CDB file to open.
     * @throws java.io.IOException if the CDB file could not be
     *                             opened.
     */
    public Cdb(File cdbFile) throws IOException {
        /* Open the CDB file. */
        file = new RandomAccessFile(cdbFile, "r");
        fileChannel = file.getChannel();

        /* Read and parse the slot table.  We do not throw an exception
         * if this fails; the file might empty, which is not an error. */
        if (fileChannel.size() > 2048) {
            /* Read the table. */
            ByteBuffer slotTableBuffer = ByteBuffer.allocateDirect(2048).order(ByteOrder.LITTLE_ENDIAN);
            int read = fileChannel.read(slotTableBuffer);
            if (read < 2048) throw new IOException("Unable to read slot table");
            slotTableBuffer.flip();
            /* Create and parse the slot table. */
            slotTable = new SlotEntry[256];
            for (int i = 0; i < slotTable.length; i++) {
                slotTable[i] = new SlotEntry(slotTableBuffer);
            }
        }
    }

    /**
     * @deprecated Use {@link #hash(ByteBuffer)}
     * @param key the key to hash
     * @return the hash value
     */
    @Deprecated
    public static long hash(byte[] key) {
        return hash(ByteBuffer.wrap(key));
    }

    /**
     * Computes and returns the hash value for the given key.
     *
     * @param key The key to compute the hash value for.
     * @return The hash value of <code>key</code>.
     */
    public static long hash(ByteBuffer key) {
        return CdbHash.hash(key);
    }

    public static CdbElementEnumeration elements(final SeekableByteChannel input) throws IOException {
        ByteBuffer eodBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        int read = input.read(eodBuffer);
        eodBuffer.flip();
        /* Read the end-of-data value. */
        final int eod = eodBuffer.getInt();

        /* Skip the rest of the hashtable. */
        long skipped = HASHTABLE_LENGTH - 4;
        input.position(input.position() + skipped);

        /* Return the Enumeration. */
        return new CdbElementEnumeration(input, eod);
    }

    /**
     * Returns an Enumeration containing a CdbElement for each entry in
     * the constant database.
     *
     * @param filepath The CDB file to read.
     * @return An Enumeration containing a CdbElement for each entry in
     * the constant database.
     * @throws java.io.IOException if an error occurs reading the
     *                             constant database.
     */
    public static CdbElementEnumeration elements(final String filepath)
            throws IOException {
        return elements(Files.newByteChannel(Paths.get(filepath)));
    }

    /**
     * Closes the CDB database.
     */
    public final void close() throws IOException {
        /* Close the CDB file. */
        try {
            file.close();
        } finally {
            file = null;
        }
    }

    /**
     * Prepares the class to search for the given key
     */
    public synchronized final void findstart() {
        loop = 0;
    }

    /**
     * Finds the first record stored under the given key.
     *
     * @param key The key to search for.
     * @return The record store under the given key, or
     * <code>null</code> if no record with that key could be found.
     */
    public final synchronized ByteBuffer find(ByteBuffer key) {
        findstart();
        return findnext(key);
    }

    /**
     * Finds the next record stored under the given key.
     *
     * @param key The key to search for.
     * @return The next record store under the given key, or
     * <code>null</code> if no record with that key could be found.
     */
    public final synchronized ByteBuffer findnext(ByteBuffer key) {
        /* There are no keys if we could not read the slot table. */
        if (slotTable == null)
            return null;

        /* Locate the hash entry if we have not yet done so. */
        if (loop == 0) {
            /* Get the hash value for the key. */
            long u = CdbHash.hash(key);
            key.clear();
            /* Unpack the information for this record. */
            int index = (int)(u % 256);
            SlotEntry slot = slotTable[index];
            hslots = slot.length;
            if (hslots == 0)
                return null;
            hpos = slot.position;

            /* Store the hash value. */
            khash = u;

            /* Locate the slot containing this key. */
            u >>>= 8;
            u %= hslots;
            u <<= 3;
            kpos = hpos + u;
        }

        /* Search all of the hash slots for this key. */
        try {
            ByteBuffer local = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            while (loop < hslots) {
                /* Read the entry for this key from the hash slot. */
                fileChannel.position(kpos);
                fileChannel.read(local);
                local.flip();

                int h = local.getInt();
                int pos = local.getInt();
                if (pos == 0)
                    return null;

                /* Advance the loop count and key position.  Wrap the
                 * key position around to the beginning of the hash slot
                 * if we are at the end of the table. */
                loop += 1;

                kpos += 8;
                if (kpos == (hpos + (hslots << 3)))
                    kpos = hpos;

                /* Ignore this entry if the hash values do not match. */
                if (h != khash)
                    continue;

                /* Get the length of the key and data in this hash slot
                 * entry. */
                fileChannel.position(pos);
                local.clear();
                fileChannel.read(local);
                local.flip();

                int klen = local.getInt();
                if (klen != key.remaining())
                    continue;

                int dlen = local.getInt();

                /* Read the key stored in this entry and compare it to
                 * the key we were given. */
                ByteBuffer k = ByteBuffer.allocate(klen);
                fileChannel.read(k);
                k.flip();

                /* No match; check the next slot. */
                if (!key.equals(k))
                    continue;

                /* The keys match, return the data. */
                ByteBuffer d = ByteBuffer.allocate(dlen);
                fileChannel.read(d);
                d.flip();
                return d;
            }
        } catch (IOException ignored) {
            return null;
        }

        /* No more data values for this key. */
        return null;
    }

    static class SlotEntry {
        public final int position;
        public final int length;

        SlotEntry(ByteBuffer byteBuffer) {
            this.position = byteBuffer.getInt();
            this.length = byteBuffer.getInt();
        }
    }

}
