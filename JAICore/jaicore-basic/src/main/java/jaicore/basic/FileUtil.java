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

	public static List<String> readFileAsList(final File file) throws IOException {
		return readFileAsList(file.getAbsolutePath());
	}

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

	public static String readFileAsString(final File file) throws IOException {
		return readFileAsString(file.getAbsolutePath());
	}

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

	public static void deleteFolderRecursively(final File dir) throws IOException {
		if (dir.isDirectory()) {
			final String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteFolderRecursively(new File(dir, children[i]));
			}
		}
		Files.delete(dir.toPath());// The directory is empty now and can be deleted.
	}

	public static void zipFiles(final Collection<String> files, final String archive) throws FileNotFoundException, IOException {
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

	public static Object unserializeObject(final String pathname) throws IOException, ClassNotFoundException {
		try (ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(new FileInputStream(pathname)))) {
			return is.readObject();
		}
	}

	public static void touch(final String filename) {
		try (FileWriter fw = new FileWriter(filename)) {
			fw.write("");
		} catch (IOException e) {
			logger.error("Could not create file {}.", filename, e);
		}
	}

	public static boolean move(final File from, final File to) {
		return from.renameTo(to);
	}

	public static void move(final String from, final String to) {
		move(new File(from), new File(to));
	}

	public static List<File> getFilesOfFolder(final File folder) {
		List<File> files = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (!file.isDirectory()) {
				files.add(file);
			}
		}
		return files;
	}

	public static Properties readPropertiesFile(final File propertiesFile) throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(propertiesFile));
		return props;
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
}
