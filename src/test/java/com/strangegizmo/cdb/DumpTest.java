package com.strangegizmo.cdb;

import cdb.Dump;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;

/**
 * tests if Dump produces our example Make file
 */
public class DumpTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testDump() throws IOException {
        Path tmp = temporaryFolder.getRoot().toPath().resolve("test.cdb");
        Files.copy(DumpTest.class.getResourceAsStream("/test.cdb"), tmp);
        Enumeration elements = Cdb.elements(Files.newByteChannel(tmp));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream, true, "US-ASCII");
        Dump.dump(elements, out);
        out.close();

        String result = new String(outputStream.toByteArray(), Charset.forName("US-ASCII"));
        assertEquals(CdbMakeTest.TEST_MAKE, result);
    }
}
