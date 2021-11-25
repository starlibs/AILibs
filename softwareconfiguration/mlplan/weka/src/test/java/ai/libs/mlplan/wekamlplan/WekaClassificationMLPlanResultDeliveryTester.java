package ai.libs.mlplan.wekamlplan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;

import com.google.common.eventbus.Subscribe;

import ai.libs.automl.AutoMLAlgorithmForClassificationResultProductionTester;
import ai.libs.hasco.core.events.TwoPhaseHASCOSelectionPhaseSkippedEvent;
import ai.libs.hasco.core.events.TwoPhaseHASCOSolutionEvaluationEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;
import ai.libs.mlplan.weka.weka.WekaPipelineValidityCheckingNodeEvaluator;

public class WekaClassificationMLPlanResultDeliveryTester extends AutoMLAlgorithmForClassificationResultProductionTester {

	private boolean selectionPhaseSkippedEventObserved = false;
	private List<ClassifierFoundEvent> searchPhaseEvents = new ArrayList<>();
	private List<TwoPhaseHASCOSolutionEvaluationEvent> selectionPhaseEvents = new ArrayList<>();

	@Override
	public IAlgorithm<ILabeledDataset<?>, IWekaClassifier> getAutoMLAlgorithm(final ILabeledDataset<?> data) throws AlgorithmCreationException {
		try {
			this.logger.info("Creating ML-Plan instance.");
			MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
			int baseTime = Math.max(5, (int) Math.ceil(1.2 * this.getTrainTimeOfMajorityClassifier(data) / 1000.0));
			assertTrue(baseTime < 60, "The majority classifier already needs too much time: " + baseTime);
			Timeout totalTimeout = new Timeout(Math.min(90, (data.size() + data.getNumAttributes()) / 1000 + 10 * baseTime), TimeUnit.SECONDS);
			builder.withTimeOut(totalTimeout); // time out at most 90 seconds
			builder.withCandidateEvaluationTimeOut(new Timeout(totalTimeout.seconds() / 2, TimeUnit.SECONDS));
			builder.withNodeEvaluationTimeOut(new Timeout(totalTimeout.seconds(), TimeUnit.SECONDS));

			MLPlan<IWekaClassifier> mlplan = builder.withDataset(data).build();
			this.logger.info("Built of ML-Plan complete");
			return mlplan;
		} catch (IOException | TrainingException | DatasetDeserializationFailedException e) {
			throw new AlgorithmCreationException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // re-interrupt
			fail("Thread has been interrupted.");
			return null;
		}
	}

	@Override
	public void afterInitHook(final IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> algorithm) {
		MLPlan<IWekaClassifier> mlplan = (MLPlan<IWekaClassifier>) algorithm;
		BestFirst<?, ?, ?, ?> bf = ((BestFirst<?, ?, ?, ?>) mlplan.getSearch());
		assertNotNull(bf);
		IPathEvaluator<?, ?, ?> pe = bf.getNodeEvaluator();
		assertTrue(pe instanceof AlternativeNodeEvaluator, "Expecting the node evaluator to be an alternative node evaluator, but is of type: " + pe.getClass());
		AlternativeNodeEvaluator<?, ?, ?> ape = (AlternativeNodeEvaluator<?, ?, ?>) pe;
		while (ape.getPrimaryNodeEvaluator() instanceof AlternativeNodeEvaluator<?, ?, ?>) {
			ape = (AlternativeNodeEvaluator<?, ?, ?>) ape.getPrimaryNodeEvaluator();
		}
		assertTrue(ape.getPrimaryNodeEvaluator() instanceof WekaPipelineValidityCheckingNodeEvaluator,
				"The ML-Plan searcher uses a node evaluator that does not use " + WekaPipelineValidityCheckingNodeEvaluator.class.getName() + " as its eventual primary node evaluator. The evaluator is: " + ape);

		/* reset received events for solutions */
		this.searchPhaseEvents.clear();
		this.selectionPhaseEvents.clear();
		this.selectionPhaseSkippedEventObserved = false;

		mlplan.registerListener(new Object() {

			@Subscribe
			public void receiveSolutionEvent(final ClassifierFoundEvent e) {
				WekaClassificationMLPlanResultDeliveryTester.this.searchPhaseEvents.add(e);
			}

			@Subscribe
			public void receiveSelectionPhaseSkippedEvent(final TwoPhaseHASCOSelectionPhaseSkippedEvent e) {
				WekaClassificationMLPlanResultDeliveryTester.this.selectionPhaseSkippedEventObserved = true;
			}
		});
		mlplan.getOptimizingFactory().registerListener(new Object() {

			@Subscribe
			public void receivePhase2SolutionEvaluationEvent(final TwoPhaseHASCOSolutionEvaluationEvent e) {
				WekaClassificationMLPlanResultDeliveryTester.this.selectionPhaseEvents.add(e);
			}
		});
	}

	@Override
	public void afterCompletionHook(final IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> algorithm) {
		MLPlan<IWekaClassifier> mlplan = (MLPlan<IWekaClassifier>) algorithm;
		IComponentInstance selectedSolution = mlplan.getComponentInstanceOfSelectedClassifier();

		/* get best solution found in search phase */
		Optional<ClassifierFoundEvent> eventOfBestClassifier = this.searchPhaseEvents.stream().min((e1, e2) -> Double.compare(e1.getScore(), e2.getScore()));
		assertTrue(eventOfBestClassifier.isPresent(), "No solution was communicated in the search phase.");

		/* if the selection phase was not indicated to have been skipped, check behavior for that phase */
		if (!this.selectionPhaseSkippedEventObserved) {

			/* check that the best solution of phase 1 was evaluated in phase 2 */
			assertFalse(this.selectionPhaseEvents.isEmpty(), "No solution was successfully evaluated in the selection phase!");
			assertTrue(this.selectionPhaseEvents.stream().anyMatch(e -> e.getComponentInstance().equals(eventOfBestClassifier.get().getComponentDescription())), "The best solution of phase 1 was not evaluated in the selection phase!");

			/* make sure that the selected solution was evaluated in the selection phase */
			assertTrue(this.selectionPhaseEvents.stream().anyMatch(e -> e.getComponentInstance().equals(selectedSolution)), "ML-Plan selected a pipeline that was not executed successfully in the selection phase!");
		}

		/* otherwise check that the best solution of phase 1 was chosen */
		else {
			assertEquals(selectedSolution, eventOfBestClassifier.get().getComponentDescription(), "The best solution of phase 1 was not selected even though not selection phase was run!");
		}
	}

	public int getTrainTimeOfMajorityClassifier(final ILabeledDataset<?> data) throws TrainingException, InterruptedException, DatasetDeserializationFailedException {
		long start = System.currentTimeMillis();
		new MajorityClassifier().fit(data);
		return (int) (System.currentTimeMillis() - start);
	}
}
