package jaicore.ml.skikitwrapper;

import java.util.Scanner;

public abstract class ProcessListener {
	Scanner input;
	Scanner error;

	public void listenTo(Process process) {
		Scanner input = new Scanner(process.getInputStream());
		input.useDelimiter("\n");
		Scanner error = new Scanner(process.getErrorStream());
		error.useDelimiter("\n");
		// While process is alive the output- and error stream is output.
		while (process.isAlive()) {
			while (input.hasNext()) {
				handleInput(input.next());
			}
			while(error.hasNext())
				handleError(error.next());
		}
		if (input.hasNext()) {
			handleInput(input.next());
		}
		if (error.hasNext()) {
			handleError(error.next());
		}
		input.close();
		error.close();
	}

	public abstract void handleError(String error);

	public abstract void handleInput(String input);
}
