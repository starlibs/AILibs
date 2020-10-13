package ai.libs.jaicore.ml.scikitwrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import ai.libs.jaicore.processes.EOperatingSystem;
import ai.libs.jaicore.processes.ProcessUtil;

/**
 * The process listener may be attached to a process in order to handle its ouputs streams in a controlled way.
 * For instance, the process listener can be used to pipe all outputs of the invoked process to a logger system.
 *
 * @author scheiblm, wever
 */
public abstract class AProcessListener implements IProcessListener, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(AProcessListener.class);

	private boolean listenForPIDFromProcess = false;
	private int processIDObtainedFromListening = -1;

	public AProcessListener() {

	}

	public AProcessListener(final boolean listenForPIDFromProcess) {
		this.listenForPIDFromProcess = listenForPIDFromProcess;
	}

	@Override
	public void listenTo(final Process process) throws IOException, InterruptedException {
		this.logger.info("Starting to listen to process {}", process);
		try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream())); BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			// While process is alive the output- and error stream is output.
			while (process.isAlive()) {
				this.logger.debug("Process is alive.");
				if (Thread.interrupted()) { // reset flag since we will throw an exception now
					this.logger.info("Detected interrupt on process execution.");
					if (this.listenForPIDFromProcess && this.processIDObtainedFromListening > 0) {
						ProcessUtil.killProcess(this.processIDObtainedFromListening);
					} else {
						ProcessUtil.killProcess(process);
					}
					throw new InterruptedException("Process execution was interrupted.");
				}
				String line;
				while (this.checkReady(inputReader) && (line = inputReader.readLine()) != null) {
					this.handleProcessIDLine(line);
					if (line.contains("import imp") || line.contains("imp module")) {
						continue;
					}
					this.handleInput(line);
				}
				while (this.checkReady(inputReader) && (line = errorReader.readLine()) != null) {
					if (line.contains("import imp") || line.contains("imp module")) {
						continue;
					}
					this.handleError(line);
				}
			}
		}
	}

	private boolean checkReady(final BufferedReader inputReader) throws IOException {
		if (ProcessUtil.getOS() == EOperatingSystem.MAC) {
			return inputReader.ready();
		} else {
			return true;
		}
	}

	private void handleProcessIDLine(final String line) {
		if (this.listenForPIDFromProcess && !Strings.isNullOrEmpty(line)) {
			if (line.startsWith("CURRENT_PID:")) {
				this.processIDObtainedFromListening = Integer.parseInt(line.replace("CURRENT_PID:", "").trim());
				this.logger.debug("Listen to process id: {}", this.processIDObtainedFromListening);
			}
			this.logger.trace("Other console output: {}", line);
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

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
