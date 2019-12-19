package ai.libs.mlplan.core;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.hasco.core.HASCOFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
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
public interface IMLPlanBuilder<L extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>, B extends IMLPlanBuilder<L, B>> {

	/**
	 * This is the splitter that splits the given input data into data for the search phase and for the selection phase
	 * @return
	 */
	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<? extends ILabeledInstance>> getSearchSelectionDatasetSplitter();

	/**
	 * This is the factory that will be used to create the pipeline evaluators for evaluation during search time
	 * @return
	 * @throws LearnerEvaluatorConstructionFailedException
	 */
	public ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>  getLearnerEvaluationFactoryForSearchPhase();

	/**
	 * This is the factory that will be used to create the pipeline evaluators for evaluation during selection time
	 * @return
	 * @throws LearnerEvaluatorConstructionFailedException
	 */
	public ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>  getLearnerEvaluationFactoryForSelectionPhase();

	public String getRequestedInterface();

	public File getSearchSpaceConfigFile();

	public ILearnerFactory<L> getLearnerFactory();

	public HASCOFactory<GraphSearchWithPathEvaluationsInput<TFDNode, String, Double>, TFDNode, String, Double> getHASCOFactory();

	public MLPlanClassifierConfig getAlgorithmConfig();

	public double getPortionOfDataReservedForSelectionPhase();

	public B getSelf();
}
