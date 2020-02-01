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

package com.strangegizmo.cdb;

/* Java imports. */

import java.io.*;
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
    static class SlotEntry {
        public final int position;
        public final int length;

        SlotEntry(int position, int length) {
            this.position = position;
            this.length = length;
        }

        SlotEntry(ByteBuffer byteBuffer) {
            this.position = byteBuffer.getInt();
            this.length = byteBuffer.getInt();
        }
    }

    public static final int HASHTABLE_LENGTH = 2048;
    public static final long BIT_MASK_32 = 0x00000000ffffffffL;

    /**
     * The RandomAccessFile for the CDB file.
     */
    private RandomAccessFile file = null;
    private final FileChannel fileChannel;

    /**
     * The slot pointers, cached here for efficiency as we do not have
     * mmap() to do it for us.  These entries are paired as (pos, len)
     * tuples.
     */
    private final SlotEntry[] slotTable = new SlotEntry[257];

    /**
     * The number of hash slots searched under this key.
     */
    private int loop = 0;

    /**
     * The hash value for the current key.
     */
    private int khash = 0;


    /**
     * The number of hash slots in the hash table for the current key.
     */
    private int hslots = 0;

    /**
     * The position of the hash table for the current key
     */
    private int hpos = 0;


    /**
     * The position of the current key in the slot.
     */
    private int kpos = 0;

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
            for (int i = 0; i < 256; i++) {
                slotTable[i] = new SlotEntry(slotTableBuffer);
            }
        }
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
     * Computes and returns the hash value for the given key.
     *
     * @param key The key to compute the hash value for.
     * @return The hash value of <code>key</code>.
     */
    static final int hash(byte[] key) {
		/* Initialize the hash value. */
        long h = 5381;

		/* Add each byte to the hash value. */
        for (int i = 0; i < key.length; i++) {
//			h = ((h << 5) + h) ^ key[i];
            long l = h << 5;
            h += (l & BIT_MASK_32);
            h = (h & BIT_MASK_32);

            int k = key[i];
            k = (k + 0x100) & 0xff;

            h = h ^ k;
        }

		/* Return the hash value. */
        return (int) (h & BIT_MASK_32);
    }


    /**
     * Prepares the class to search for the given key.
     *
     * @param key The key to search for.
     */
    public synchronized final void findstart(byte[] key) {
        loop = 0;
    }

    /**
     * Finds the first record stored under the given key.
     *
     * @param key The key to search for.
     * @return The record store under the given key, or
     * <code>null</code> if no record with that key could be found.
     */
    public final synchronized byte[] find(byte[] key) {
        findstart(key);
        return findnext(key);
    }

    /**
     * Finds the next record stored under the given key.
     *
     * @param key The key to search for.
     * @return The next record store under the given key, or
     * <code>null</code> if no record with that key could be found.
     */
    public final synchronized byte[] findnext(byte[] key) {
		/* There are no keys if we could not read the slot table. */
        if (slotTable == null)
            return null;

		/* Locate the hash entry if we have not yet done so. */
        if (loop == 0) {
			/* Get the hash value for the key. */
            int u = hash(key);

			/* Unpack the information for this record. */
            SlotEntry slot = slotTable[u % 256];
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
            while (loop < hslots) {
				/* Read the entry for this key from the hash slot. */
                file.seek(kpos);

                int h = file.readUnsignedByte()
                        | (file.readUnsignedByte() << 8)
                        | (file.readUnsignedByte() << 16)
                        | (file.readUnsignedByte() << 24);

                int pos = file.readUnsignedByte()
                        | (file.readUnsignedByte() << 8)
                        | (file.readUnsignedByte() << 16)
                        | (file.readUnsignedByte() << 24);
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
                file.seek(pos);

                int klen = file.readUnsignedByte()
                        | (file.readUnsignedByte() << 8)
                        | (file.readUnsignedByte() << 16)
                        | (file.readUnsignedByte() << 24);
                if (klen != key.length)
                    continue;

                int dlen = file.readUnsignedByte()
                        | (file.readUnsignedByte() << 8)
                        | (file.readUnsignedByte() << 16)
                        | (file.readUnsignedByte() << 24);

				/* Read the key stored in this entry and compare it to
				 * the key we were given. */
                boolean match = true;
                byte[] k = new byte[klen];
                file.readFully(k);
                for (int i = 0; i < k.length; i++) {
                    if (k[i] != key[i]) {
                        match = false;
                        break;
                    }
                }

				/* No match; check the next slot. */
                if (!match)
                    continue;

				/* The keys match, return the data. */
                byte[] d = new byte[dlen];
                file.readFully(d);
                return d;
            }
        } catch (IOException ignored) {
            return null;
        }

		/* No more data values for this key. */
        return null;
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

}
