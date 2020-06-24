package ai.libs.jaicore.ml.scikitwrapper;

public enum EBasicProblemType {

	REGRESSION("--regression", "", "", "", "BaseLearner"), CLASSIFICATION("", "sklearn/scikit_template_classification.twig.py", "AbstractClassifier", "BasicClassifier", "BaseLearner"),
	RUL("--rul", "sklearn/scikit_template_rul.twig.py", "AbstractRegressor", "BasicRegressor", "BaseLearner");

	private final String scikitLearnCommandLineFlag;
	private final String ressourceScikitTemplate;
	private final String requestedInterface;
	private final String requestedBasicProblemInterface;
	private final String requestedBaseLearnerInterface;

	private EBasicProblemType(final String scikitLearnCommandLineFlag, final String ressourceScikitTemplate, final String requestedInterface, final String requestedBasicProblemInterface, final String requestedBaseLearnerInterface) {
		this.scikitLearnCommandLineFlag = scikitLearnCommandLineFlag;
		this.ressourceScikitTemplate = ressourceScikitTemplate;
		this.requestedInterface = requestedInterface;
		this.requestedBasicProblemInterface = requestedBasicProblemInterface;
		this.requestedBaseLearnerInterface = requestedBaseLearnerInterface;
	}

	public String getScikitLearnCommandLineFlag() {
		return this.scikitLearnCommandLineFlag;
	}

	public String getRessourceScikitTemplate() {
		return this.ressourceScikitTemplate;
	}

	public String getRequestedInterface() {
		return this.requestedInterface;
	}

	public String getPreferredComponentName() {
		return this.getPreferredComponentName(this.requestedInterface);
	}

	public String getPreferredBasicProblemComponentName() {
		return this.getPreferredComponentName(this.requestedBasicProblemInterface);
	}

	private String getPreferredComponentName(final String requestedInterface) {
		return "resolve" + requestedInterface + "With";
	}

	public String getRequestedBaseLearnerInterface() {
		return this.requestedBaseLearnerInterface;
	}

	public static EBasicProblemType getProblemType(final String preferredComponentName) {
		for (EBasicProblemType problemType : EBasicProblemType.values()) {
			if (problemType.getPreferredComponentName().equals(preferredComponentName)) {
				return problemType;
			}
		}
		throw new IllegalArgumentException("No " + EBasicProblemType.class.getSimpleName() + " found for preferredComponentName=" + preferredComponentName);
	}

}
