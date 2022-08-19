package io.github.duckasteroid.cdb;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


public class CdbElementEnumerationTest {
    @TempDir
    public static Path temporaryFolder;

    @Test
    public void testEnumeration() throws IOException {
        Path file = temporaryFolder.resolve("test.cdb");
        Files.copy(CdbElementEnumeration.class.getResourceAsStream("/test.cdb"), file);
        CdbElementEnumeration subject = Cdb.elements(Files.newByteChannel(file));
        assertNotNull(subject);

        assertTrue(subject.hasMoreElements());
        CdbElement element = subject.nextElement();
        assertNotNull(element);
        assertEquals("one", StandardCharsets.UTF_8.decode(element.getKey()).toString());
        assertEquals("Hello", StandardCharsets.UTF_8.decode(element.getData()).toString());

        assertTrue(subject.hasMoreElements());
        element = subject.nextElement();
        assertNotNull(element);
        assertEquals("two", StandardCharsets.UTF_8.decode(element.getKey()).toString());
        assertEquals("Goodbye", StandardCharsets.UTF_8.decode(element.getData()).toString());

        assertFalse(subject.hasMoreElements());

        subject.close();
    }
}
