package jaicore.basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(getResourceAsFile(path)))) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Reads the contents of a resource to a list of strings.
	 *
	 * @param path The path of the resource that shall be read.
	 * @return The contents of the resource parsed to a string.
	 * @throws IOException Throws an IOException if the file could not be read.
	 * @throws FileNotFoundException Thrown if the file could not be found.
	 */
	public static List<String> readResourceFileToStringList(final String path) throws IOException {
		List<String> list = new LinkedList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(getResourceAsFile(path)))) {
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		}
		return list;
	}

	/**
	 * Returns the file corresponding to the given path.
	 *
	 * @param path The path for which a resource shall be retrieved.
	 * @return The resource file corresponding to the given path.
	 */
	public static File getResourceAsFile(final String path) {
		return new File(ResourceUtil.class.getClassLoader().getResource(path).getFile());
	}

}
