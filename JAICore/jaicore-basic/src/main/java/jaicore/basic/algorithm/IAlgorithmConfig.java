package jaicore.basic.algorithm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.aeonbits.owner.Mutable;

import jaicore.basic.FileUtil;
import jaicore.basic.PropertiesLoadFailedException;

public interface IAlgorithmConfig extends Mutable {

	public static final String K_CPUS = "cpus";
	public static final String K_THREADS = "threads";
	public static final String K_MEMORY = "memory";
	public static final String K_TIMEOUT = "timeout";

	/**
	 * @return Number of CPU cores available for parallelization.
	 */
	@Key(K_CPUS)
	@DefaultValue("8")
	public int cpus();

	/**
	 * @return Number of threads that may be spawned by the algorithm. If set to -1, the number of CPUs is used as the number of threads. If set to 0, parallelization is deactivated.
	 */
	@Key(K_THREADS)
	@DefaultValue("-1")
	public int threads();

	/**
	 * @return The main memory that is available to be used. This is merely a documentation variable since the true memory must be set over the JVM initialization anyway and cannot be restricted inside of it.
	 */
	@Key(K_MEMORY)
	@DefaultValue("256")
	public int memory();

	/**
	 * @return Overall timeout for the algorithm in milliseconds.
	 */
	@Key(K_TIMEOUT)
	@DefaultValue("-1")
	public long timeout();

	/**
	 * Reads properties of a config from a config file.
	 * @param file The file to read in as properties.
	 * @throws IOException Throws an IOException if an issue occurs while reading in the properties from the given file.
	 */
	default IAlgorithmConfig loadPropertiesFromFile(final File file) {
		if (!file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("File (" + file.getAbsolutePath() + ") to load properties from does not exist or is not a file.");
		}
		try {
			return loadPropertiesFromList(FileUtil.readFileAsList(file));
		} catch (IOException e) {
			throw new PropertiesLoadFailedException("Could not load properties from the given file.", e);
		}
	}

	/**
	 * Loads properties from a resource (instead of a file).
	 * @param resourcePath The path to the resource.
	 * @throws IOException Throws an IOException if an issue occurs while reading in the properties from the given resource.
	 */
	default IAlgorithmConfig loadPropertiesFromResource(final String resourcePath) throws IOException {
		// Get file from resources folder
		ClassLoader classLoader = FileUtil.class.getClassLoader();
		String content = null;
		try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
			ByteArrayOutputStream result = new ByteArrayOutputStream(1024);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			content = result.toString(StandardCharsets.UTF_8.name());
		}

		if (content != null) {
			return loadPropertiesFromList(Arrays.asList(content.split("\n")));
		}
		return this;
	}

	/**
	 * Loads a properties config from a list of property assignments.
	 *
	 * @param propertiesList The list of property assignments.
	 */
	default IAlgorithmConfig loadPropertiesFromList(final List<String> propertiesList) {
		for (String line : propertiesList) {
			if (!line.contains("=") || line.startsWith("#")) {
				continue;
			}
			String[] split = line.split("=");
			this.setProperty(split[0].trim(), split[1].trim());
		}
		return this;
	}
}
