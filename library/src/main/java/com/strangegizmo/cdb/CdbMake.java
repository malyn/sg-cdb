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
import java.util.LinkedList;
import java.util.List;

/**
 * CdbMake implements the database-creation side of
 * D.&nbsp;J.&nbsp;Bernstein's constant database package.
 *
 * @author Michael Alyn Miller &lt;malyn@strangeGizmo.com&gt;
 * @version 1.0.0
 */
public final class CdbMake {
    /**
     * The RandomAccessFile for the CdbRunner file.
     */
    private RandomAccessFile file = null;


    /**
     * The list of hash pointers in the file, in their order in the
     * constant database.
     */
    private List<CdbHashPointer> hashPointers = null;

    /**
     * The number of entries in each hash table.
     */
    private int[] tableCount = null;

    /**
     * The first entry in each table.
     */
    private int[] tableStart = null;


    /**
     * The position of the current key in the constant database.
     */
    private int pos = -1;


    /**
     * Constructs a CdbMake object and prepares it for the creation of a
     * constant database.
     */
    public CdbMake() {
    }

    /**
     * Builds a CdbRunner file from a CdbRunner-format text file.
     *
     * @param dataFilepath The CdbRunner data file to read.
     * @param cdbFilepath  The CdbRunner file to create.
     * @param tempFilepath The temporary file to use when creating the
     *                     CdbRunner file.
     * @throws java.io.IOException if an error occurs rebuilding the
     *                             CdbRunner file.
     */
    public static void make(String dataFilepath, String cdbFilepath,
                            String tempFilepath) throws IOException {
        make(dataFilepath, cdbFilepath, tempFilepath, null);
    }

    /**
     * Builds a CdbRunner file from a CdbRunner-format text file, excluding records
     * with data matching keys in `ignoreCdb'.
     *
     * @param dataFilepath The CdbRunner data file to read.
     * @param cdbFilepath  The CdbRunner file to create.
     * @param tempFilepath The temporary file to use when creating the
     *                     CdbRunner file.
     * @param ignoreCdb    If the data for an entry matches a key in this
     *                     CdbRunner file, the entry will not be added to the new file.
     * @throws java.io.IOException if an error occurs rebuilding the
     *                             CdbRunner file.
     */
    public static void make(String dataFilepath, String cdbFilepath,
                            String tempFilepath, Cdb ignoreCdb) throws IOException {
        make(dataFilepath, new File(cdbFilepath), new File(tempFilepath), ignoreCdb);
    }

    /**
     * Builds a CdbRunner file from a CdbRunner-format text file, excluding records
     * with data matching keys in `ignoreCdb'.
     *
     * @param dataFilepath The CdbRunner data file to read.
     * @param cdbFilepath  The CdbRunner file to create.
     * @param tempFilepath The temporary file to use when creating the
     *                     CdbRunner file.
     * @param ignoreCdb    If the data for an entry matches a key in this
     *                     CdbRunner file, the entry will not be added to the new file.
     * @throws java.io.IOException if an error occurs rebuilding the
     *                             CdbRunner file.
     */
    public static void make(String dataFilepath, File cdbFilepath,
                            File tempFilepath, Cdb ignoreCdb) throws IOException {
        /* Open the data file. */
        BufferedInputStream in
                = new BufferedInputStream(
                new FileInputStream(dataFilepath));

        /* Build the database. */
        make(in, cdbFilepath, tempFilepath, ignoreCdb);

        /* Close the data file. */
        in.close();
    }

    /**
     * Builds a CdbRunner file from a CdbRunner-format InputStream.
     *
     * @param in           The InputStream to read.
     * @param cdbFilepath  The CdbRunner file to create.
     * @param tempFilepath The temporary file to use when creating the
     *                     CdbRunner file.
     * @throws java.io.IOException if an error occurs rebuilding the
     *                             CdbRunner file.
     */
    public static void make(InputStream in, String cdbFilepath,
                            String tempFilepath) throws IOException {
        make(in, cdbFilepath, tempFilepath, null);
    }

    /**
     * Builds a CdbRunner file from a CdbRunner-format InputStream, excluding
     * records with data matching keys in `ignoreCdb'.
     *
     * @param in           The InputStream to read.
     * @param cdbFilepath  The CdbRunner file to create.
     * @param tempFilepath The temporary file to use when creating the
     *                     CdbRunner file.
     * @param ignoreCdb    If the data for an entry matches a key in this
     *                     CdbRunner file, the entry will not be added to the new file.
     * @throws java.io.IOException if an error occurs rebuilding the
     *                             CdbRunner file.
     */
    public static void make(InputStream in, String cdbFilepath,
                            String tempFilepath, Cdb ignoreCdb) throws IOException {
        make(in, new File(cdbFilepath), new File(tempFilepath), ignoreCdb);
    }

    /**
     * Builds a CdbRunner file from a CdbRunner-format InputStream, excluding
     * records with data matching keys in `ignoreCdb'.
     *
     * @param in           The InputStream to read.
     * @param cdbFilepath  The CdbRunner file to create.
     * @param tempFilepath The temporary file to use when creating the
     *                     CdbRunner file.
     * @param ignoreCdb    If the data for an entry matches a key in this
     *                     CdbRunner file, the entry will not be added to the new file.
     * @throws java.io.IOException if an error occurs rebuilding the
     *                             CdbRunner file.
     */
    public static void make(InputStream in, File cdbFilepath,
                            File tempFilepath, Cdb ignoreCdb) throws IOException {
        /* Create the CdbMake object. */
        CdbMake cdbMake = new CdbMake();

        /* Create the CdbRunner file. */
        cdbMake.start(tempFilepath);

        /* Process the data file. */
        int ch;
        for (; ; ) {
            /* Read and process a byte. */
            ch = in.read();
            if (ch == -1)
                break;
            if (ch == '\n')
                break;
            if (ch != '+')
                throw new IllegalArgumentException(
                        "input file not in correct format");

            /* Get the key length. */
            int klen = 0;
            for (; ; ) {
                ch = in.read();
                if (ch == ',')
                    break;
                if ((ch < '0') || (ch > '9'))
                    throw new IllegalArgumentException(
                            "input file not in correct format");
                if (klen > 429496720)
                    throw new IllegalArgumentException(
                            "key length is too big");
                klen = klen * 10 + (ch - '0');
            }

            /* Get the data length. */
            int dlen = 0;
            for (; ; ) {
                ch = in.read();
                if (ch == ':')
                    break;
                if ((ch < '0') || (ch > '9'))
                    throw new IllegalArgumentException(
                            "input file not in correct format");
                if (dlen > 429496720)
                    throw new IllegalArgumentException(
                            "data length is too big");
                dlen = dlen * 10 + (ch - '0');
            }

            /* Read in the key. */
            byte[] key = new byte[klen];
            for (int i = 0; i < klen; i++) {
                /* Read the character. */
                ch = in.read();
                if (ch == -1)
                    throw new IllegalArgumentException(
                            "input file is truncated");

                /* Store the character. */
                key[i] = (byte) (ch & 0xff);
            }

            /* Read key/data separator characters. */
            ch = in.read();
            if (ch != '-')
                throw new IllegalArgumentException(
                        "input file not in correct format");

            ch = in.read();
            if (ch != '>')
                throw new IllegalArgumentException(
                        "input file not in correct format");

            /* Read in the data. */
            byte[] data = new byte[dlen];
            for (int i = 0; i < dlen; i++) {
                /* Read the character. */
                ch = in.read();
                if (ch == -1)
                    throw new IllegalArgumentException(
                            "input file is truncated");

                /* Store the character. */
                data[i] = (byte) (ch & 0xff);
            }

            /* Add the key/data pair to the database if it is not in
             * ignoreCdb. */
            if ((ignoreCdb == null) || (ignoreCdb.find(ByteBuffer.wrap(data)) == null))
                cdbMake.add(key, data);

            /* Read the terminating LF. */
            ch = in.read();
            if (ch != '\n')
                throw new IllegalArgumentException(
                        "input file not in correct format");
        }

        /* Finish the CdbRunner file. */
        cdbMake.finish();

        /* Rename the data file. */
        boolean rename = tempFilepath.renameTo(cdbFilepath);
        if (!rename) {
            throw new IOException("Unable to rename to " + cdbFilepath.getAbsolutePath());
        }
    }

    /**
     * Begins the constant database creation process.
     *
     * @param filepath The path to the constant database to create.
     * @throws java.io.IOException If an error occurs creating the
     *                             constant database file.
     */
    public void start(File filepath) throws IOException {
        hashPointers = new LinkedList<>();
        tableCount = new int[256];
        tableStart = new int[256];

        /* Clear the table counts. */
        for (int i = 0; i < 256; i++)
            tableCount[i] = 0;

        /* Open the temporary CDB file. */
        file = new RandomAccessFile(filepath, "rw");

        /* Seek to the end of the header. */
        pos = 2048;
        file.seek(pos);
    }

    /**
     * Adds a key to the constant database.
     *
     * @param key  The key to add to the database.
     * @param data The data associated with this key.
     * @throws java.io.IOException If an error occurs adding the key
     *                             to the database.
     */
    public void add(byte[] key, byte[] data) throws IOException {
        // Write out the key length.
        writeLeInt(key.length);

        /* Write out the data length. */
        writeLeInt(data.length);

        /* Write out the key. */
        file.write(key);

        /* Write out the data. */
        file.write(data);


        /* Add the hash pointer to our list. */
        long hash = Cdb.hash(key);
        hashPointers.add(new CdbHashPointer(hash, pos));

        /* Add this item to the count. */
        tableCount[(int)(hash & 0xff)]++;


        /* Update the file position pointer. */
        posplus(8);
        posplus(key.length);
        posplus(data.length);
    }

    /**
     * Finalizes the constant database.
     *
     * @throws java.io.IOException If an error occurs closing out the
     *                             database.
     */
    public void finish() throws IOException {
        /* Find the start of each hash table. */
        int curEntry = 0;
        for (int i = 0; i < 256; i++) {
            curEntry += tableCount[i];
            tableStart[i] = curEntry;
        }

        /* Create a new hash pointer list in order by hash table. */
        CdbHashPointer[] slotPointers
                = new CdbHashPointer[hashPointers.size()];
        for (CdbHashPointer hp : hashPointers) {
            slotPointers[--tableStart[(int)(hp.hash & 0xff)]] = hp;
        }

        /* Write out each of the hash tables, building the slot table in
         * the process. */
        byte[] slotTable = new byte[2048];
        for (int i = 0; i < 256; i++) {
            /* Get the length of the hashtable. */
            int len = tableCount[i] * 2;

            /* Store the position of this table in the slot table. */
            slotTable[(i * 8)] = (byte) (pos & 0xff);
            slotTable[(i * 8) + 1] = (byte) ((pos >>> 8) & 0xff);
            slotTable[(i * 8) + 2] = (byte) ((pos >>> 16) & 0xff);
            slotTable[(i * 8) + 3] = (byte) ((pos >>> 24) & 0xff);
            slotTable[(i * 8) + 4] = (byte) (len & 0xff);
            slotTable[(i * 8) + 4 + 1] = (byte) ((len >>> 8) & 0xff);
            slotTable[(i * 8) + 4 + 2] = (byte) ((len >>> 16) & 0xff);
            slotTable[(i * 8) + 4 + 3] = (byte) ((len >>> 24) & 0xff);

            /* Build the hash table. */
            int curSlotPointer = tableStart[i];
            CdbHashPointer[] hashTable = new CdbHashPointer[len];
            for (int u = 0; u < tableCount[i]; u++) {
                /* Get the hash pointer. */
                CdbHashPointer hp = slotPointers[curSlotPointer++];

                /* Locate a free space in the hash table. */
                int where = (int)((hp.hash >>> 8) % len);
                while (hashTable[where] != null)
                    if (++where == len)
                        where = 0;

                /* Store the hash pointer. */
                hashTable[where] = hp;
            }

            /* Write out the hash table. */
            for (int u = 0; u < len; u++) {
                CdbHashPointer hp = hashTable[u];
                if (hp != null) {
                    writeLeInt((int)(hashTable[u].hash));
                    writeLeInt(hashTable[u].pos);
                } else {
                    writeLeInt(0);
                    writeLeInt(0);
                }
                posplus(8);
            }
        }

        /* Seek back to the beginning of the file and write out the
         * slot table. */
        file.seek(0);
        file.write(slotTable);

        /* Close the file. */
        file.close();
    }

    /**
     * Writes an integer in little-endian format to the constant
     * database at the current file offset.
     *
     * @param v The integer to write to the file.
     */
    private void writeLeInt(int v) throws IOException {
        file.writeByte((byte) (v & 0xff));
        file.writeByte((byte) ((v >>> 8) & 0xff));
        file.writeByte((byte) ((v >>> 16) & 0xff));
        file.writeByte((byte) ((v >>> 24) & 0xff));
    }

    /**
     * Advances the file pointer by <code>count</code> bytes, throwing
     * an exception if doing so would cause the file to grow beyond
     * 4 GB.
     *
     * @param count The count of bytes to increase the file pointer by.
     * @throws java.io.IOException If increasing the file pointer by
     *                             <code>count</code> bytes would cause the file to grow beyond
     *                             4 GB.
     */
    private void posplus(int count) throws IOException {
        int newpos = pos + count;
        if (newpos < count)
            throw new IOException("CdbRunner file is too big.");
        pos = newpos;
    }
}


class CdbHashPointer {
    /**
     * The hash value of this entry.
     */
    final long hash;

    /**
     * The position in the constant database of this entry.
     */
    final int pos;


    /**
     * Creates a new CdbHashPointer and initializes it with the given
     * hash value and position.
     *
     * @param hash The hash value for this hash pointer.
     * @param pos  The position of this entry in the constant database.
     */
    CdbHashPointer(long hash, int pos) {
        this.hash = hash;
        this.pos = pos;
    }
}
