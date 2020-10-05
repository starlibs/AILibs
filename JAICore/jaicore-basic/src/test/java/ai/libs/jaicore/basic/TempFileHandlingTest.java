package ai.libs.jaicore.basic;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import ai.libs.jaicore.test.ShortTest;

public class TempFileHandlingTest {

	@ShortTest
	public void testFileCreationAndDeletion() throws IOException {
		TempFileHandler h = new TempFileHandler();
		String uuid = h.createTempFile().getName();
		File f = new File(System.getProperty("user.home") + File.separator + ".ailibs" + File.separator + uuid);
		assertTrue(f.exists() && f.isFile());
		h.close();
		assertTrue(!f.exists());
	}

}
