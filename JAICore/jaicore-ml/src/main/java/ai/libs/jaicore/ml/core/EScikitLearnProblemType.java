package ai.libs.jaicore.ml.core;

public enum EScikitLearnProblemType {

	REGRESSION("regression"), //
	CLASSIFICATION("classification"), //
	RUL("rul", "rul-python-connection"), //
	FEATURE_ENGINEERING("fe", "rul-python-connection");

	private final String scikitLearnCommandLineFlag;
	private final String[] pythonRequiredModules;

	private EScikitLearnProblemType(final String scikitLearnCommandLineFlag, final String... pythonRequiredModules) {
		this.scikitLearnCommandLineFlag = scikitLearnCommandLineFlag;
		this.pythonRequiredModules = pythonRequiredModules;
	}

	public String getScikitLearnCommandLineFlag() {
		return this.scikitLearnCommandLineFlag;
	}

	public String[] getPythonRequiredModules() {
		return this.pythonRequiredModules;
	}

	public String getName() {
		return this.getClass().getSimpleName() + "." + this.toString();
	}

}
