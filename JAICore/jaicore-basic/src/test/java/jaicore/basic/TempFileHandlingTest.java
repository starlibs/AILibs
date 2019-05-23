package jaicore.basic;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TempFileHandlingTest {

	@Test
	public void testFileCreationAndDeletion() throws IOException {
		TempFileHandler h = new TempFileHandler();
		String uuid = h.createTempFile();
		File f = new File(System.getProperty("user.home") + File.separator + uuid);
		assertTrue(f.exists() && f.isFile());
		h.close();
		assertTrue(!f.exists());
	}
	
}
