package ai.libs.mlplan.meka;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.InstanceWiseF1;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;

public enum EMLPlanMekaProblemType implements IProblemType<IMekaClassifier> {

	CLASSIFICATION_MULTILABEL("automl/searchmodels/meka/mlplan-meka.json", "conf/searchmodels/mlplan-meka.json", "mlplan/meka-preferenceList.txt", "conf/mlpan-meka-preferenceList.txt", "MLClassifier", "MLClassifier",
			new MekaPipelineFactory(), new InstanceWiseF1(), new InstanceWiseF1(), new RandomHoldoutSplitter<>(0, .7));

	private final String searchSpaceConfigFileFromResource;
	private final String systemSearchSpaceConfigFromFileSystem;
	private final String preferedComponentsListFromResource;
	private final String preferedComponentsListFromFileSystem;

	private final String requestedHascoInterface;
	private final String requestedBasicProblemInterface;

	private final ILearnerFactory<IMekaClassifier> learnerFactory;

	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase;
	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase;
	private final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> searchSelectionDatasetSplitter;

	private EMLPlanMekaProblemType(final String searchSpaceConfigFileFromResource, final String systemSearchSpaceConfigFromFileSystem, final String preferedComponentsListFromResource, final String preferedComponentsListFromFileSystem,
			final String requestedHascoInterface, final String requestedBasicProblemInterface, final ILearnerFactory<IMekaClassifier> learnerFactory, final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase,
			final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase, final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> searchSelectionDatasetSplitter) {

		this.searchSpaceConfigFileFromResource = searchSpaceConfigFileFromResource;
		this.systemSearchSpaceConfigFromFileSystem = systemSearchSpaceConfigFromFileSystem;

		this.preferedComponentsListFromResource = preferedComponentsListFromResource;
		this.preferedComponentsListFromFileSystem = preferedComponentsListFromFileSystem;

		this.requestedHascoInterface = requestedHascoInterface;
		this.requestedBasicProblemInterface = requestedBasicProblemInterface;

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
	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter() {
		return this.searchSelectionDatasetSplitter;
	}

	@Override
	public ILearnerFactory<IMekaClassifier> getLearnerFactory() {
		return this.learnerFactory;
	}

	@Override
	public PipelineValidityCheckingNodeEvaluator getValidityCheckingNodeEvaluator() {
		return null; // we do not have such a checker for meka pipelines
	}
}
