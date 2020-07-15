package ai.libs.mlplan.multiclass.sklearn;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.ESkLearnProblemType;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;

public enum EMLPlanSkLearnProblemType implements IProblemType<ScikitLearnWrapper<IPrediction, IPredictionBatch>> {

	CLASSIFICATION_MULTICLASS(ESkLearnProblemType.CLASSIFICATION, "automl/searchmodels/sklearn/sklearn-mlplan.json", "conf/mlplan-sklearn.json", "automl/searchmodels/sklearn/sklearn-preferenceList.txt", "conf/sklearn-preferenceList.txt",
			"MLPipeline", "BasicClassifier", EClassificationPerformanceMeasure.ERRORRATE, EClassificationPerformanceMeasure.ERRORRATE, new DefaultSKLearnClassifierFactory(),
			new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>())), //
	CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES(ESkLearnProblemType.CLASSIFICATION, "automl/searchmodels/sklearn/ml-plan-ul.json", EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFromFileSystem(),
			EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromResource(), EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromFileSystem(), "AbstractClassifier", "BasicClassifier",
			EClassificationPerformanceMeasure.ERRORRATE, EClassificationPerformanceMeasure.ERRORRATE, CLASSIFICATION_MULTICLASS.getLearnerFactory(), CLASSIFICATION_MULTICLASS.getSearchSelectionDatasetSplitter()), //
	RUL(ESkLearnProblemType.RUL, "automl/searchmodels/sklearn/sklearn-rul.json", "conf/sklearn-rul.json", null, "conf/sklearn-preferenceList.txt", "MLPipeline", "BasicRegressor", ERulPerformanceMeasure.ASYMMETRIC_LOSS,
			ERulPerformanceMeasure.ASYMMETRIC_LOSS, new RULSKLearnFactory(), CLASSIFICATION_MULTICLASS.getSearchSelectionDatasetSplitter());

	private final ESkLearnProblemType problemType;

	private final String searchSpaceConfigFileFromResource;
	private final String systemSearchSpaceConfigFromFileSystem;

	private final String preferedComponentsListFromResource;
	private final String preferedComponentsListFromFileSystem;

	private final String requestedHascoInterface;
	private final String requestedBasicProblemInterface;

	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase;
	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase;

	private final ASKLearnClassifierFactory learnerFactory;

	private final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> searchSelectionDatasetSplitter;

	private EMLPlanSkLearnProblemType(final ESkLearnProblemType problemType, final String searchSpaceConfigFileFromResource, final String systemSearchSpaceConfigFromFileSystem, final String preferedComponentsListFromResource,
			final String preferedComponentsListFromFileSystem, final String requestedHascoInterface, final String requestedBasicProblemInterface, final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase,
			final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase, final ASKLearnClassifierFactory learnerFactory,
			final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> searchSelectionDatasetSplitter) {
		this.problemType = problemType;

		this.searchSpaceConfigFileFromResource = searchSpaceConfigFileFromResource;
		this.systemSearchSpaceConfigFromFileSystem = systemSearchSpaceConfigFromFileSystem;

		this.preferedComponentsListFromResource = preferedComponentsListFromResource;
		this.preferedComponentsListFromFileSystem = preferedComponentsListFromFileSystem;

		this.requestedHascoInterface = requestedHascoInterface;
		this.requestedBasicProblemInterface = requestedBasicProblemInterface;

		this.performanceMetricForSearchPhase = performanceMetricForSearchPhase;
		this.performanceMetricForSelectionPhase = performanceMetricForSelectionPhase;
		this.learnerFactory = learnerFactory;

		this.searchSelectionDatasetSplitter = searchSelectionDatasetSplitter;
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

	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "." + this.toString();
	}

	@Override
	public ASKLearnClassifierFactory getLearnerFactory() {
		return this.learnerFactory;
	}

	@Override
	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter() {
		return this.searchSelectionDatasetSplitter;
	}

	@Override
	public PipelineValidityCheckingNodeEvaluator getValidityCheckingNodeEvaluator() {
		return null; // we do not have such a checker for sklearn pipelines
	}

}
