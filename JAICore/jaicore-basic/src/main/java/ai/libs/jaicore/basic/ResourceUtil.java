package ai.libs.jaicore.basic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ai.libs.jaicore.basic.sets.SetUtil;

/**
 * Utils for handling resource access in a more convenient way.
 *
 * @author mwever
 */
public class ResourceUtil {

	private ResourceUtil() {
		/* Intentionally left blank; simply prevent this util class to be instantiated. */
	}

	/**
	 * Reads the contents of a resource to a string.
	 *
	 * @param path The path of the resource that shall be read.
	 * @return The contents of the resource parsed to a string.
	 * @throws IOException Throws an IOException if the file could not be read.
	 */
	public static String readResourceFileToString(final String path) throws IOException {
		return SetUtil.implode(readResourceFileToStringList(path), "\n");
	}

	/**
	 * Reads the contents of a resource to a list of strings.
	 *
	 * @param path The path of the resource that shall be read.
	 * @return The contents of the resource parsed to a string.
	 * @throws IOException Throws an IOException if the file could not be read.
	 */
	public static List<String> readResourceFileToStringList(final String path) throws IOException {
		List<String> list = new LinkedList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(ResourceUtil.class.getClassLoader().getResourceAsStream(path)))) {
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		}
		return list;
	}

	/**
	 * Reads the contents of a resource to a list of strings.
	 *
	 * @param resourceFile The resource file to read.
	 * @return The contents of the resource parsed to a string.
	 * @throws IOException Throws an IOException if the file could not be read.
	 */
	public static List<String> readResourceFileToStringList(final ResourceFile resourceFile) throws IOException {
		return readResourceFileToStringList(resourceFile.getPathName());
	}

	/**
	 * Returns the file corresponding to the given path.
	 *
	 * @param path The path for which a resource shall be retrieved.
	 * @return The resource file corresponding to the given path.
	 * @throws IOException
	 */
	public static ResourceFile getResourceAsFile(final String path) {
		return new ResourceFile(path);
	}

	/**
	 * Returns the file corresponding to the given path.
	 *
	 * @param path The path for which a resource shall be retrieved.
	 * @return The resource file corresponding to the given path.
	 * @throws IOException
	 */
	public static URL getResourceAsURL(final String path) {
		return ResourceUtil.class.getClassLoader().getResource(path);
	}

	/**
	 * Creates a temporary file from the resource to load.
	 * @param resourcePath The path to the resource.
	 * @return The canonical path to the temporary file reflecting the contents of the resource.
	 */
	public static String getResourceAsTempFile(final String resourcePath) {
		try {
			File tempFile = File.createTempFile("ai.libs-", ".res");
			tempFile.deleteOnExit();
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
				bw.write(ResourceUtil.readResourceFileToString(resourcePath));
			}
			return tempFile.getCanonicalPath();
		} catch (IOException e) {
			throw new LoadResourceAsFileFailedException("Could not load resource as a temporary file", e);
		}
	}

}
