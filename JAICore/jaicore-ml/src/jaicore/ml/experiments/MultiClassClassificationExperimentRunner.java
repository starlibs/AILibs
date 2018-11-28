package jaicore.ml.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import jaicore.logging.LoggerUtil;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.singlelabel.MulticlassMetric;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public abstract class MultiClassClassificationExperimentRunner {

	private static final Logger logger = LoggerFactory.getLogger(MultiClassClassificationExperimentRunner.class);
	
	private final File datasetFolder;
	private final List<File> availableDatasets;
	private final String[] classifiers;
	private final Map<String, String[]> setups;
	private final int numberOfSetups;
	private final int[] timeoutsInSeconds;
	private final int numberOfRunsPerExperiment;
	private final float trainingPortion;
	private final int numberOfCPUs;
	private final int memoryInMB;
	private final MulticlassMetric performanceMeasure;
	private final IMultiClassClassificationExperimentDatabase database;
	private final int totalExperimentSize;
	private Collection<MLExperiment> experimentsConductedEarlier;

	@SuppressWarnings("serial")
	class ExperimentAlreadyConductedException extends Exception {
		public ExperimentAlreadyConductedException(String message) {
			super(message);
		}
	}
	
	public MultiClassClassificationExperimentRunner(File datasetFolder, String[] classifiers, Map<String, String[]> setups, int[] timeoutsInSeconds, int numberOfRunsPerExperiment,
			float trainingPortion, int numberOfCPUs, int memoryInMB, MulticlassMetric performanceMeasure, IMultiClassClassificationExperimentDatabase logger) throws IOException {
		super();
		this.datasetFolder = datasetFolder;
		this.availableDatasets = getAvailableDatasets(datasetFolder);
		this.classifiers = classifiers;
		this.setups = setups;
		this.timeoutsInSeconds = timeoutsInSeconds;
		this.numberOfRunsPerExperiment = numberOfRunsPerExperiment;
		this.trainingPortion = trainingPortion;
		this.numberOfCPUs = numberOfCPUs;
		this.memoryInMB = memoryInMB;
		this.performanceMeasure = performanceMeasure;
		this.database = logger;

		int tmpNumberOfSetups = 0;
		for (String[] setupsOfClassifier : this.setups.values())
			tmpNumberOfSetups += setupsOfClassifier.length;
		numberOfSetups = tmpNumberOfSetups;

		totalExperimentSize = classifiers.length * availableDatasets.size() * numberOfSetups * numberOfRunsPerExperiment * timeoutsInSeconds.length;
		
		/* print information about the experiments */
		System.out.println("Available datasets: ");
		final AtomicInteger i = new AtomicInteger();
		availableDatasets.stream().forEach(ds -> System.out.println("\t" + (i.getAndIncrement()) + ": " + ds.getName()));
		System.out.println("Available algorithms: ");
		i.set(0);
		Arrays.asList(classifiers).stream().forEach(c -> System.out.println("\t" + (i.getAndIncrement()) + ": " + c));
	}

	protected abstract Classifier getConfiguredClassifier(int seed, String algoName, String algoMode, int timeout, int numberOfCPUs, int memoryInMB,
			MulticlassMetric performanceMeasure);

	public void runAll() throws Exception {
		experimentsConductedEarlier = database.getExperimentsForWhichARunExists();
		for (int k = 0; k < totalExperimentSize; k++) {
			try {
				runSpecific(k);
			} catch (ExperimentAlreadyConductedException e) {
				System.out.println(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void runAny() throws Exception {
		experimentsConductedEarlier = database.getExperimentsForWhichARunExists();
		List<Integer> indices = new ArrayList<>(ContiguousSet.create(Range.closed(0, totalExperimentSize - 1), DiscreteDomain.integers()).asList());
		Collections.shuffle(indices);
		for (int index : indices){
			try {
				runSpecific(index);
				return;
			} catch (ExperimentAlreadyConductedException e) {
				System.out.println(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		while (true);
	}

	public void runSpecific(int k) throws Exception {

		int numberOfDatasets = availableDatasets.size();
		int numberOfSeeds = numberOfRunsPerExperiment;
		int numberOfTimeouts = timeoutsInSeconds.length;
		System.out.println("Number of runs (seeds) per dataset/algo-combination: " + numberOfSeeds);
		int frameSizeForTimeout = totalExperimentSize / numberOfTimeouts;
		int frameSizeForSeed = frameSizeForTimeout / numberOfSeeds;
		int frameSizeForSetup = frameSizeForSeed / numberOfSetups;
		int frameSizeForDataset = frameSizeForSetup / numberOfDatasets;
		if (k >= totalExperimentSize)
			throw new IllegalArgumentException("Only " + totalExperimentSize + " experiments defined.");

		/* determine exact experiment */
		int timeoutId = (int) Math.floor(k / frameSizeForTimeout * 1f);
		int indexWithinTimeout = k % frameSizeForTimeout;
		int seedId = (int) Math.floor(indexWithinTimeout / frameSizeForSeed * 1f);
		int indexWithinSeed = indexWithinTimeout % frameSizeForSeed;
		int datasetId = (int) Math.floor(indexWithinSeed / frameSizeForDataset * 1f);
		int indexWithinDataset = indexWithinSeed % frameSizeForDataset;
		int algoAndSetupId = (int) Math.floor(indexWithinDataset / frameSizeForSetup * 1f);
		// int indexWithinSetup = indexWithinDataset % frameSizeForSetup;

		System.out.println("Running experiment " + k + "/" + totalExperimentSize + ". The setup is: " + timeoutId + "/" + seedId + "/" + datasetId + "/" + "/" + algoAndSetupId
				+ "(timeout/seed/dataset/algo-setup-id)");
		runExperiment(datasetId, timeoutId, seedId, algoAndSetupId);
	}

	private int getAlgoIdForAlgoSetupId(int algoSetupId) {
		int counter = 0;
		for (int i = 0; i < classifiers.length; i++) {
			counter += setups.get(classifiers[i]).length;
			if (algoSetupId < counter)
				return i;
		}
		return -1;
	}

	private int getSetupIdForAlgoSetupId(int algoSetupId) {
		int counter = 0;
		for (int i = 0; i < classifiers.length; i++) {
			String[] setupsOfThisClassifier = setups.get(classifiers[i]);
			for (int j = 0; j < setupsOfThisClassifier.length; j++) {
				if (counter == algoSetupId)
					return j;
				counter++;
			}
		}
		return -1;
	}

	public void runExperiment(int datasetId, int timeoutId, int seedId, int algoAndSetupId) throws Exception {

		/* read dataset */
		String datasetName = availableDatasets.get(datasetId).getName();
		datasetName = datasetName.substring(0, datasetName.lastIndexOf("."));

		/* create experiment */
		int algoId = getAlgoIdForAlgoSetupId(algoAndSetupId);
		int setupId = getSetupIdForAlgoSetupId(algoAndSetupId);
		String algo = classifiers[algoId];
		String algoMode = setups.get(algo)[setupId];
		int timeoutInSeconds = timeoutsInSeconds[timeoutId];
		if (performanceMeasure != MulticlassMetric.errorRate) {
			throw new IllegalArgumentException("Currently the only supported performance measure is errorRate");
		}
		MLExperiment exp = new MLExperiment(new File(datasetFolder + File.separator + availableDatasets.get(datasetId)).getAbsolutePath(), algo, algoMode, seedId, timeoutInSeconds, numberOfCPUs, memoryInMB, performanceMeasure.toString());
		
		/* conduct a first check whether this experiment already exists */
		if (experimentsConductedEarlier != null && experimentsConductedEarlier.contains(exp)) {
			throw new ExperimentAlreadyConductedException("Experiment " + exp + " has already been conducted");
		}

		try {
			
			/* create actual classifier */
			System.out.println("Now configuring classifier ...");
			Classifier c = getConfiguredClassifier(seedId, algo, algoMode, timeoutInSeconds, numberOfCPUs, memoryInMB, performanceMeasure);
			
			/* get all experiments conducted or in progress so far */
			Collection<MLExperiment> experiments = database.getExperimentsForWhichARunExists();
			if (experiments.contains(exp)) {
				throw new ExperimentAlreadyConductedException("Experiment has already been conducted, but rather recently: " + exp);
			}
			
			int runId = database.createRunIfDoesNotExist(exp);
			if (runId < 0) {
				throw new ExperimentAlreadyConductedException("Experiment has already been conducted, but quite recently: " + exp);
			}
			System.out.println("The assigned runId for this experiment is " + runId);
			
			/* create random object */
			Random r = new Random(seedId);

			/* create actual dataset */
			Instances data = getKthInstances(datasetFolder, datasetId);
			data.setClassIndex(data.numAttributes() - 1);
			
			Collection<Integer>[] overallSplitIndices = WekaUtil.getStratifiedSplitIndices(data, r, trainingPortion);
			List<Instances> overallSplit = WekaUtil.realizeSplit(data, overallSplitIndices);
			Instances internalData = overallSplit.get(0);
			Instances testData = overallSplit.get(1);
			ObjectMapper om = new ObjectMapper();
			ArrayNode an = om.createArrayNode();
			overallSplitIndices[0].stream().sorted().forEach(v -> an.add(v));
			System.out.println("Data were split into " + internalData.size() + "/" + testData.size());
			
			/* update database information */
			Map<String,String> runUpdate = new HashMap<>();
			runUpdate.put("rows_for_training", an.toString());
			database.updateExperiment(exp, runUpdate);
			database.associatedRunWithClassifier(runId, c);
			System.out.println("Classifier configured. Determining result files.");

			System.out.println("Invoking " + getExperimentDescription(datasetId, c, seedId) + " with setup " + algoMode + " and timeout " + timeoutsInSeconds[timeoutId] + "s");
			long start = System.currentTimeMillis();
			try {
				c.buildClassifier(internalData);
				long end = System.currentTimeMillis();
				System.out.println("Search has finished. Runtime: " + (end - start) / 1000f + " s");

				/* check performance of the pipeline */
				int mistakes = 0;
				Method m = MethodUtils.getMatchingAccessibleMethod(c.getClass(), "classifyInstances", Instances.class);
				if (m != null) {
					double[] predictions = (double[]) m.invoke(c, testData);
					for (int i = 0; i < predictions.length; i++) {
						if (predictions[i] != testData.get(i).classValue())
							mistakes ++;
					}
				}
				else {
					for (Instance i : testData) {
						if (i.classValue() != c.classifyInstance(i))
							mistakes++;
					}
				}
				double error = mistakes * 10000f / testData.size();
				System.out.println("Sending error Rate " + error + " to logger.");
				database.addResultEntry(runId, error);
			} catch (Throwable e) {
				logger.error("Experiment failed. Details:\n{}", LoggerUtil.getExceptionInfo(e));
				System.out.println("Sending error Rate -10000 to logger.");
				try {
					database.addResultEntry(runId, -10000);
				} catch (Exception e1) {
					logger.error("Could not write result to database. Details:\n{}", LoggerUtil.getExceptionInfo(e1));
				}
			}
		} catch (Exception e) {
			logger.error("Experiment failed. Details:\n{}", LoggerUtil.getExceptionInfo(e));
		}
	}

	public String getExperimentDescription(int datasetId, Classifier algorithm, int seed) {
		return algorithm + "-" + availableDatasets.get(datasetId).getName() + "-" + seed;
	}

	public List<File> getAvailableDatasets(File folder) throws IOException {
		List<File> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(folder.toPath())) {
			paths.filter(f -> f.getParent().toFile().equals(folder) && f.toFile().getAbsolutePath().endsWith(".arff")).forEach(f -> files.add(f.toFile()));
		}
		return files.stream().sorted().collect(Collectors.toList());
	}

	public Instances getKthInstances(File folder, int k) throws IOException {
		File f = getAvailableDatasets(folder).get(k);
		System.out.println("Selecting " + f);
		Instances inst = new Instances(new BufferedReader(new FileReader(f)));
		inst.setRelationName((f.getAbsolutePath()).replace(File.separator, "/"));
		return inst;
	}

	public IMultiClassClassificationExperimentDatabase getLogger() {
		return database;
	}
}
