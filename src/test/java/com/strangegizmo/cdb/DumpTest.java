package com.strangegizmo.cdb;

import cdb.Dump;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;

/**
 * tests if Dump produces our example Make file
 */
public class DumpTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File cdb;

    @Before
    public void createCdbFile() throws IOException {
        cdb = temp.newFile("test.cdb");
        OutputStream out = new FileOutputStream(cdb);
        InputStream cdbStream = DumpTest.class.getResourceAsStream("/test.cdb");
        // copy
        IOUtils.copy(cdbStream, out);
        out.flush();
        out.close();
    }

    @Test
    public void testDump() throws IOException {
        Enumeration elements = Cdb.elements(cdb.getAbsolutePath());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream, true, "US-ASCII");
        Dump.dump(elements, out);
        out.close();

        String result = new String(outputStream.toByteArray(), Charset.forName("US-ASCII"));
        assertEquals(CdbMakeTest.TEST_MAKE, result);
    }
}
