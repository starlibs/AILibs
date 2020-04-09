package ai.libs.jaicore.ml.scikitwrapper;

public enum EBasicProblemType {

	REGRESSION("--regression", ""), CLASSIFICATION("", "sklearn/scikit_template_classification.twig.py"), RUL("--rul", "sklearn/scikit_template_rul.twig.py");

	private final String scikitLearnCommandLineFlag;
	private final String ressourceScikitTemplate;

	private EBasicProblemType(final String scikitLearnCommandLineFlag, final String ressourceScikitTemplate) {
		this.scikitLearnCommandLineFlag = scikitLearnCommandLineFlag;
		this.ressourceScikitTemplate = ressourceScikitTemplate;
	}

	public String getScikitLearnCommandLineFlag() {
		return this.scikitLearnCommandLineFlag;
	}

	public String getRessourceScikitTemplate() {
		return this.ressourceScikitTemplate;
	}

}
