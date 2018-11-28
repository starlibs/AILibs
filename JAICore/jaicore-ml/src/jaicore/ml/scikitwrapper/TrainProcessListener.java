package jaicore.ml.scikitwrapper;

public class TrainProcessListener extends DefaultProcessListener {

	private static final String DUMP_NAME_FLAG = "dump: ";
	String modelPath = "";

	public TrainProcessListener(boolean verbose) {
		super(verbose);
	}

	public String getModelPath() {
		if (modelPath.equals("")) {
			System.err.println("No model name was returned.");
		}
		return modelPath;
	}

	@Override
	public void handleError(String error) {
		super.handleError(error);
	}

	@Override
	public void handleInput(String input) {
		super.handleInput(input);
		if (input.startsWith(DUMP_NAME_FLAG)) {
			modelPath = input.substring(DUMP_NAME_FLAG.length());
		}
	}

}
