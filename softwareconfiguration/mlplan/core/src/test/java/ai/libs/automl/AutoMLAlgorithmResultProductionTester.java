package ai.libs.automl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.IClassifierEvaluator;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.Tester;
import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.concurrent.GlobalTimer;
import ai.libs.jaicore.interrupt.Interrupter;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.DatasetUtil;
import ai.libs.jaicore.ml.core.evaluation.evaluator.PreTrainedPredictionBasedClassifierEvaluator;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;

/**
 * This test tests whether or not the algorithm delivers a solution on each given dataset within 30 seconds.
 *
 * @author fmohr
 *
 */
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AutoMLAlgorithmResultProductionTester extends Tester {

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<OpenMLProblemSet[]> data() throws DatasetDeserializationFailedException {
		try {
			List<OpenMLProblemSet> problemSets = new ArrayList<>();
			//			problemSets.add(new OpenMLProblemSet(3)); // kr-vs-kp l�ppt
			//			problemSets.add(new OpenMLProblemSet(9)); // autos l�ppt
			//			problemSets.add(new OpenMLProblemSet(24)); // mushroom l�ppt
			//			problemSets.add(new OpenMLProblemSet(39)); // ecoli l�ppt
			//			problemSets.add(new OpenMLProblemSet(44)); // spambase l�ppt
			//			problemSets.add(new OpenMLProblemSet(60)); // waveform-5000 l�ppt
			//			problemSets.add(new OpenMLProblemSet(61)); // iris l�ppt
			//			//			problemSets.add(new OpenMLProblemSet(149)); // CovP okElec fail
			//			//			problemSets.add(new OpenMLProblemSet(155)); // pokerhand fail
			//			problemSets.add(new OpenMLProblemSet(182)); // satimage fail
			//			problemSets.add(new OpenMLProblemSet(273)); // IMDB drama fail
			//			//			problemSets.add(new OpenMLProblemSet(554)); // mnist fail
			//			problemSets.add(new OpenMLProblemSet(1039)); // hiva-agnostic fail
			//			problemSets.add(new OpenMLProblemSet(1101)); // lymphoma_2classes l�ppt
			//			problemSets.add(new OpenMLProblemSet(1104)); // leukemia l�ppt
			//			problemSets.add(new OpenMLProblemSet(1150)); // AP_Breast_Lung fail
			//			problemSets.add(new OpenMLProblemSet(1152)); // AP_Prostate_Ovary l�ppt
			//			problemSets.add(new OpenMLProblemSet(1156)); // AP_Omentum_Ovary l�ppt
			//			problemSets.add(new OpenMLProblemSet(1240)); // AirlinesCodrnaAdult
			problemSets.add(new OpenMLProblemSet(1457)); // amazon
			//			problemSets.add(new OpenMLProblemSet(1501)); // semeion
			//			problemSets.add(new OpenMLProblemSet(1590)); // adult
			//			problemSets.add(new OpenMLProblemSet(4136)); // dexter
			//			problemSets.add(new OpenMLProblemSet(4137)); // dorothea
			//			problemSets.add(new OpenMLProblemSet(40668)); // connect-4
			//			problemSets.add(new OpenMLProblemSet(40691)); // winequality
			//			problemSets.add(new OpenMLProblemSet(40927)); // cifar-10
			//			problemSets.add(new OpenMLProblemSet(41026)); // gisette
			//			problemSets.add(new OpenMLProblemSet(41065)); // mnist-rotate
			//			problemSets.add(new OpenMLProblemSet(41066)); // secom

			OpenMLProblemSet[][] data = new OpenMLProblemSet[problemSets.size()][1];
			for (int i = 0; i < data.length; i++) {
				data[i][0] = problemSets.get(i);
			}
			return Arrays.asList(data);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	@Parameter(0)
	public OpenMLProblemSet problemSet;

	public abstract IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> getAutoMLAlgorithm(ILabeledDataset<?> data) throws AlgorithmCreationException, IOException;

	public void afterInitHook(final IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> algorithm) {

	}

	@Test
	public void testThatModelIsTrained()
			throws DatasetDeserializationFailedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, ObjectEvaluationFailedException, SplitFailedException, AlgorithmCreationException, IOException {
		try {
			assertTrue("There are still jobs on the global timer: " + GlobalTimer.getInstance().getActiveTasks(), GlobalTimer.getInstance().getActiveTasks().isEmpty());
			assertFalse("The thread should not be interrupted when calling the AutoML-tool!", Thread.currentThread().isInterrupted());

			/* load dataset and get splits */
			this.logger.info("Loading dataset {} for test.", this.problemSet.getName());
			ILabeledDataset<?> dataset = this.problemSet.getDataset();
			if (dataset.getLabelAttribute() instanceof INumericAttribute) {
				this.logger.info("Changing regression dataset to classification dataset!");
				dataset = DatasetUtil.convertToClassificationDataset(dataset);
			}
			String datasetname = this.problemSet.getName();
			List<ILabeledDataset<?>> trainTestSplit = SplitterUtil.getLabelStratifiedTrainTestSplit(dataset, new Random(0), .7);
			ILabeledDataset<?> train = trainTestSplit.get(0);
			ILabeledDataset<?> test = trainTestSplit.get(1);
			if (train.getNumAttributes() != test.getNumAttributes()) {
				throw new IllegalStateException();
			}

			/* get algorithm */
			this.logger.info("Loading the algorithm");
			IAlgorithm<ILabeledDataset<?>, ? extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>> algorithm = this.getAutoMLAlgorithm(train); // AutoML-tools should deliver a classifier
			algorithm.setNumCPUs(1);

			assert algorithm != null : "The factory method has returned NULL as the algorithm object";
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
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
			assertTrue("Algorithm timeout violated. Runtime was " + runtime + ", but configured timeout was " + algorithm.getTimeout(), runtime < algorithm.getTimeout().milliseconds());
			assertFalse("The thread should not be interrupted after calling the AutoML-tool!", Thread.currentThread().isInterrupted());
			this.logger.info("Identified classifier {} as solution to the problem.", c);
			assertNotNull("The algorithm as not returned any classifier.", c);

			/* free memory */
			dataset = null;
			this.problemSet = null;

			/* compute error rate */
			assertTrue("At least 10 instances must be classified!", test.size() >= 10);
			IClassifierEvaluator evaluator = new PreTrainedPredictionBasedClassifierEvaluator(test, EClassificationPerformanceMeasure.ERRORRATE);
			double score = evaluator.evaluate(c);
			Thread.sleep(algorithm.getTimeout().seconds() / 20);
			assertTrue("There are still jobs on the global timer: " + GlobalTimer.getInstance().getActiveTasks(), GlobalTimer.getInstance().getActiveTasks().isEmpty());
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
