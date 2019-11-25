package ai.libs.mlplan.core;

import java.io.File;

import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.hasco.core.HASCOFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.LearnerEvaluatorConstructionFailedException;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

/**
 * The IMLPlanBuilder provides the general interface of an ML-Plan builder independent
 * of the problem domain or specific library that is used for the configuration of machine
 * learning pipelines.
 *
 * @author mwever
 * @author fmohr
 *
 */
public interface IMLPlanBuilder<L extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<?>>, B extends IMLPlanBuilder<L, B>> {

	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter();

	public PipelineEvaluator getClassifierEvaluationInSearchPhase(ILabeledDataset<?> dataShownToSearch, int randomSeed, int size) throws LearnerEvaluatorConstructionFailedException;

	public PipelineEvaluator getClassifierEvaluationInSelectionPhase(ILabeledDataset<?> dataShownToSearch, int randomSeed) throws LearnerEvaluatorConstructionFailedException;

	public ISupervisedLearnerMetric getPerformanceMeasure();

	public String getRequestedInterface();

	public File getSearchSpaceConfigFile();

	public ILearnerFactory<L> getLearnerFactory();

	public HASCOFactory<GraphSearchWithPathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> getHASCOFactory();

	public MLPlanClassifierConfig getAlgorithmConfig();

	public void prepareNodeEvaluatorInFactoryWithData(ILabeledDataset<?> data);

	public boolean getUseCache();

	public B getSelf();
}
