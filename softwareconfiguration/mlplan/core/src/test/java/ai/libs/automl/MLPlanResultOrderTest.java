package ai.libs.automl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.junit.jupiter.api.Test;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.test.MediumTest;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.PipelineValidityCheckingNodeEvaluator;
import ai.libs.mlplan.core.TimeTrackingLearnerWrapper;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;

/**
 * This test checks whether ML-Plan delivers the solutions in the order of preferred components
 *
 * @author Felix Mohr
 *
 */
public abstract class MLPlanResultOrderTest<L extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> extends ATest {

	public abstract AMLPlanBuilder<L, ?> getMLPlanBuilder() throws Exception;

	private MLPlan<L> mlplan;

	private MLPlan<L> prepare(final AMLPlanBuilder<L, ?> builder) throws DatasetDeserializationFailedException, IOException{
		builder.withDataset(OpenMLDatasetReader.deserializeDataset(39));
		List<String> preferredComponents = builder.getPreferredComponents().stream().limit(10).collect(Collectors.toList());
		final AtomicInteger observedIndex = new AtomicInteger(-1);
		builder.withTimeOut(new Timeout(1, TimeUnit.DAYS));
		builder.withPipelineValidityCheckingNodeEvaluator(new PipelineValidityCheckingNodeEvaluator(builder.getComponents(), builder.getDataset()) {

			@Override
			public Double evaluate(final ILabeledPath<TFDNode, String> path) throws PathEvaluationException, InterruptedException {
				return null;
			}
		}); // remove validity checks here in this test to really cover all potentially relevant algorithms
		builder.withSearchPhaseEvaluatorFactory(() -> (p -> {
			String classOfClassifier = ((TimeTrackingLearnerWrapper)p).getComponentInstance().getComponent().getName();
			int indexOfClassifier = preferredComponents.indexOf(classOfClassifier);
			if (indexOfClassifier == observedIndex.get() + 1) {
				observedIndex.incrementAndGet();
				if (observedIndex.get() == preferredComponents.size() - 1) {
					this.mlplan.cancel();
				}
			}
			else {
				assertTrue ("Current index of observed components is " + observedIndex.get() + ", but the component for which an evaluation is requested has index " + indexOfClassifier, indexOfClassifier <= observedIndex.get());
			}
			return 0.5; // evaluate all pipelines to 0.5. This implies that none of them is pursued before the whole first part of the tree has been considered.
		}));
		this.mlplan = builder.build();
		return this.mlplan;
	}

	@Test
	@MediumTest
	public void testThatSolutionsArriveInPreferredOrder() throws Exception {
		MLPlan<L> mlplan = this.prepare(this.getMLPlanBuilder());
		mlplan.setLoggerName("testedalgorithm");
		try {

			/* use this listener to check the return order of solutions */
			mlplan.registerListener(new Object() {

				@Subscribe
				public void receiveSolution(final ClassifierFoundEvent e) {
					MLPlanResultOrderTest.this.logger.info("Observed solution {}", e);
				}
			});
			mlplan.call();
		}
		catch (AlgorithmExecutionCanceledException e) {
			/* this is intentional */
		}
	}

	@Test
	@MediumTest
	public void testThatDefaultConfigurationsAreReturnedFirst() throws Exception {
		MLPlan<L> mlplan = this.prepare(this.getMLPlanBuilder());
		mlplan.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		Set<ComponentInstance> seenUnparametrizedComponents = new HashSet<>();
		try {

			/* use this listener to check the return order of solutions */
			mlplan.registerListener(new Object() {

				@Subscribe
				public void receiveSolution(final ClassifierFoundEvent e) {
					ComponentInstance ci = e.getComponentDescription();
					ComponentInstance unparametrizedPipeline = ComponentInstanceUtil.getDefaultParametrization(ci);
					if (!seenUnparametrizedComponents.contains(unparametrizedPipeline)) {
						assertTrue("Component instance " + new ComponentSerialization().serialize(ci) + " is the first of its kind (no other instance of " + new ComponentSerialization().serialize(unparametrizedPipeline) + " has been observed) but not default parametrized!", ComponentInstanceUtil.isDefaultConfiguration(ci));
						seenUnparametrizedComponents.add(unparametrizedPipeline);
					}
				}
			});
			mlplan.call();
		}
		catch (AlgorithmExecutionCanceledException e) {
			/* this is intentional */
		}
	}

}
