package com.strangegizmo.cdb;

import java.nio.ByteBuffer;

public class CdbHash {
    public static final long CDB_HASHSTART = 5381;
    public static final long BIT_MASK_32 = 0x00000000ffffffffL;

    public static final long hashAdd(long hash, long b) {
        hash += (hash << 5);
        hash = hash ^ b;
        return hash & BIT_MASK_32;
    }

    public static final long hash(ByteBuffer bytes) {
        long hash = CDB_HASHSTART;
        while(bytes.hasRemaining()) {
            byte b = bytes.get();
            hash = hashAdd(hash, b & 0xFF);
        }
        return hash;
    }
}
