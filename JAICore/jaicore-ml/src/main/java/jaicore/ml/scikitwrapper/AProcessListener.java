package jaicore.ml.scikitwrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class AProcessListener implements IProcessListener {

	@Override
	public void listenTo(final Process process) throws IOException, InterruptedException {
		try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream())); BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			// While process is alive the output- and error stream is output.
			while (process.isAlive()) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException();
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

	public abstract void handleError(String error) throws IOException, InterruptedException;

	public abstract void handleInput(String input) throws IOException, InterruptedException;
}
