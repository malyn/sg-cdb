package io.github.duckasteroid.cdb;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 *
 */
public class CdbMakeTest {
    /**
     * Input Data
     */
    public static final String TEST_MAKE = readFile();
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static String readFile() {
        try {
            return IOUtils.toString(CdbMakeTest.class.getResourceAsStream("/test.make"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return e.toString();
        }
    }

    @Test
    public void createTestFile() throws IOException {
        // output data
        File folder = temporaryFolder.getRoot();
        Path path = folder.toPath();
        Path output = path.resolve("output.cdb");
        Path temp = path.resolve("temp.cdb");

        // input data
        InputStream testData = new ByteArrayInputStream(TEST_MAKE.getBytes(StandardCharsets.UTF_8));
        CdbMake.make(testData, output.toString(), temp.toString());

        // check the data
        byte[] outputData = IOUtils.toByteArray(new FileInputStream(output.toFile()));
        assertNotNull(outputData);
        assertEquals(2114, outputData.length);

        byte[] expectedData = IOUtils.toByteArray(CdbMakeTest.class.getResourceAsStream("/test.cdb"));
        assertArrayEquals(expectedData, outputData);
    }
}
