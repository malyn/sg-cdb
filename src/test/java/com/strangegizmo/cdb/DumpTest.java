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

    @Test
    public void testDump() throws IOException {
        Enumeration elements = Cdb.elements(DumpTest.class.getResourceAsStream("/test.cdb"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream, true, "US-ASCII");
        Dump.dump(elements, out);
        out.close();

        String result = new String(outputStream.toByteArray(), Charset.forName("US-ASCII"));
        assertEquals(CdbMakeTest.TEST_MAKE, result);
    }
}
