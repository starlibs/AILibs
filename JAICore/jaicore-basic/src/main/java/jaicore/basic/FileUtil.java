package jaicore.basic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for handling file I/O.
 *
 * @author fmohr, mwever
 *
 */
public class FileUtil {

	/* Logging. */
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	private FileUtil() {
		/* Intentionally left blank to prevent instantiation of this class. */
	}

	/**
	 * Reads the content of the given file as a list of strings.
	 *
	 * @param file The file to be read.
	 * @return The list of strings representing the content of the file.
	 * @throws IOException Thrown, if there are issues reading the file.
	 */
	public static List<String> readFileAsList(final File file) throws IOException {
		return readFileAsList(file.getAbsolutePath());
	}

	/**
	 * Reads the content of the given file as a list of strings.
	 *
	 * @param filename The path to the file to be read.
	 * @return The list of strings representing the content of the file.
	 * @throws IOException Thrown, if there are issues reading the file.
	 */
	public static List<String> readFileAsList(final String filename) throws IOException {
		try (BufferedReader r = Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
			String line;
			final List<String> lines = new LinkedList<>();
			while ((line = r.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		}
	}

	/**
	 * Reads the content of the given file into a single string.
	 *
	 * @param file The file to be read.
	 * @return The String representing the content of the file.
	 * @throws IOException Thrown, if there are issues reading the file.
	 */
	public static String readFileAsString(final File file) throws IOException {
		return readFileAsString(file.getAbsolutePath());
	}

	/**
	 * Reads the content of the given file into a single string.
	 *
	 * @param filename The path to the file to be read.
	 * @return The String representing the content of the file.
	 * @throws IOException Thrown, if there are issues reading the file.
	 */
	public static String readFileAsString(final String filename) throws IOException {
		final StringBuilder sb = new StringBuilder();
		try (BufferedReader r = Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
			String line;
			while ((line = r.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Reads the content of the given file as a matrix of string which are separated by the given separation string.
	 *
	 * @param filename The path to the file to be read.
	 * @param separation The string separating the matrix entries of a row.
	 * @return The matrix of strings.
	 * @throws IOException Thrown, if there are issues reading the file.
	 */
	public static List<List<String>> readFileAsMatrix(final String filename, final String separation) throws IOException {
		final List<String> content = readFileAsList(filename);
		final List<List<String>> matrix = new ArrayList<>(content.size());
		for (final String line : content) {
			final String[] lineAsArray = line.split(separation);
			final List<String> lineAsList = new ArrayList<>(lineAsArray.length);
			for (final String field : lineAsArray) {
				lineAsList.add(field.trim());
			}
			matrix.add(lineAsList);
		}
		return matrix;
	}

	/**
	 * Reads the given file into a Properties object.
	 *
	 * @param propertiesFile The file to be read.
	 * @return The properties object loaded from the specified file.
	 * @throws IOException Thrown, if there are issues reading the file.
	 */
	public static Properties readPropertiesFile(final File propertiesFile) throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(propertiesFile));
		return props;
	}

	/**
	 * Archives a collection of files specified by their paths into a zip-file.
	 *
	 * @param files The files to be archived in a zip file.
	 * @param archive The path to output the zipped files.
	 * @throws IOException Thrown, if an issue occurs while creating zip entries or writing the zip file to disk.
	 */
	public static void zipFiles(final Collection<String> files, final String archive) throws IOException {
		try (final FileOutputStream fos = new FileOutputStream(archive); final ZipOutputStream zos = new ZipOutputStream(fos)) {
			final int total = files.size();
			int i = 0;
			for (final String fileName : files) {
				final File file = new File(fileName);
				try (final FileInputStream fis = new FileInputStream(file)) {
					final ZipEntry zipEntry = new ZipEntry(file.getName());
					zos.putNextEntry(zipEntry);

					final byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						zos.write(bytes, 0, length);
					}

					zos.closeEntry();
				}
				i++;
				logger.debug("{} / {} ready.", i, total);
			}
		}
	}

	/**
	 * This method helps to serialize class objects as files.
	 *
	 * @param object The object to be serialized.
	 * @param pathname The path where to store the serialized object.
	 * @throws IOException Thrown if the object cannot be serialized or the serialized object cannot be written to disk.
	 */
	public static synchronized void serializeObject(final Object object, final String pathname) throws IOException {
		File file = new File(pathname);
		if (file.getParentFile() != null && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try (ObjectOutputStream os2 = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath())))) {
			os2.writeObject(object);
		} catch (NotSerializableException e) {
			Files.delete(file.toPath());
			throw e;
		}
	}

	/**
	 * This method can be used to unserialize an object from disk (located at the specified path) and restore the original object.
	 *
	 * @param pathname The path from where to unserialize the object.
	 * @return The unserialized object.
	 * @throws IOException Thrown, if the binary file cannot be read.
	 * @throws ClassNotFoundException Thrown, if the class of the object which is unserialized cannot be found on the classpath.
	 */
	public static Object unserializeObject(final String pathname) throws IOException, ClassNotFoundException {
		try (ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(pathname)))) {
			return is.readObject();
		}
	}

	/**
	 * Creates an empty file for the given path if it does not already exist.
	 *
	 * @param filename The path of the file.
	 */
	public static void touch(final String filename) {
		try (FileWriter fw = new FileWriter(filename)) {
			fw.write("");
		} catch (IOException e) {
			logger.error("Could not create file {}.", filename, e);
		}
	}

	/**
	 * This operation moves a file "from" to a destination "to".
	 * @param from The original file.
	 * @param to The destination of the file.
	 * @return Returns true, iff the move operation was successful.
	 */
	public static boolean move(final File from, final File to) {
		return from.renameTo(to);
	}

	/**
	 * Moves the path "from" to a destination path "to".
	 * @param from The original path.
	 * @param to The destination path where to move the original path to.
	 * @return Returns true, iff the move operation was successful.
	 */
	public static boolean move(final String from, final String to) {
		return move(new File(from), new File(to));
	}

	/**
	 * Returns a list of files contained in the specified folder.
	 * @param folder The folder for which the list of files shall be returned.
	 * @return A list of files contained in the given folder.
	 */
	public static List<File> getFilesOfFolder(final File folder) {
		List<File> files = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (!file.isDirectory()) {
				files.add(file);
			}
		}
		return files;
	}

	/**
	 * Checks whetehr a given file exists and if so, whether it is actually a file and not a directory.
	 * @param file The file to be checked.
	 * @throws FileIsDirectoryException Is thrown if the file exists but is a directory.
	 * @throws FileNotFoundException Is thrown if there exists no such file.
	 */
	public static void requireFileExists(final File file) throws FileIsDirectoryException, FileNotFoundException {
		Objects.requireNonNull(file);
		if (!file.exists()) {
			throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist");
		}
		if (!file.isFile()) {
			throw new FileIsDirectoryException("The file " + file.getAbsolutePath() + " is not a file but a directory.");
		}
	}

	/**
	 * Returns the file for a given path with the highest priority which also exists; the resource path is the backup solution with lowest priority.
	 * The getter iterates over the fileSytemPaths array and returns the first existing file, i.e. paths with a lower array index have higher priority.
	 * If there is no element in the array or none of the given paths exists as a file, the file corresponding to the resource path will be returned as
	 * a fall-back, i.e., the resource path has the lowest priority.
	 *
	 * @param resourcePath The resource path that is to be taken as a fall-back.
	 * @param fileSystemPaths An array of paths with descending priority.
	 * @return The existing file with highest priority.
	 */
	public static File getExistingFileWithHighestPriority(final String resourcePath, final String... fileSystemPaths) {
		if (fileSystemPaths.length > 0) {
			for (String fileSystemConfig : fileSystemPaths) {
				File configFile = new File(fileSystemConfig);
				if (configFile.exists()) {
					return configFile;
				}
			}
		}
		return ResourceUtil.getResourceAsFile(resourcePath);
	}

	/**
	 * Helper method to delete non-empty folders, i.e., recursively deleting all contained files and sub-folders.
	 *
	 * @param dir The folder to be deleted.
	 * @throws IOException Thrown, if there an issue arises while deleting all the files and sub-folders.
	 */
	public static void deleteFolderRecursively(final File dir) throws IOException {
		if (dir.isDirectory()) {
			final String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteFolderRecursively(new File(dir, children[i]));
			}
		}
		Files.delete(dir.toPath());// The directory is empty now and can be deleted.
	}
}
