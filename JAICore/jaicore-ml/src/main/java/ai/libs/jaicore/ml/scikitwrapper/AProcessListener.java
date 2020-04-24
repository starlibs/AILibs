package ai.libs.jaicore.ml.scikitwrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.base.Strings;

import ai.libs.jaicore.processes.ProcessUtil;

/**
 * The process listener may be attached to a process in order to handle its ouputs streams in a controlled way.
 * For instance, the process listener can be used to pipe all outputs of the invoked process to a logger system.
 *
 * @author scheiblm, wever
 */
public abstract class AProcessListener implements IProcessListener {

	private boolean listenForPIDFromProcess = false;
	private int processIDObtainedFromListening = -1;

	public AProcessListener() {

	}

	public AProcessListener(final boolean listenForPIDFromProcess) {
		this.listenForPIDFromProcess = listenForPIDFromProcess;
	}

	@Override
	public void listenTo(final Process process) throws IOException, InterruptedException {
		try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream())); BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			// While process is alive the output- and error stream is output.
			while (process.isAlive()) {
				if (Thread.currentThread().isInterrupted()) {
					if (this.listenForPIDFromProcess && this.processIDObtainedFromListening > 0) {
						ProcessUtil.killProcess(this.processIDObtainedFromListening);
					} else {
						ProcessUtil.killProcess(process);
					}
					throw new InterruptedException("Process execution was interrupted.");
				}
				String line;
				while (inputReader.ready() && (line = inputReader.readLine()) != null) {
					this.handleProcessIDLine(line);
					if (line.contains("import imp") || line.contains("imp module")) {
						continue;
					}
					this.handleInput(line);
				}
				while (errorReader.ready() && (line = errorReader.readLine()) != null) {
					if (line.contains("import imp") || line.contains("imp module")) {
						continue;
					}
					this.handleError(line);
				}
			}
		}
	}

	private void handleProcessIDLine(final String line) {
		if (this.listenForPIDFromProcess && !Strings.isNullOrEmpty(line)) {
			if (line.startsWith("CURRENT_PID:")) {
				this.processIDObtainedFromListening = Integer.parseInt(line.replaceAll("CURRENT_PID:", "").strip());
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
