package jaicore.basic;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class for managing temporary files and corresponding readers/writers.
 * A directory for the temporary files can be given, otherwise a new one in the
 * Home Directory is created.
 *
 * @author Lukas Brandt
 *
 */
public class TempFileHandler implements Closeable {

	private static final String ERR_MSG_CANNOT_CLOSE_READER = "Cannot close reader";

	private Logger logger = LoggerFactory.getLogger(TempFileHandler.class);

	// Directory where the temporary files will be saved
	private File tempFileDirectory;

	// Map of all temporary files identified with an UUID
	private Map<String, File> tempFiles;

	// Maps of the readers/writers for the corresponding files identified by UUID
	private Map<String, BufferedReader> tempFileReaders;
	private Map<String, FileWriter> tempFileWriters;

	public TempFileHandler(final File tempFileDirectory) {
		this.tempFileDirectory = tempFileDirectory;
		this.tempFiles = new HashMap<>();
		this.tempFileReaders = new HashMap<>();
		this.tempFileWriters = new HashMap<>();
	}

	public TempFileHandler() {
		this(new File(System.getProperty("user.home")));
	}

	/**
	 * Create a new temporary file in the given directory.
	 *
	 * @return UUID associated with the new temporary file.
	 */
	public String createTempFile() {
		String uuid = UUID.randomUUID().toString();
		String path = this.tempFileDirectory.getAbsolutePath() + File.separator + uuid;

		FileUtil.touch(path);
		File file = new File(path);
		file.deleteOnExit();
		this.tempFiles.put(uuid, file);

		return uuid;
	}

	/**
	 * Get the temporary file with the UUID.
	 *
	 * @param uuid
	 *            UUID of the file.
	 * @return File object associated with the UUID.
	 */
	public File getTempFile(final String uuid) {
		return this.tempFiles.get(uuid);
	}

	/**
	 * Create or retrieve an existing file reader for a temporary file by UUID.
	 *
	 * @param uuid
	 *            UUID of the temporary file.
	 * @return An existing or new file reader for the given temporary file.
	 */
	public BufferedReader getFileReaderForTempFile(final String uuid) {
		if (this.tempFileReaders.containsKey(uuid)) {
			try {
				this.tempFileReaders.get(uuid).close();
			} catch (IOException e) {
				this.logger.error(ERR_MSG_CANNOT_CLOSE_READER, e);
			}
			this.tempFileReaders.remove(uuid);
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.tempFiles.get(uuid)));
			this.tempFileReaders.put(uuid, reader);
			return reader;
		} catch (FileNotFoundException e) {
			this.logger.error(String.format("File for UUID %s does not exist!", uuid), e);
		}

		return null;
	}

	/**
	 * Create or retrieve an existing file writer for a temporary file by UUID.
	 *
	 * @param uuid
	 *            UUID of the temporary file.
	 * @return An existing or new file writer for the given temporary file.
	 */
	public FileWriter getFileWriterForTempFile(final String uuid) {
		if (this.tempFileWriters.containsKey(uuid)) {
			return this.tempFileWriters.get(uuid);
		} else {
			try {
				FileWriter writer = new FileWriter(this.tempFiles.get(uuid));
				this.tempFileWriters.put(uuid, writer);
				return writer;
			} catch (IOException e) {
				this.logger.error(String.format("Cannot create FileWriter for file with UUID %s", uuid), e);
			}

		}
		return null;
	}

	/***
	 * Delete a temporary file by UUID and if created the corresponding
	 * reader/writer.
	 *
	 * @param uuid
	 *            UUID of the file.
	 */
	public void deleteTempFile(final String uuid) {
		if (this.tempFileReaders.containsKey(uuid)) {
			try {
				this.tempFileReaders.get(uuid).close();
			} catch (IOException e) {
				this.logger.error(ERR_MSG_CANNOT_CLOSE_READER, e);
			}
			this.tempFileReaders.remove(uuid);
		}
		if (this.tempFileWriters.containsKey(uuid)) {
			try {
				this.tempFileWriters.get(uuid).close();
			} catch (IOException e) {
				this.logger.error(ERR_MSG_CANNOT_CLOSE_READER, e);
			}
			this.tempFileWriters.remove(uuid);
		}
		if (this.tempFiles.containsKey(uuid)) {
			try {
				Files.delete(this.tempFiles.get(uuid).toPath());
			} catch (IOException e) {
				this.logger.error(String.format("Cannot delete file for UUID %s", uuid), e);
			}
		}
	}

	/***
	 * Removes all temporary files and close all readers/writers.
	 */
	public void cleanUp() {
		Set<String> uuids = new HashSet<>(this.tempFiles.keySet());
		for (String uuid : uuids) {
			this.deleteTempFile(uuid);
		}
	}

	@Override
	public void close() throws IOException {
		this.cleanUp();
	}

	/**
	 * Returns the absolute path of the temporary file directory.
	 *
	 * @return The absolute path of the temporary file directory.
	 */
	public String getTempFileDirPath() {
		return this.tempFileDirectory.getAbsolutePath();
	}

}
