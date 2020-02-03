package com.strangegizmo.cdb;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CdbTest {
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    private File cdb;

    /**
     * Used to generate test data for hash function
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            if ("--string".equals(args[0])) {
                byte[] bytes = args[1].getBytes(StandardCharsets.UTF_8);
                System.out.println(Base64.getEncoder().encodeToString(bytes));
                System.out.println(Cdb.hash(bytes));
            } else if ("--hex".equals(args[0])) {
                ArrayList<Byte> bytes = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    String s = args[i];
                    if (s.startsWith("0x")) {
                        s = s.substring(2);
                    }
                    for (int j = 0; j < s.length(); j += 2) {
                        int x = Integer.parseInt(s.substring(j, j + 2), 16);
                        bytes.add((byte) (x & 0xFF));
                    }
                }
                byte[] tmp = new byte[bytes.size()];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = bytes.get(i);
                }
                String sBytes = bytes.stream().map(b -> Integer.toHexString(b & 0xFF)).map(String::toUpperCase).collect(Collectors.joining(" ", "[", "]"));
                System.out.println(sBytes + " size=" + sBytes.length());
                System.out.println(Base64.getEncoder().encodeToString(tmp));
                System.out.println(Cdb.hash(tmp));
            }
        }
    }

    @Before
    public void createCdbFile() throws IOException {
        cdb = temp.newFile("test.cdb");
        try (OutputStream out = new FileOutputStream(cdb)) {
            InputStream cdbStream = CdbTest.class.getResourceAsStream("/test.cdb");
            // copy
            IOUtils.copy(cdbStream, out);
            out.flush();
        }
    }

    @Test
    public void find() throws IOException {
        Cdb subject = new Cdb(cdb);
        ByteBuffer result = subject.find(ByteBuffer.wrap("one".getBytes(StandardCharsets.US_ASCII)));
        String strResult = StandardCharsets.US_ASCII.decode(result).toString();
        assertEquals("Hello", strResult);

        result = subject.find(ByteBuffer.wrap("two".getBytes()));
        strResult = StandardCharsets.US_ASCII.decode(result).toString();
        assertEquals("Goodbye", strResult);
    }

    @Test
    public void findNext() throws IOException {
        Cdb subject = new Cdb(cdb);
        final ByteBuffer key = ByteBuffer.wrap("one".getBytes(StandardCharsets.US_ASCII));
        ByteBuffer bytes = subject.findnext(key);
        assertNotNull(bytes);
        assertEquals("Hello", StandardCharsets.US_ASCII.decode(Objects.requireNonNull(bytes)).toString());

        bytes = subject.findnext(key);
        assertNull(bytes);
    }

    @Test
    public void testHashFunction() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(CdbTest.class.getResourceAsStream("/test-hashes.data")))) {
            Base64.Decoder decoder = Base64.getDecoder();
            String line = reader.readLine();
            while (line != null) {
                String[] split = line.split(",");
                byte[] key = decoder.decode(split[0]);
                long expected = Long.parseLong(split[1]);
                assertEquals(expected, Cdb.hash(key));
                line = reader.readLine();
            }
        }
    }
}