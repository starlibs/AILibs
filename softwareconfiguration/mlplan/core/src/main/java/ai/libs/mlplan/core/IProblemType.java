package ai.libs.mlplan.core;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

public interface IProblemType<L extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> {

	public String getName();

	public String getSearchSpaceConfigFileFromResource();

	public String getSearchSpaceConfigFromFileSystem();

	public String getRequestedInterface();

	public String getPreferredComponentListFromResource();

	public String getPreferredComponentListFromFileSystem();

	public String getLastHASCOMethodPriorToParameterRefinementOfBareLearner();

	public String getLastHASCOMethodPriorToParameterRefinementOfPipeline();

	public PipelineValidityCheckingNodeEvaluator getValidityCheckingNodeEvaluator();

	public ILearnerFactory<L> getLearnerFactory();

	public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSearchPhase();

	public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSelectionPhase();

	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter();
}
