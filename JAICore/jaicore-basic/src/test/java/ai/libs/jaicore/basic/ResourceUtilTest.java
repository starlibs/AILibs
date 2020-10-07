package ai.libs.jaicore.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

/**
 * This test checks the basic functionalities provided by the ResourceUtil class which provides a convenient API for handling resource files.
 *
 * @author mwever
 */
class ResourceUtilTest {

	private static final String RESOURCE_FILE_PATH = "ai/libs/jaicore/basic/testrsc/dummy.resource";
	private static final String EXPECTED_CONTENT = "Lorem ipsum dolor sit amet.";

	/**
	 * Test to read the content of a resource file to string.
	 * @throws FileNotFoundException Thrown if the resource file could not be found.
	 * @throws IOException Thrown if the resource file could not be read.
	 */
	@Test
	void testReadResourceToString() throws IOException {
		String actualContent = ResourceUtil.readResourceFileToString(RESOURCE_FILE_PATH).trim();
		assertEquals("The content of the read-in resource does not match the expected content", EXPECTED_CONTENT, actualContent);
	}

	/**
	 * Test to get a resource as a file object.
	 */
	@Test
	void testGetResourceAsFile() {
		File resourceFile = ResourceUtil.getResourceAsFile(RESOURCE_FILE_PATH);
		assertTrue("The file returned for the resource path does not exist", resourceFile.exists());
		assertTrue("The file returned is not a file but a directory.", !resourceFile.isDirectory());
	}

	@Test
	void test() throws IOException {
		ResourceFile resFile = ResourceUtil.getResourceAsFile(RESOURCE_FILE_PATH);
		ResourceFile res2File = new ResourceFile(resFile.getParentFile(), "dummy2.resource");
		assertEquals(this.readRes(resFile.getPathName()), this.readRes(res2File.getPathName()));
	}

	private String readRes(final String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path)))) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		}
		return sb.toString();
	}

}
