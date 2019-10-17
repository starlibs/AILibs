package ai.libs.mlplan.core;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.hasco.core.HASCOFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ClassifierEvaluatorConstructionFailedException;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.mlpipeline_evaluation.PerformanceDBAdapter;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.multiclass.wekamlplan.ILearnerFactory;

/**
 * The IMLPlanBuilder provides the general interface of an ML-Plan builder independent
 * of the problem domain or specific library that is used for the configuration of machine
 * learning pipelines.
 *
 * @author mwever
 *
 */
public interface IMLPlanBuilder<I extends ILabeledInstance, D extends ILabeledDataset<I>, L extends ISupervisedLearner<I, D>, B extends IMLPlanBuilder<I, D, L, B>> {

	public IFoldSizeConfigurableRandomDatasetSplitter<D> getSearchSelectionDatasetSplitter();

	public PipelineEvaluator getClassifierEvaluationInSearchPhase(D dataShownToSearch, int randomSeed, int size) throws ClassifierEvaluatorConstructionFailedException;

	public PipelineEvaluator getClassifierEvaluationInSelectionPhase(D dataShownToSearch, int randomSeed) throws ClassifierEvaluatorConstructionFailedException;

	public String getPerformanceMeasureName();

	public String getRequestedInterface();

	public File getSearchSpaceConfigFile();

	public ILearnerFactory<L> getLearnerFactory();

	public HASCOFactory<GraphSearchWithPathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> getHASCOFactory();

	public MLPlanClassifierConfig getAlgorithmConfig();

	public void prepareNodeEvaluatorInFactoryWithData(D data);

	public PerformanceDBAdapter getDBAdapter();

	public boolean getUseCache();

	public B getSelf();
}
