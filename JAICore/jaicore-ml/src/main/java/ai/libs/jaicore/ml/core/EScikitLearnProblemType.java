package ai.libs.jaicore.ml.core;

public enum EScikitLearnProblemType {

	REGRESSION("--regression", "sklearn/scikit_template_classification.twig.py"), CLASSIFICATION("", "sklearn/scikit_template_classification.twig.py", "tpot"), RUL("--rul", "sklearn/scikit_template_rul.twig.py"), FEATURE_ENGINEERING("--fe",
			"sklearn/scikit_template_rul.twig.py");

	private final String[] pythonRequiredModules;
	private final String scikitLearnCommandLineFlag;
	private final String ressourceScikitTemplate;

	private EScikitLearnProblemType(final String scikitLearnCommandLineFlag, final String ressourceScikitTemplate, final String... pythonRequiredModules) {
		this.scikitLearnCommandLineFlag = scikitLearnCommandLineFlag;
		this.ressourceScikitTemplate = ressourceScikitTemplate;
		this.pythonRequiredModules = pythonRequiredModules;
	}

	public String[] getPythonRequiredModules() {
		return this.pythonRequiredModules;
	}

	public String getScikitLearnCommandLineFlag() {
		return this.scikitLearnCommandLineFlag;
	}

	public String getRessourceScikitTemplate() {
		return this.ressourceScikitTemplate;
	}

	public String getName() {
		return this.getClass().getSimpleName() + "." + this.toString();
	}

}
