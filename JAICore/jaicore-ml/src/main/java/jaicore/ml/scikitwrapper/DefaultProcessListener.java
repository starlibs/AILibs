package jaicore.ml.scikitwrapper;

public class DefaultProcessListener extends ProcessListener {
	boolean verbose;

	public DefaultProcessListener(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public void handleError(String error) {
		if (error.equals("  import imp") || error.equals(
				"/usr/lib/python3.7/site-packages/sklearn/externals/joblib/externals/cloudpickle/cloudpickle.py:47: DeprecationWarning: the imp module is deprecated in favour of importlib; see the module's documentation for alternative uses")) {
			return;
		}
		System.err.println(">>> " + error);
	}

	@Override
	public void handleInput(String input) {
		if (verbose)
			System.out.println(">>> " + input);
	}

}
