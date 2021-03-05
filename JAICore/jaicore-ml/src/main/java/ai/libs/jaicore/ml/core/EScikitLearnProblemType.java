package ai.libs.jaicore.ml.core;

public enum EScikitLearnProblemType {

	REGRESSION("regression"), //
	CLASSIFICATION("classification"), //
	TIME_SERIES_REGRESSION("ts-reg", "rul-python-connection"), //
	TIME_SERIES_FEATURE_ENGINEERING("ts-fe", "rul-python-connection");

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
