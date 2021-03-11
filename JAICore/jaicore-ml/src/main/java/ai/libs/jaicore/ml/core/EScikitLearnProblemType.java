package ai.libs.jaicore.ml.core;

public enum EScikitLearnProblemType {

	REGRESSION("regression"), //
	CLASSIFICATION("classification", new String[] {}, new String[] { "tpot", "xgboost" }), //
	TIME_SERIES_REGRESSION("ts-reg", new String[] { "rul-python-connection" }), //
	TIME_SERIES_FEATURE_ENGINEERING("ts-fe", new String[] { "rul-python-connection" });

	private String scikitLearnCommandLineFlag;
	private String[] pythonRequiredModules;
	private String[] pythonOptionalModules;

	private EScikitLearnProblemType(final String scikitLearnCommandLineFlag, final String[] pythonRequiredModules, final String[] pythonOptionalModules) {
		this(scikitLearnCommandLineFlag, pythonRequiredModules);
		this.pythonOptionalModules = pythonOptionalModules;
	}

	private EScikitLearnProblemType(final String scikitLearnCommandLineFlag, final String[] pythonRequiredModules) {
		this(scikitLearnCommandLineFlag);
		this.pythonRequiredModules = pythonRequiredModules;
	}

	private EScikitLearnProblemType(final String scikitLearnCommandLineFlag) {
		this.scikitLearnCommandLineFlag = scikitLearnCommandLineFlag;
		this.pythonRequiredModules = new String[0];
		this.pythonOptionalModules = new String[0];
	}

	public String getScikitLearnCommandLineFlag() {
		return this.scikitLearnCommandLineFlag;
	}

	public String[] getPythonRequiredModules() {
		return this.pythonRequiredModules;
	}

	public String[] getPythonOptionalModules() {
		return this.pythonOptionalModules;
	}

	public String getName() {
		return this.getClass().getSimpleName() + "." + this.toString();
	}

}
