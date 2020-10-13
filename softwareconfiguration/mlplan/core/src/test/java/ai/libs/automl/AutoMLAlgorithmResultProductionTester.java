package ai.libs.automl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.IClassifierEvaluator;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.awaitility.Awaitility;
import org.junit.FixMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runners.MethodSorters;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.concurrent.GlobalTimer;
import ai.libs.jaicore.interrupt.Interrupter;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.evaluation.evaluator.PreTrainedPredictionBasedClassifierEvaluator;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;
import ai.libs.jaicore.test.LongTest;

/**
 * This test tests whether or not the algorithm delivers a solution on each given dataset within 30 seconds.
 *
 * @author fmohr
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AutoMLAlgorithmResultProductionTester extends ATest {

	public abstract IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> getAutoMLAlgorithm(ILabeledDataset<?> data) throws AlgorithmCreationException, IOException;

	public abstract List<ILabeledDataset<?>> getTrainTestSplit(ILabeledDataset<?> dataset) throws SplitFailedException, InterruptedException;

	public abstract IDeterministicPredictionPerformanceMeasure<?, ?> getTestMeasure();

	public void afterInitHook(final IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> algorithm) {

	}

	@LongTest
	@ParameterizedTest(name="Test that ML-Plan delivers a model on {0}")
	@MethodSource("getDatasets")
	public void testThatModelIsTrained(final OpenMLProblemSet problemSet)
			throws DatasetDeserializationFailedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, ObjectEvaluationFailedException, SplitFailedException, AlgorithmCreationException, IOException {
		try {
			assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty(), "There are still jobs on the global timer: " + GlobalTimer.getInstance().getActiveTasks());
			assertFalse(Thread.currentThread().isInterrupted(), "The thread should not be interrupted when calling the AutoML-tool!");

			/* load dataset and get splits */
			this.logger.info("Loading dataset {} for test.", problemSet.getName());
			String datasetname = problemSet.getName();
			List<ILabeledDataset<?>> trainTestSplit = this.getTrainTestSplit(problemSet.getDataset());
			ILabeledDataset<?> train = trainTestSplit.get(0);
			ILabeledDataset<?> test = trainTestSplit.get(1);
			test.removeIf(i -> i.getLabel() == null);
			if (train.getNumAttributes() != test.getNumAttributes()) {
				throw new IllegalStateException();
			}

			/* get algorithm */
			this.logger.info("Loading the algorithm");
			IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> algorithm = this.getAutoMLAlgorithm(train); // AutoML-tools should deliver a classifier
			algorithm.setNumCPUs(1);

			assert algorithm != null : "The factory method has returned NULL as the algorithm object";
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
				this.logger.info("Setting logger of the AutoML tool {} to \"{}\"", algorithm.getClass(), ((ILoggingCustomizable) algorithm).getLoggerName());
			}

			/* find classifier */
			this.logger.info("Checking that {} delivers a model on dataset {}", algorithm.getId(), datasetname);
			long start = System.currentTimeMillis();
			algorithm.registerListener(new Object() {

				@Subscribe
				public void receiveInitEvent(final AlgorithmInitializedEvent e) {
					AutoMLAlgorithmResultProductionTester.this.afterInitHook(algorithm);
				}
			});
			ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> c = algorithm.call();
			long runtime = System.currentTimeMillis() - start;
			assertTrue(runtime < algorithm.getTimeout().milliseconds(), "Algorithm timeout violated. Runtime was " + runtime + ", but configured timeout was " + algorithm.getTimeout());
			assertFalse(Thread.currentThread().isInterrupted(), "The thread should not be interrupted after calling the AutoML-tool!");
			this.logger.info("Identified classifier {} as solution to the problem.", c);
			assertNotNull(c, "The algorithm as not returned any classifier.");

			/* free memory */
			trainTestSplit = null;

			/* compute error rate */
			assertTrue(test.size() >= 10, "At least 10 instances must be classified!");
			IClassifierEvaluator evaluator = new PreTrainedPredictionBasedClassifierEvaluator(test, this.getTestMeasure());
			double score = evaluator.evaluate(c);
			Awaitility.await().atLeast(Duration.ofSeconds(algorithm.getTimeout().seconds() / 20));
			assertTrue(GlobalTimer.getInstance().getActiveTasks().isEmpty(), "There are still jobs on the global timer: " + GlobalTimer.getInstance().getActiveTasks());
			this.logger.info("Error rate of solution {} ({}) on {} is: {}", c.getClass().getName(), c, datasetname, score);
		} catch (AlgorithmTimeoutedException e) {
			fail("No solution was found in the given timeout. Stack trace: " + Arrays.stream(e.getStackTrace()).map(se -> "\n\t" + se.toString()).collect(Collectors.joining()));
		} finally {
			this.logger.info("Cleaning up everything ...");
			GlobalTimer.getInstance().getActiveTasks().forEach(t -> {
				this.logger.info("Canceling task {}", t);
				t.cancel();
				Thread.interrupted(); // reset interrupted flag
			});
			Interrupter interrupter = Interrupter.get();
			synchronized (interrupter) {
				new ArrayList<>(interrupter.getAllUnresolvedInterrupts()).forEach(i -> {
					this.logger.warn("Interrupt reason {} for thread {} has not been resolved cleanly. Clearing it up.", i.getReasonForInterruption(), i.getInterruptedThread());
					interrupter.markInterruptAsResolved(i.getInterruptedThread(), i.getReasonForInterruption());
				});
				assert interrupter.getAllUnresolvedInterrupts().isEmpty() : "Interrupter still has list of unresolved interrupts!";
			}
			if (Thread.currentThread().isInterrupted()) {
				this.logger.error("Interrupt-flag of executing thread {} is set to TRUE!", Thread.currentThread());
			}
			assert !Thread.currentThread().isInterrupted() : "Thread is interrupted, which must not be the case!";
		}
	}
}
