package jaicore.basic;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility Class for managing temporary files. A directory for the temporary
 * files can be given, otherwise a new one in the Home Directory is created.
 * 
 * @author Lukas Brandt
 *
 */
public class TempFileHandler implements Closeable {

	// Directory where the temporary files will be saved
	private File tempFileDirectory;

	// Map of all temporary files identified with an UUID
	private Map<String, File> tempFiles;

	public TempFileHandler(File tempFileDirectory) {
		this.tempFileDirectory = tempFileDirectory;
		this.tempFiles = new HashMap<>();
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
		tempFiles.put(uuid, file);

		return uuid;
	}

	/**
	 * Get the temporary file with the UUID.
	 * 
	 * @param uuid
	 *            UUID of the file.
	 * @return File object associated with the UUID.
	 */
	public File getTempFile(String uuid) {
		return this.tempFiles.get(uuid);
	}

	/***
	 * Delete a temporary file by UUID.
	 * 
	 * @param uuid
	 *            UUID of the file.
	 */
	public void deleteTempFile(String uuid) {
		this.tempFiles.get(uuid).delete();
		this.tempFiles.remove(uuid);
	}

	/***
	 * Removes all temporary files.
	 */
	public void cleanUp() {
		for (String uuid : this.tempFiles.keySet()) {
			this.deleteTempFile(uuid);
		}
	}

	@Override
	public void close() throws IOException {
		cleanUp();
	}

}
