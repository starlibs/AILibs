package jaicore.ml.scikitwrapper;

import java.io.IOException;

public interface IProcessListener {

	/**
	 * Lets the process listener listen to the output and error stream of the given process.
	 * @param process The process to be listened to.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void listenTo(Process process) throws IOException, InterruptedException;

}
