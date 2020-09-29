package ai.libs.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.IDataConfigurable;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPredictionPerformanceMetricConfigurable;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.control.IRandomConfigurable;
import org.slf4j.Logger;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsBuilder;
import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.LearnerEvaluatorConstructionFailedException;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.safeguard.IEvaluationSafeGuard;
import ai.libs.mlplan.safeguard.IEvaluationSafeGuardFactory;

/**
 * The methods in this util are mainly used in the MLPlan algorithm itself but outsourced in order to improve readability and testability.
 *
 * @author Felix Mohr
 *
 */
abstract class MLPlanUtil {

	private MLPlanUtil() {
		/* avoid instantiation */
	}

	public static Pair<ILabeledDataset<?>, ILabeledDataset<?>> getDataForSearchAndSelection(final ILabeledDataset<?> dataset, final double dataPortionUsedForSelection, final Random random,
			final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> splitter, final Logger logger) throws InterruptedException, AlgorithmException {
		ILabeledDataset<?> dataShownToSearch;
		ILabeledDataset<?> dataShownToSelection;
		if (dataPortionUsedForSelection > 0) {
			try {
				if (splitter == null) {
					throw new IllegalArgumentException("The builder does not specify a dataset splitter for the separation between search and selection phase data.");
				}
				logger.debug("Splitting given {} data points into search data ({}%) and selection data ({}%) with splitter {}.", dataset.size(), MathExt.round((1 - dataPortionUsedForSelection) * 100, 2),
						MathExt.round(dataPortionUsedForSelection * 100, 2), splitter.getClass().getName());
				if (splitter instanceof ILoggingCustomizable) {
					((ILoggingCustomizable) splitter).setLoggerName(logger.getName() + ".searchselectsplitter");
				}
				List<ILabeledDataset<?>> split = splitter.split(dataset, random, dataPortionUsedForSelection);
				final int expectedSearchSize = (int) Math.round(dataset.size() * (1 - dataPortionUsedForSelection)); // attention; this is a bit tricky (data portion for selection is in 0)
				final int expectedSelectionSize = dataset.size() - expectedSearchSize;
				if (Math.abs(expectedSearchSize - split.get(1).size()) > 1 || Math.abs(expectedSelectionSize - split.get(0).size()) > 1) {
					throw new IllegalStateException("Invalid split produced by " + splitter.getClass().getName() + "! Split sizes are " + split.get(1).size() + "/" + split.get(0).size() + " but expected sizes were " + expectedSearchSize
							+ "/" + expectedSelectionSize);
				}
				dataShownToSearch = split.get(1); // attention; this is a bit tricky (data portion for selection is in 0)
				dataShownToSelection = dataset;
				logger.debug("Search/Selection split completed. Using {} data points in search and {} in selection.", dataShownToSearch.size(), dataShownToSelection.size());
			} catch (SplitFailedException e) {
				throw new AlgorithmException("Error in ML-Plan execution.", e);
			}
		} else {
			dataShownToSearch = dataset;
			dataShownToSelection = null;
			logger.debug("Selection phase de-activated. Not splitting the data and giving everything to the search.");
		}
		if (dataShownToSearch.isEmpty()) {
			throw new IllegalStateException("Cannot search on no data.");
		}
		if (dataShownToSelection != null && dataShownToSelection.size() < dataShownToSearch.size()) {
			throw new IllegalStateException("The search data (" + dataShownToSearch.size() + " data points) are bigger than the selection data (" + dataShownToSelection.size() + " data points)!");
		}
		return new Pair<>(dataShownToSearch, dataShownToSelection);
	}

	public static Pair<PipelineEvaluator, PipelineEvaluator> getPipelineEvaluators(final ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluatorFactoryForSearch,
			final IDeterministicPredictionPerformanceMeasure<?, ?> metricForSearch, final ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluatorFactoryForSelection,
			final IDeterministicPredictionPerformanceMeasure<?, ?> metricForSelection, final Random random, final ILabeledDataset<?> dataShownToSearch, final ILabeledDataset<?> dataShownToSelection,
			final IEvaluationSafeGuardFactory safeGuardFactory, final ILearnerFactory<? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> learnerFactory, final Timeout timeoutForCandidateEvaluation)
					throws InterruptedException, AlgorithmException, LearnerEvaluatorConstructionFailedException {

		/* set random source and data for the evaluator factories */
		if (evaluatorFactoryForSearch instanceof IPredictionPerformanceMetricConfigurable) {
			((IPredictionPerformanceMetricConfigurable) evaluatorFactoryForSearch).setMeasure(metricForSearch);
		}
		if (evaluatorFactoryForSearch instanceof IRandomConfigurable) {
			((IRandomConfigurable) evaluatorFactoryForSearch).setRandom(random);
		}
		if (evaluatorFactoryForSearch instanceof IDataConfigurable) {
			((IDataConfigurable) evaluatorFactoryForSearch).setData(dataShownToSearch);
		}
		if (evaluatorFactoryForSelection instanceof IPredictionPerformanceMetricConfigurable) {
			((IPredictionPerformanceMetricConfigurable) evaluatorFactoryForSelection).setMeasure(metricForSelection);
		}
		if (evaluatorFactoryForSelection instanceof IRandomConfigurable) {
			((IRandomConfigurable) evaluatorFactoryForSelection).setRandom(random);
		}
		if (evaluatorFactoryForSelection instanceof IDataConfigurable && dataShownToSelection != null) {
			((IDataConfigurable) evaluatorFactoryForSelection).setData(dataShownToSelection);
		}

		/* create pipeline evaluator for search phase */
		ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<?>> searchEvaluator = evaluatorFactoryForSearch.getLearnerEvaluator();
		PipelineEvaluator classifierEvaluatorForSearch = new PipelineEvaluator(learnerFactory, searchEvaluator, timeoutForCandidateEvaluation);
		if (safeGuardFactory != null) {
			safeGuardFactory.withEvaluator(searchEvaluator);
			try {
				IEvaluationSafeGuard safeGuard = safeGuardFactory.build();
				classifierEvaluatorForSearch.setSafeGuard(safeGuard);
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				throw new AlgorithmException("Could not build safe guard.", e);
			}
		}

		/* create pipeline evaluator for selection phase */
		PipelineEvaluator classifierEvaluatorForSelection = dataShownToSelection != null ? new PipelineEvaluator(learnerFactory, evaluatorFactoryForSelection.getLearnerEvaluator(), timeoutForCandidateEvaluation) : null;
		return new Pair<>(classifierEvaluatorForSearch, classifierEvaluatorForSelection);
	}

	public static HASCOViaFDAndBestFirstWithRandomCompletionsBuilder getHASCOBuilder(final MLPlanClassifierConfig algorithmConfig, final ILabeledDataset<?> dataset, final File searchSpaceFile, final String requestedHASCOInterface,
			final Predicate<TFDNode> priorizingPredicate, final List<IPathEvaluator<TFDNode, String, Double>> preferredNodeEvaluators, final PipelineValidityCheckingNodeEvaluator pipelineValidityCheckingNodeEvaluator, final String nameOfMethod1, final String nameOfMethod2) {

		/* compile software composition problem and create the builder */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem;
		try {
			problem = new RefinementConfiguredSoftwareConfigurationProblem<>(searchSpaceFile, requestedHASCOInterface, null);
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid configuration file " + searchSpaceFile, e);
		}
		HASCOViaFDAndBestFirstWithRandomCompletionsBuilder hascoBuilder = HASCOBuilder.get(problem).withBestFirst().withRandomCompletions();

		/* now configure the chain of preferred node evaluators (taking in account that the ones about checking validity and preferred components are the most important one) */
		List<IPathEvaluator<TFDNode, String, Double>> neChain = new ArrayList<>();
		if (pipelineValidityCheckingNodeEvaluator != null) {
			pipelineValidityCheckingNodeEvaluator.setComponents(problem.getComponents());
			pipelineValidityCheckingNodeEvaluator.setData(dataset);
			neChain.add(pipelineValidityCheckingNodeEvaluator);
		}
		if (algorithmConfig.preferredComponents() != null && !algorithmConfig.preferredComponents().isEmpty()) {
			Objects.requireNonNull(nameOfMethod1, "First HASCO method must not be null!");
			Objects.requireNonNull(nameOfMethod2, "Second HASCO method must not be null!");
			neChain.add(new PreferenceBasedNodeEvaluator(problem.getComponents(), algorithmConfig.preferredComponents(), nameOfMethod1, nameOfMethod2));
		}
		neChain.addAll(preferredNodeEvaluators);
		if (!neChain.isEmpty()) {
			IPathEvaluator<TFDNode, String, Double> preferredNodeEvaluator = neChain.remove(0);
			for (IPathEvaluator<TFDNode, String, Double> ne : neChain) {
				preferredNodeEvaluator = new AlternativeNodeEvaluator<>(preferredNodeEvaluator, ne);
			}
			hascoBuilder.withPreferredNodeEvaluator(preferredNodeEvaluator);
		}
		hascoBuilder.withNumSamples(algorithmConfig.numberOfRandomCompletions());
		hascoBuilder.withSeed(algorithmConfig.seed());
		hascoBuilder.withTimeoutForNode(new Timeout(algorithmConfig.timeoutForNodeEvaluation(), TimeUnit.MILLISECONDS));
		hascoBuilder.withTimeoutForSingleEvaluation(new Timeout(algorithmConfig.timeoutForCandidateEvaluation(), TimeUnit.MILLISECONDS));
		hascoBuilder.withPriorizingPredicate(priorizingPredicate);
		return hascoBuilder;
	}
}
