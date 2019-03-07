package jaicore.ml.scikitwrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The process listener may be attached to a process in order to handle its ouputs streams in a controlled way.
 * For instance, the process listener can be used to pipe all outputs of the invoked process to a logger system.
 *
 * @author scheiblm, wever
 */
public abstract class AProcessListener implements IProcessListener {

	@Override
	public void listenTo(final Process process) throws IOException, InterruptedException {
		try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream())); BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			// While process is alive the output- and error stream is output.
			while (process.isAlive()) {
				if (Thread.currentThread().isInterrupted()) {
					process.destroyForcibly();
					throw new InterruptedException("Process execution was interrupted.");
				}
				String line;
				while ((line = input.readLine()) != null) {
					this.handleInput(line);
				}
				while ((line = error.readLine()) != null) {
					this.handleError(line);
				}
			}
		}
	}

	/**
	 * Handle the output of the error output stream.
	 *
	 * @param error The line sent by the process via the error stream.
	 * @throws IOException An IOException is thrown if there is an issue reading from the process's stream.
	 * @throws InterruptedException An interrupted exception is thrown if the thread/process is interrupted while processing the error messages.
	 */
	public abstract void handleError(String error) throws IOException, InterruptedException;

	/**
	 * Handle the output of the standard output stream.
	 *
	 * @param error The line sent by the process via the standard output stream.
	 * @throws IOException An IOException is thrown if there is an issue reading from the process's stream.
	 * @throws InterruptedException An interrupted exception is thrown if the thread/process is interrupted while processing the standard output messages.
	 */
	public abstract void handleInput(String input) throws IOException, InterruptedException;
}
