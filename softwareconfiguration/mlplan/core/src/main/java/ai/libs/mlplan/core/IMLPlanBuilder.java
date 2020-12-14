package ai.libs.mlplan.core;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDBuilder;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.LearnerEvaluatorConstructionFailedException;
import ai.libs.mlplan.multiclass.IMLPlanClassifierConfig;
import ai.libs.mlplan.safeguard.IEvaluationSafeGuardFactory;

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
	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter();

	/**
	 * This is the factory that will be used to create the pipeline evaluators for evaluation during search time
	 * @return
	 * @throws LearnerEvaluatorConstructionFailedException
	 */
	public ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearnerEvaluationFactoryForSearchPhase();

	public IDeterministicPredictionPerformanceMeasure<?, ?> getMetricForSearchPhase();

	/**
	 * This is the factory that will be used to create the pipeline evaluators for evaluation during selection time
	 * @return
	 * @throws LearnerEvaluatorConstructionFailedException
	 */
	public ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearnerEvaluationFactoryForSelectionPhase();

	public IDeterministicPredictionPerformanceMeasure<?, ?> getMetricForSelectionPhase();

	public String getRequestedInterface();

	public File getSearchSpaceConfigFile();

	public ILearnerFactory<L> getLearnerFactory();

	public HASCOViaFDBuilder<Double, ?> getHASCOFactory();

	public IMLPlanClassifierConfig getAlgorithmConfig();

	public IEvaluationSafeGuardFactory getSafeGuardFactory();

	public double getPortionOfDataReservedForSelectionPhase();

	public B getSelf();
}
