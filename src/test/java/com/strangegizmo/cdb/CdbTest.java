package com.strangegizmo.cdb;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;

import static org.junit.Assert.*;

public class CdbTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File cdb;

    @Before
    public void createCdbFile() throws IOException {
        cdb = temp.newFile("test.cdb");
        try(OutputStream out = new FileOutputStream(cdb)) {
            InputStream cdbStream = DumpTest.class.getResourceAsStream("/test.cdb");
            // copy
            IOUtils.copy(cdbStream, out);
            out.flush();
        }
    }

    @Test
    public void find() throws IOException {
        Cdb subject = new Cdb(cdb);
        byte[] result = subject.find("one".getBytes());
        String strResult = new String(result);
        assertEquals("Hello", strResult);

        result = subject.find("two".getBytes());
        strResult = new String(result);
        assertEquals("Goodbye", strResult);
    }

    @Test
    public void findNext() throws IOException {
        Cdb subject = new Cdb(cdb);
        final byte[] key ="one".getBytes();
        byte[] bytes = subject.findnext(key);
        assertEquals("Hello", new String(bytes));

        bytes = subject.findnext(key);
        assertNull(bytes);
    }
}