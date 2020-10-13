package ai.libs.mlplan.weka;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.regression.loss.dataset.RootMeanSquaredError;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;
import ai.libs.mlplan.weka.weka.WekaPipelineFactory;
import ai.libs.mlplan.weka.weka.WekaPipelineValidityCheckingNodeEvaluator;
import ai.libs.mlplan.weka.weka.WekaRegressorFactory;

public enum EMLPlanWekaProblemType implements IProblemType<IWekaClassifier> {

	CLASSIFICATION_MULTICLASS("automl/searchmodels/weka/weka-full.json", "conf/mlplan-weka.json", "mlplan/weka-preferenceList-autoweka.txt", "conf/preferenceList.txt", "AbstractClassifier", new WekaPipelineFactory(),
			EClassificationPerformanceMeasure.ERRORRATE, EClassificationPerformanceMeasure.ERRORRATE, new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>())), //

	CLASSIFICATION_MULTICLASS_REDUCED("automl/searchmodels/weka/weka-reduced.json", "conf/mlplan-weka.json", "mlplan/weka-preferenceList-autoweka.txt", "conf/preferenceList.txt", "AbstractClassifier", new WekaPipelineFactory(),
			EClassificationPerformanceMeasure.ERRORRATE, EClassificationPerformanceMeasure.ERRORRATE, new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>())), //

	CLASSIFICATION_MULTICLASS_BASE("automl/searchmodels/weka/base/index.json", "conf/mlplan-weka.json", "mlplan/weka-preferenceList-autoweka.txt", "conf/preferenceList.txt", "AbstractClassifier", new WekaPipelineFactory(),
			EClassificationPerformanceMeasure.ERRORRATE, EClassificationPerformanceMeasure.ERRORRATE, new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>())), //

	REGRESSION(EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFileFromResource(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFromFileSystem(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromResource(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromFileSystem(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getRequestedInterface(), new WekaRegressorFactory(), new RootMeanSquaredError(), new RootMeanSquaredError(), new RandomHoldoutSplitter<>(new Random(0), .7)), //

	CLASSIFICATION_MULTICLASS_TINY("automl/searchmodels/weka/weka-small.json", EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFromFileSystem(), "mlplan/weka-preferenceList-tiny.txt",
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromFileSystem(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getRequestedInterface(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getLearnerFactory(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPerformanceMetricForSearchPhase(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPerformanceMetricForSelectionPhase(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSelectionDatasetSplitter());

	private final String searchSpaceConfigFileFromResource;
	private final String systemSearchSpaceConfigFromFileSystem;

	private final String preferedComponentsListFromResource;
	private final String preferedComponentsListFromFileSystem;

	private final String requestedHascoInterface;

	private final ILearnerFactory<IWekaClassifier> learnerFactory;

	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase;
	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase;
	private final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> searchSelectionDatasetSplitter;

	private EMLPlanWekaProblemType(final String searchSpaceConfigFileFromResource, final String systemSearchSpaceConfigFromFileSystem, final String preferedComponentsListFromResource, final String preferedComponentsListFromFileSystem,
			final String requestedHascoInterface, final ILearnerFactory<IWekaClassifier> learnerFactory, final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase,
			final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase, final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> searchSelectionDatasetSplitter) {

		this.searchSpaceConfigFileFromResource = searchSpaceConfigFileFromResource;
		this.systemSearchSpaceConfigFromFileSystem = systemSearchSpaceConfigFromFileSystem;

		this.preferedComponentsListFromResource = preferedComponentsListFromResource;
		this.preferedComponentsListFromFileSystem = preferedComponentsListFromFileSystem;

		this.requestedHascoInterface = requestedHascoInterface;

		this.learnerFactory = learnerFactory;

		this.performanceMetricForSearchPhase = performanceMetricForSearchPhase;
		this.performanceMetricForSelectionPhase = performanceMetricForSelectionPhase;
		this.searchSelectionDatasetSplitter = searchSelectionDatasetSplitter;
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
		return this.getPreferredComponentName("PipelineClassifier");
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
	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter() {
		return this.searchSelectionDatasetSplitter;
	}

	@Override
	public ILearnerFactory<IWekaClassifier> getLearnerFactory() {
		return this.learnerFactory;
	}

	@Override
	public PipelineValidityCheckingNodeEvaluator getValidityCheckingNodeEvaluator() {
		return new WekaPipelineValidityCheckingNodeEvaluator();
	}
}
