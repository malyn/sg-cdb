package com.strangegizmo.cdb;

import cdb.Dump;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * tests if Dump produces our example Make file
 */
public class DumpTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testDump() throws IOException {
        Path tmp = temporaryFolder.getRoot().toPath().resolve("test.cdb");
        Files.copy(DumpTest.class.getResourceAsStream("/test.cdb"), tmp);
        CdbElementEnumeration elements = Cdb.elements(Files.newByteChannel(tmp));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream, true, "US-ASCII");
        Dump.dump(elements, out);
        out.close();

        String result = new String(outputStream.toByteArray(), StandardCharsets.US_ASCII);
        String TEST_MAKE = IOUtils.toString(DumpTest.class.getResourceAsStream("/test.make"), StandardCharsets.UTF_8);
        Assert.assertEquals(TEST_MAKE, result);
    }
}
