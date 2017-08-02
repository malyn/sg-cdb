package com.strangegizmo.cdb;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 *
 */
public class CdbMakeTest {
    /** Input Data */
    public static final String TEST_MAKE = "+3,5:one->Hello" + "\n" +
            "+3,7:two->Goodbye" +"\n\n";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void createTestFile() throws IOException {
        // output data
        File folder = temporaryFolder.getRoot();
        Path path = folder.toPath();
        Path output = path.resolve("output.cdb");
        Path temp = path.resolve("temp.cdb");

        // input data
        InputStream testData = new ByteArrayInputStream(TEST_MAKE.getBytes("UTF-8"));
        CdbMake.make(testData, output.toString(), temp.toString());

        // check the data
        byte[] outputData = IOUtils.toByteArray(new FileInputStream(output.toFile()));
        assertNotNull(outputData);
        assertEquals(2114, outputData.length);

        byte[] expectedData = IOUtils.toByteArray(CdbMakeTest.class.getResourceAsStream("/test.cdb"));
        assertArrayEquals(expectedData, outputData);
    }
}