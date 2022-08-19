package io.github.duckasteroid.cdb;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CdbMakeTest {
    /**
     * Input Data
     */
    public static final String TEST_MAKE = readFile();
    @TempDir
    public static Path temporaryFolder;

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
        Path output = temporaryFolder.resolve("output.cdb");
        Path temp = temporaryFolder.resolve("temp.cdb");

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
