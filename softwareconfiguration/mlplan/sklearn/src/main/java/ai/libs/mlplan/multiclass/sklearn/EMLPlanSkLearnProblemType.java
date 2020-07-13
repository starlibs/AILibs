package ai.libs.mlplan.multiclass.sklearn;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.ESkLearnProblemType;
import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.core.IProblemType;

public enum EMLPlanSkLearnProblemType implements IProblemType<ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch>> {

	CLASSIFICATION_MULTICLASS(ESkLearnProblemType.CLASSIFICATION, "automl/searchmodels/sklearn/sklearn-mlplan.json", "conf/mlplan-sklearn.json", "automl/searchmodels/sklearn/sklearn-preferenceList.txt", "conf/sklearn-preferenceList.txt", "MLPipeline",
			"BasicClassifier", EClassificationPerformanceMeasure.ERRORRATE, EClassificationPerformanceMeasure.ERRORRATE, 0.0), //
	CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES(ESkLearnProblemType.CLASSIFICATION, "automl/searchmodels/sklearn/ml-plan-ul.json", EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFromFileSystem(),
			EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromResource(), EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromFileSystem(), "AbstractClassifier", "BasicClassifier",
			EClassificationPerformanceMeasure.ERRORRATE, EClassificationPerformanceMeasure.ERRORRATE, 0.0), //
	RUL(ESkLearnProblemType.RUL, "automl/searchmodels/sklearn/sklearn-rul.json", "conf/sklearn-rul.json", "automl/searchmodels/sklearn/sklearn-preferenceList.txt", "conf/sklearn-preferenceList.txt", "MLPipeline", "BasicRegressor",
			ERulPerformanceMeasure.ASYMMETRIC_LOSS, ERulPerformanceMeasure.ASYMMETRIC_LOSS, 0.0);

	private final ESkLearnProblemType problemType;

	private final String searchSpaceConfigFileFromResource;
	private final String systemSearchSpaceConfigFromFileSystem;

	private final String preferedComponentsListFromResource;
	private final String preferedComponentsListFromFileSystem;

	private final String requestedHascoInterface;
	private final String requestedBasicProblemInterface;

	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase;
	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase;
	private final double portionOfDataReservedForSelectionPhase;

	private EMLPlanSkLearnProblemType(final ESkLearnProblemType problemType, final String searchSpaceConfigFileFromResource, final String systemSearchSpaceConfigFromFileSystem, final String preferedComponentsListFromResource,
			final String preferedComponentsListFromFileSystem, final String requestedHascoInterface, final String requestedBasicProblemInterface, final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase,
			final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase, final double portionOfDataReservedForSelectionPhase) {
		this.problemType = problemType;

		this.searchSpaceConfigFileFromResource = searchSpaceConfigFileFromResource;
		this.systemSearchSpaceConfigFromFileSystem = systemSearchSpaceConfigFromFileSystem;

		this.preferedComponentsListFromResource = preferedComponentsListFromResource;
		this.preferedComponentsListFromFileSystem = preferedComponentsListFromFileSystem;

		this.requestedHascoInterface = requestedHascoInterface;
		this.requestedBasicProblemInterface = requestedBasicProblemInterface;

		this.performanceMetricForSearchPhase = performanceMetricForSearchPhase;
		this.performanceMetricForSelectionPhase = performanceMetricForSelectionPhase;
		this.portionOfDataReservedForSelectionPhase = portionOfDataReservedForSelectionPhase;
	}

	public ESkLearnProblemType getSkLearnProblemType() {
		return this.problemType;
	}

	@Override
	public String getSearchSpaceConfigFileFromResource() {
		return this.searchSpaceConfigFileFromResource;
	}

	@Override
	public String getSearchSpaceConfigFromFileSystem() {
		return this.systemSearchSpaceConfigFromFileSystem;
	}

	@Override
	public String getPreferredComponentListFromResource() {
		return this.preferedComponentsListFromResource;
	}

	@Override
	public String getPreferredComponentListFromFileSystem() {
		return this.preferedComponentsListFromFileSystem;
	}

	@Override
	public String getRequestedInterface() {
		return this.requestedHascoInterface;
	}

	@Override
	public String getLastHASCOMethodPriorToParameterRefinementOfBareLearner() {
		return this.getPreferredComponentName(this.requestedHascoInterface);
	}

	@Override
	public String getLastHASCOMethodPriorToParameterRefinementOfPipeline() {
		return this.getPreferredComponentName(this.requestedBasicProblemInterface);
	}

	private String getPreferredComponentName(final String requestedInterface) {
		return "resolve" + requestedInterface + "With";
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSearchPhase() {
		return this.performanceMetricForSearchPhase;
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSelectionPhase() {
		return this.performanceMetricForSelectionPhase;
	}

	public static IProblemType getProblemType(final String preferredComponentName) {
		for (EMLPlanSkLearnProblemType problemType : EMLPlanSkLearnProblemType.values()) {
			if (problemType.getLastHASCOMethodPriorToParameterRefinementOfBareLearner().equals(preferredComponentName)) {
				return problemType;
			}
		}
		throw new IllegalArgumentException("No " + ESkLearnProblemType.class.getSimpleName() + " found for preferredComponentName=" + preferredComponentName);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "." + this.toString();
	}

	@Override
	public ILearnerFactory<ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch>> getLearnerFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter() {
		// TODO Auto-generated method stub
		return null;
	}

}
