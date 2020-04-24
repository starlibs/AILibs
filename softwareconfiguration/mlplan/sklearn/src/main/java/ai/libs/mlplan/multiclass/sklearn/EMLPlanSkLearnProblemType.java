package ai.libs.mlplan.multiclass.sklearn;

import ai.libs.jaicore.ml.scikitwrapper.EBasicProblemType;

public enum EMLPlanSkLearnProblemType {

	CLASSIFICATION(EBasicProblemType.CLASSIFICATION, "automl/searchmodels/sklearn/sklearn-mlplan.json", "conf/mlplan-sklearn.json", "AbstractClassifier"), RUL(EBasicProblemType.RUL, "automl/searchmodels/sklearn/sklearn-rul.json",
			"conf/sklearn-rul.json", "AbstractRegressor");

	private final EBasicProblemType problemType;

	private final String resourceSearchSpaceConfigFile;
	private final String fileSystemSearchSpaceConfig;
	private final String requestedHascoInterface;

	private EMLPlanSkLearnProblemType(final EBasicProblemType problemType, final String resourceSearchSpaceConfigFile, final String fileSystemSearchSpaceConfig, final String requestedHascoInterface) {
		this.problemType = problemType;
		this.resourceSearchSpaceConfigFile = resourceSearchSpaceConfigFile;
		this.fileSystemSearchSpaceConfig = fileSystemSearchSpaceConfig;
		this.requestedHascoInterface = requestedHascoInterface;
	}

	public EBasicProblemType getBasicProblemType() {
		return this.problemType;
	}

	public String getResourceSearchSpaceConfigFile() {
		return this.resourceSearchSpaceConfigFile;
	}

	public String getFileSystemSearchSpaceConfig() {
		return this.fileSystemSearchSpaceConfig;
	}

	public String getRequestedInterface() {
		return this.requestedHascoInterface;
	}

}