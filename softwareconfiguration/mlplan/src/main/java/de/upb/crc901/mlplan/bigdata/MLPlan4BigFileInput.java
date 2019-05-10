package de.upb.crc901.mlplan.bigdata;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.core.events.ClassifierCreatedEvent;
import hasco.model.ComponentInstance;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.StatisticsUtil;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.ml.core.dataset.sampling.infiles.ReservoirSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.SimpleRandomSamplingFactory;
import jaicore.ml.evaluation.evaluators.weka.events.MCCVSplitEvaluationEvent;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolatedEvent;
import jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawExtrapolationMethod;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This is a version of ML-Plan that tries to cope with medium sized data in the sense of big files.
 * That is, the data is still enough to be organized in a single file such that no streaming is required.
 * The data is, however, in general too large to be entirely loaded into memory.
 *
 * We use simple sampling to create a relatively small subset of the data, then run info gain, and then ML-Plan with
 * learning curve prediction.
 *
 * @author fmohr
 *
 */
public class MLPlan4BigFileInput extends AAlgorithm<File, Classifier> implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(MLPlan4BigFileInput.class);

	private File intermediateSizeDownsampledFile = new File("testrsc/sampled/intermediate/" + this.getInput().getName());

	private final int[] anchorpointsTraining = new int[] { 8, 16, 64, 128 };
	private Map<Classifier, ComponentInstance> classifier2modelMap = new HashMap<>();
	private Map<ComponentInstance, int[]> trainingTimesDuringSearch = new HashMap<>();
	private Map<ComponentInstance, List<Integer>> trainingTimesDuringSelection = new HashMap<>();
	private int numTrainingInstancesUsedInSelection;
	private MLPlan mlplan;

	public MLPlan4BigFileInput(final File input) {
		super(input);
	}

	private void downsampleData(final File from, final File to, final int size) throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		ReservoirSampling sampler = new ReservoirSampling(new Random(0), this.getInput());
		try {
			File outputFolder = to.getParentFile();
			if (!outputFolder.exists()) {
				this.logger.info("Creating data output folder {}", outputFolder.getAbsolutePath());
				outputFolder.mkdirs();
			}
			this.logger.info("Starting sampler {} for data source {}", sampler.getClass().getName(), from.getAbsolutePath());
			sampler.setOutputFileName(to.getAbsolutePath());
			sampler.setSampleSize(size);
			sampler.call();
			this.logger.info("Reduced dataset size to {}", size);
		} catch (IOException e) {
			throw new AlgorithmException(e, "Could not create a sub-sample of the given data.");
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case created:

			/* first create an intermediate sized downsampled file (10k instances), which is the basis for further operations */
			this.downsampleData(this.getInput(), this.intermediateSizeDownsampledFile, 10000);

			/* down-sample the intermediate sized input data again for ML-Plan */
			File downsampledFile = new File("testrsc/sampled/" + this.getInput().getName());
			this.downsampleData(this.intermediateSizeDownsampledFile, downsampledFile, 1000);
			if (!downsampledFile.exists()) {
				throw new AlgorithmException("The file " + downsampledFile.getAbsolutePath() + " that should be used for ML-Plan does not exist!");
			}
			Instances data;
			try {
				data = new Instances(new FileReader(downsampledFile));
				data.setClassIndex(data.numAttributes() - 1);
				this.logger.info("Loaded {}x{} dataset", data.size(), data.numAttributes());
			} catch (IOException e) {
				throw new AlgorithmException(e, "Could not create a sub-sample of the given data.");
			}

			/* apply ML-Plan to reduced data */
			MLPlanWekaBuilder builder;
			try {
				builder = AbstractMLPlanBuilder.forWeka();
				builder.withLearningCurveExtrapolationEvaluation(this.anchorpointsTraining, new SimpleRandomSamplingFactory<>(), .7, new InversePowerLawExtrapolationMethod());
				builder.withNodeEvaluationTimeOut(new TimeOut(15, TimeUnit.MINUTES));
				builder.withCandidateEvaluationTimeOut(new TimeOut(5, TimeUnit.MINUTES));
				this.mlplan = new MLPlan(builder, data);
				this.mlplan.setLoggerName(this.getLoggerName() + ".mlplan");
				this.mlplan.registerListener(this);
				this.mlplan.setTimeout(new TimeOut(this.getTimeout().seconds() - 30, TimeUnit.SECONDS));
				this.mlplan.setNumCPUs(8);
				this.mlplan.setBuildSelectedClasifierOnGivenData(false); // we will build the classifier, ML-Plan should not waste time with this
				this.logger.info("ML-Plan initialized, activation finished!");
				return this.activate();
			} catch (IOException e) {
				throw new AlgorithmException(e, "Could not initialize ML-Plan!");
			}
		case active:

			/* run ML-Plan */
			this.logger.info("Starting ML-Plan.");
			this.mlplan.call();
			this.logger.info("ML-Plan has finished. Selected classifier is {} with observed internal performance {}. Will now try to determine the portion of training data that may be used for final training.",
					this.mlplan.getSelectedClassifier(), this.mlplan.getInternalValidationErrorOfSelectedClassifier());

			/* fit regression model to estimate the runtime behavior of the selected classifier */
			int[] trainingTimesDuringSearch = this.trainingTimesDuringSearch.get(this.mlplan.getComponentInstanceOfSelectedClassifier());
			List<Integer> trainingTimesDuringSelection = this.trainingTimesDuringSelection.get(this.mlplan.getComponentInstanceOfSelectedClassifier());
			this.logger.info("Observed training times of selected classifier: {} (search) and {} (selection on {} training instances)", Arrays.toString(trainingTimesDuringSearch), trainingTimesDuringSelection,
					this.numTrainingInstancesUsedInSelection);
			Instances observedRuntimeData = this.getTrainingTimeInstancesForClassifier(this.mlplan.getComponentInstanceOfSelectedClassifier());
			this.logger.info("Infered the following data:\n{}", observedRuntimeData);
			LinearRegression lr = new LinearRegression();
			try {
				lr.buildClassifier(observedRuntimeData);
				this.logger.info("Obtained the following output for the regression model: {}", lr);
			} catch (Exception e1) {
				throw new AlgorithmException(e1, "Could not build a regression model for the runtime.");
			}

			/* determine the number of instances that can be used for training with this classifier in the remaining time */
			int numInstances = 500;
			int remainingTime = (int)this.getRemainingTimeToDeadline().milliseconds();
			this.logger.info("Determining number of instances that can be used for training given that {}s are remaining.", (int)Math.round(remainingTime / 1000.0));
			while (numInstances < 10000) {
				Instance low = this.getInstanceForRuntimeAnalysis(numInstances);
				try {
					double predictedRuntime = lr.classifyInstance(low);
					if (predictedRuntime > remainingTime) {
						this.logger.info("Obtained predicted runtime of {}ms for {} training instances, which is more time than we still have. Choosing this number.", predictedRuntime, numInstances);
						break;
					}
					else {
						this.logger.info("Obtained predicted runtime of {}ms for {} training instances, which still seems managable.", predictedRuntime, numInstances);
						numInstances += 50;
					}
				} catch (Exception e) {
					throw new AlgorithmException(e, "Could not obtain a runtime prediction for " + numInstances + " instances.");
				}
			}
			this.logger.info("Believe that {} instances can be used for training in time!", numInstances);

			/* train the classifier with the determined number of samples */
			try {

				File finalDataFile = new File("testrsc/sampled/final/" + this.getInput().getName());
				this.downsampleData(this.intermediateSizeDownsampledFile, finalDataFile, numInstances);
				Instances completeData = new Instances(new FileReader(finalDataFile));
				completeData.setClassIndex(completeData.numAttributes() - 1);
				this.logger.info("Created final dataset with {} instances. Now building the final classifier.", completeData.size());
				long startFinalTraining = System.currentTimeMillis();
				this.mlplan.getSelectedClassifier().buildClassifier(completeData);
				this.logger.info("Classifier has been fully trained within {}ms.", System.currentTimeMillis() - startFinalTraining);
			} catch (Exception e) {
				throw new AlgorithmException(e, "Could not train the final classifier with the full data.");
			}
			return this.terminate();
		default:
			throw new IllegalStateException();
		}
	}

	private Instances getTrainingTimeInstancesForClassifier(final ComponentInstance ci) {
		ArrayList<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute("numInstances"));
		//		attributes.add(new Attribute("numInstancesSquared"));
		attributes.add(new Attribute("runtime"));
		Instances data = new Instances("Runtime Analysis Regression Data for " + ci, attributes, 0);

		/* create one instance for each data point during search phase */
		for (int i = 0; i < this.anchorpointsTraining.length; i++) {
			Instance inst = this.getInstanceForRuntimeAnalysis(this.anchorpointsTraining[i]);
			inst.setValue(1, this.trainingTimesDuringSearch.get(ci)[i]);
			data.add(inst);
		}

		/* create one instance for the mean of the values observed in selection phase */
		if (this.trainingTimesDuringSelection.containsKey(ci)) {
			Instance inst = this.getInstanceForRuntimeAnalysis(this.numTrainingInstancesUsedInSelection);
			inst.setValue(1, StatisticsUtil.mean(this.trainingTimesDuringSelection.get(ci)));
			data.add(inst);
		} else {
			this.logger.warn("Classifier {} has not been evaluated in selection phase. Cannot use this information to fit its regression model.", ci);
		}

		/* set target attribute and return data */
		data.setClassIndex(1);
		return data;
	}

	private Instance getInstanceForRuntimeAnalysis(final int numberOfInstances) {
		Instance inst = new DenseInstance(3);
		inst.setValue(0, numberOfInstances);
		//		inst.setValue(1, Math.pow(numberOfInstances, 2));
		return inst;
	}

	@Subscribe
	public void receiveClassifierCreatedEvent(final ClassifierCreatedEvent e) {
		this.logger.info("Binding component instance {} to classifier {}", e.getInstance(), e.getClassifier());
		this.classifier2modelMap.put(e.getClassifier(), e.getInstance());
	}

	@Subscribe
	public void receiveExtrapolationFinishedEvent(final LearningCurveExtrapolatedEvent e) {
		ComponentInstance ci = this.classifier2modelMap.get(e.getExtrapolator().getLearner());
		this.logger.info("Storing training times {} for classifier {}", Arrays.toString(e.getExtrapolator().getTrainingTimes()), ci);
		this.trainingTimesDuringSearch.put(ci, e.getExtrapolator().getTrainingTimes());
	}

	@Subscribe
	public void receiveMCCVFinishedEvent(final MCCVSplitEvaluationEvent e) {
		ComponentInstance ci = this.classifier2modelMap.get(e.getClassifier());
		this.logger.info("Storing training time {} for classifier {} in selection phase with {} training instances and {} validation instances", e.getSplitEvaluationTime(), ci, e.getNumInstancesUsedForTraining(),
				e.getNumInstancesUsedForValidation());
		if (this.numTrainingInstancesUsedInSelection == 0) {
			this.numTrainingInstancesUsedInSelection = e.getNumInstancesUsedForTraining();
		} else if (this.numTrainingInstancesUsedInSelection != e.getNumInstancesUsedForTraining()) {
			this.logger.warn("Memorized {} as number of instances used for training in selection phase, but now observed one classifier using {} instances.", this.numTrainingInstancesUsedInSelection, e.getNumInstancesUsedForTraining());
		}
		if (!this.trainingTimesDuringSelection.containsKey(ci)) {
			this.trainingTimesDuringSelection.put(ci, new ArrayList<>());
		}
		this.trainingTimesDuringSelection.get(ci).add(e.getSplitEvaluationTime());
	}

	@Override
	public Classifier call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		while (this.hasNext()) {
			this.next();
		}
		return this.mlplan.getSelectedClassifier();
	}

	@Override
	public void setLoggerName(final String loggerName) {
		this.logger = LoggerFactory.getLogger(loggerName);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}
}
