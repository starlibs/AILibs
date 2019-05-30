package de.upb.crc901.automl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.concurrent.GlobalTimer;
import jaicore.interrupt.Interrupter;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;

/**
 * This test tests whether or not the algorithm delivers a solution on each given dataset within 30 seconds.
 *
 * @author fmohr
 *
 */
@RunWith(Parameterized.class)
public abstract class AutoMLAlgorithmResultProductionTester {

	private static final Logger logger = LoggerFactory.getLogger(AutoMLAlgorithmResultProductionTester.class);
	private static final TimeOut timeout = new TimeOut(2, TimeUnit.MINUTES);

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<OpenMLProblemSet[]> data() throws IOException, Exception {
		List<OpenMLProblemSet> problemSets = new ArrayList<>();
		problemSets.add(new OpenMLProblemSet(3)); // kr-vs-kp
		problemSets.add(new OpenMLProblemSet(1150)); // AP_Breast_Lung
		problemSets.add(new OpenMLProblemSet(1156)); // AP_Omentum_Ovary
		//		problemSets.add(new OpenMLProblemSet(1152)); // AP_Prostate_Ovary
		//		problemSets.add(new OpenMLProblemSet(1240)); // AirlinesCodrnaAdult
		problemSets.add(new OpenMLProblemSet(1457)); // amazon
		problemSets.add(new OpenMLProblemSet(1501)); // semeion
		//		problemSets.add(new OpenMLProblemSet(149)); // CovPokElec
		//		problemSets.add(new OpenMLProblemSet(41103)); // cifar-10
		//		problemSets.add(new OpenMLProblemSet(40668)); // connect-4
		problemSets.add(new OpenMLProblemSet(1590)); // adult
		//		problemSets.add(new OpenMLProblemSet(182)); // satimage
		//				problemSets.add(new OpenMLProblemSet(24)); // mushroom
		problemSets.add(new OpenMLProblemSet(39)); // ecoli
		problemSets.add(new OpenMLProblemSet(44)); // spambase
		problemSets.add(new OpenMLProblemSet(60)); // waveform-5000
		problemSets.add(new OpenMLProblemSet(61)); // iris
		problemSets.add(new OpenMLProblemSet(9)); // autos
		//		problemSets.add(new OpenMLProblemSet(1039)); // hiva-agnostic
		//		problemSets.add(new OpenMLProblemSet(1104)); // leukemia
		//		problemSets.add(new OpenMLProblemSet(1101)); // lymphoma_2classes
		problemSets.add(new OpenMLProblemSet(554)); // mnist
		//		problemSets.add(new OpenMLProblemSet(1101)); // lymphoma_2classes
		//		problemSets.add(new OpenMLProblemSet(155)); // pokerhand
		problemSets.add(new OpenMLProblemSet(40691)); // winequality

		OpenMLProblemSet[][] data = new OpenMLProblemSet[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Parameter(0)
	public OpenMLProblemSet problemSet;

	public abstract IAlgorithm<Instances, Classifier> getAutoMLAlgorithm(Instances data);

	@Test
	public void testThatModelIsTrained() throws Exception {
		try {
			assertTrue("There are still jobs on the global timer: " + GlobalTimer.getInstance().getActiveTasks(), GlobalTimer.getInstance().getActiveTasks().isEmpty());
			System.gc();
			assertFalse("The thread should not be interrupted when calling the AutoML-tool!", Thread.currentThread().isInterrupted());

			/* create instances and set attribute */
			logger.info("Loading dataset {} from {} for test.", this.problemSet.getName(), this.problemSet.getDatasetSource().getX());
			File cacheFile = new File("testrsc/openml/" + this.problemSet.getId() + ".arff");
			if (!cacheFile.exists()) {
				logger.info("Cache file does not exist, creating it.");
				cacheFile.getParentFile().mkdirs();
				Instances dataset = this.problemSet.getDatasetSource().getX().getDataSet();
				DataSink.write(cacheFile.getAbsolutePath(), dataset);
			}
			logger.info("Loading ARFF file from cache.");
			Instances dataset = new Instances(new FileReader(cacheFile));
			Attribute targetAttribute = dataset.attribute(this.problemSet.getDatasetSource().getY());
			dataset.setClassIndex(targetAttribute.index());
			String datasetname = dataset.relationName();

			if (false) {
				logger.info("Creating a 70/30 (non-stratified) split over the data");
				int splitIndex = (int)Math.floor(dataset.size() * 0.7);
				Instances train = new Instances(dataset, 0, splitIndex);
				Instances test = new Instances(dataset, splitIndex, dataset.size() - train.size());
				assertEquals(dataset.size(), train.size() + test.size());
			}
			List<Instances> splits = WekaUtil.getStratifiedSplit(dataset, 0, .7);
			Instances train = splits.get(0);
			Instances test = splits.get(1);

			dataset = null;

			/* get algorithm */
			logger.info("Loading the algorithm");
			IAlgorithm<Instances, Classifier> algorithm = this.getAutoMLAlgorithm(train); // AutoML-tools should deliver a classifier
			assert algorithm != null : "The factory method has returned NULL as the algorithm object";
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
			}
			algorithm.setTimeout(timeout);

			/* find classifier */
			Instances data = algorithm.getInput();
			logger.info("Checking that {} delivers a model on dataset {}", algorithm.getId(), datasetname);
			Classifier c = algorithm.call();
			assertFalse("The thread should not be interrupted after calling the AutoML-tool!", Thread.currentThread().isInterrupted());
			logger.info("Identified classifier {} as solution to the problem.", WekaUtil.getClassifierDescriptor(c));
			assertNotNull("The algorithm as not returned any classifier.", c);

			/* compute error rate */
			Evaluation eval = new Evaluation(train);
			eval.evaluateModel(c, test);
			assertTrue("At least 10 instances must be classified!", test.size() >= 10);
			assertTrue("There are still jobs on the global timer: " + GlobalTimer.getInstance().getActiveTasks(), GlobalTimer.getInstance().getActiveTasks().isEmpty());
			logger.info("Error rate of solution {} on {} is: {}", c.getClass().getName(), datasetname, eval.errorRate());
		}
		catch (AlgorithmTimeoutedException e) {
			fail("No solution was found in the given timeout. Stack trace: " + Arrays.stream(e.getStackTrace()).map(se -> "\n\t" + se.toString()).collect(Collectors.joining()));
		}
		finally {
			logger.info("Cleaning up everything ...");
			GlobalTimer.getInstance().getActiveTasks().forEach(t -> {
				logger.info("Canceling task {}", t);
				t.cancel();
				Thread.interrupted(); // reset interrupted flag
			});
			Interrupter interrupter = Interrupter.get();
			synchronized (interrupter) {
				new ArrayList<>(interrupter.getAllUnresolvedInterrupts()).forEach(i -> {
					logger.warn("Interrupt reason {} for thread {} has not been resolved cleanly. Clearing it up.", i.getReasonForInterruption(), i.getInterruptedThread());
					interrupter.markInterruptAsResolved(i.getInterruptedThread(), i.getReasonForInterruption());
				});
				assert interrupter.getAllUnresolvedInterrupts().isEmpty() : "Interrupter still has list of unresolved interrupts!";
			}
			if (Thread.currentThread().isInterrupted()) {
				logger.error("Interrupt-flag of executing thread {} is set to TRUE!", Thread.currentThread());
			}
			assert !Thread.currentThread().isInterrupted() : "Thread is interrupted, which must not be the case!";
		}
	}
}
