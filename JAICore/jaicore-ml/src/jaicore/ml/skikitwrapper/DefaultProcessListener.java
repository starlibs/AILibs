package jaicore.ml.skikitwrapper;

public class DefaultProcessListener extends ProcessListener {

	@Override
	public void handleError(String error) {
		System.err.println(">>> " + error);
	}

	@Override
	public void handleInput(String input) {
		System.out.println(">>> " + input);
	}

}
