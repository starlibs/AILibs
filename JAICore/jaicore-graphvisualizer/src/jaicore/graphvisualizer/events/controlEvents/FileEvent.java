package jaicore.graphvisualizer.events.controlEvents;

import java.io.File;

/**
 * A ControlEvent which function is to interact with files. The main purpose of
 * this event is to load data from or store data in a file.
 * 
 * @author jkoepe
 *
 */
public class FileEvent implements ControlEvent {

	/*
	 * A boolean which indicates, if data should be stored or loaded.
	 */
	private boolean load;
	/*
	 * The file to interact with
	 */
	private File file;

	/**
	 * A Constructor for a FileEvent
	 * 
	 * @param load A boolean, which indicates if the event is used to store data, or
	 *             load data
	 * @param file The file, which will be altered.
	 */
	public FileEvent(boolean load, File file) {
		this.load = load;
		this.file = file;
	}

	/**
	 * @return <code>true</code> if the event is used to load data,
	 *         <code>false</code> else
	 */
	public boolean isLoad() {
		return load;
	}

	/**
	 * Returns the file to interact with.
	 * 
	 * @return The file.
	 */
	public File getFile() {
		return file;
	}
}
