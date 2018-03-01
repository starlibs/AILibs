package de.upb.crc901.mlplan.scc2018;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import de.upb.crc901.mlplan.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import de.upb.crc901.mlplan.core.MLUtil;
import de.upb.crc901.mlplan.core.MySQLMLPlanExperimentLogger;
import de.upb.crc901.mlplan.search.evaluators.BalancedRandomCompletionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MonteCarloCrossValidationEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MulticlassEvaluator;
import de.upb.crc901.services.core.HttpServiceServer;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class SCC2018MLTest {

	private final File datasetFolder;
	private final MySQLMLPlanExperimentLogger expLogger = new MySQLMLPlanExperimentLogger("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "mlplan_services_results");

	public SCC2018MLTest(File datasetFolder) {
		this.datasetFolder = datasetFolder;
	}

	protected String[] getClassifierNames() {
		return new String[] { "MLS-Plan" };
	}

	protected String[] getSetupNames() {
		return new String[] { "3-70-MCCV" };
	}

	protected int getNumberOfRunsPerExperiment() {
		return 10;
	}

	protected float getTrainingPortion() {
		return 0.7f;
	}

	protected void logExperimentStart(String dataset, ArrayNode rowsForSearch, String algoName, int seed, int timeout, int numCPUs, String setupName) {
		expLogger.createAndSetRun(dataset, rowsForSearch, algoName, seed, timeout, numCPUs, setupName);
	}

	protected void logExperimentResult(String dataset, ArrayNode rowsForSearch, String algoName, int seed, int timeout, int numCPUs, String setupName, Classifier c, double loss) {
		expLogger.addResultEntry(c, loss);
		expLogger.close();
	}

	protected Classifier getConfiguredClassifier(int seed, String setupName, String algoName, int timeout) {
		try {
			switch (algoName) {
			case "MLS-Plan": {

				File evaluablePredicatFile = new File("testrsc/services/automl.evaluablepredicates_");
				File searchSpaceFile = new File("testrsc/services/automl-services.searchspace");
				TwoPhaseHTNBasedPipelineSearcher<Double> bs = new TwoPhaseHTNBasedPipelineSearcher<>();
//				execDeriavationTree(searchSpaceFile, evaluablePredicatFile, bs, seed, timeout , 4 /* num CPUs*/ );
				logicalDerivationTree(searchSpaceFile, evaluablePredicatFile);
				
				return bs;
			}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void execDeriavationTree(File searchSpaceFile, File evaluablePredicatFile, TwoPhaseHTNBasedPipelineSearcher<Double> bs, int seed, int timeout, int numCPUs) throws IOException {

		Random random = new Random(seed);
		bs.setHtnSearchSpaceFile(searchSpaceFile);
		//bs.setHtnSearchSpaceFile(new File("testrsc/automl3.testset"));
		bs.setEvaluablePredicateFile(evaluablePredicatFile);
		bs.setRandom(random);
		bs.setTimeout(1000 * timeout);
		bs.setNumberOfCPUs(numCPUs);
		MulticlassEvaluator evaluator = new MulticlassEvaluator(random);
		bs.setSolutionEvaluatorFactory4Search(() -> new MonteCarloCrossValidationEvaluator(evaluator, 3, .7f));
		bs.setSolutionEvaluatorFactory4Selection(() -> new MonteCarloCrossValidationEvaluator(evaluator, 10, .7f));
		bs.setRce(new BalancedRandomCompletionEvaluator(random, 3, new MonteCarloCrossValidationEvaluator(evaluator, 3, .7f)));
		bs.setTimeoutPerNodeFComputation(1000 * (timeout == 60 ? 15 : 300));
		bs.setTooltipGenerator(new TFDTooltipGenerator<>());
		bs.setPortionOfDataForPhase2(.3f);

		bs.setExperimentLogger(expLogger);
		evaluator.getMeasurementEventBus().register(expLogger);
	}

	private void logicalDerivationTree(File searchSpaceFile, File evaluablePredicatFile) throws IOException {

		 ORGraphSearch<TFDNode, String, Double> bf = new BestFirst<>(MLUtil.getGraphGenerator(searchSpaceFile, evaluablePredicatFile, null, null), n
		 -> 0.0);
		 new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());;
		
		 while(bf.nextSolution() != null);
	}
	
	public static void main(String[] args) throws Exception {
		HttpServiceServer server = HttpServiceServer.TEST_SERVER();
		try {

			File folder = new File(args[0]);
			int k = Integer.valueOf(args[1]);
			SCC2018MLTest runner = new SCC2018MLTest(folder);
			runner.run(k);
			System.exit(0);
		} finally {
			server.shutdown();
		}
	}

	protected int[] getTimeouts() {
		return new int[] { 60 };
	}

	protected int getNumberOfCPUS() {
		return 4;
	}

	public void run(int k) throws Exception {

		/* get classifiers */
		String[] classifiers = getClassifierNames();
		String[] setups = getSetupNames();
		int[] timeouts = getTimeouts();

		/* read data sets */
		List<File> availableDatasets = getAvailableDatasets(datasetFolder);
		System.out.println("Available datasets: ");
		final AtomicInteger i = new AtomicInteger();
		availableDatasets.stream().forEach(ds -> System.out.println("\t" + (i.getAndIncrement()) + ": " + ds.getName()));
		System.out.println("Available algorithms: ");
		i.set(0);
		Arrays.asList(classifiers).stream().forEach(c -> System.out.println("\t" + (i.getAndIncrement()) + ": " + c.getClass().getName()));
		int numberOfDatasets = availableDatasets.size();
		int numberOfClassifiers = classifiers.length;
		int numberOfSetups = setups.length;
		int numberOfSeeds = getNumberOfRunsPerExperiment();
		int numberOfTimeouts = timeouts.length;
		System.out.println("Number of runs (seeds) per dataset/algo-combination: " + numberOfSeeds);
		int totalExperimentSize = numberOfClassifiers * numberOfDatasets * numberOfSetups * numberOfSeeds * numberOfTimeouts;
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
		int setupId = (int) Math.floor(indexWithinSeed / frameSizeForSetup * 1f);
		int indexWithinSetup = indexWithinSeed % frameSizeForSetup;
		int datasetId = (int) Math.floor(indexWithinSetup / frameSizeForDataset * 1f);
		int indexWithinDataset = indexWithinSetup % frameSizeForDataset;
		int algoId = indexWithinDataset % frameSizeForSeed;

		/* read dataset */
		String datasetName = getAvailableDatasets(datasetFolder).get(datasetId).getName();
		datasetName = datasetName.substring(0, datasetName.lastIndexOf("."));

		System.out.println("Running experiment " + k + "/" + totalExperimentSize + ". The setup is: " + timeoutId + "/" + seedId + "/" + setupId + "/" + datasetId + "/" + algoId
				+ "(timeout/seed/setup/dataset/algo)");

		/* create random object */
		Random r = new Random(seedId);

		/* create actual dataset */
		Instances data = getKthInstances(datasetFolder, datasetId);
		data.setClassIndex(data.numAttributes() - 1);
		Collection<Integer>[] overallSplitIndices = WekaUtil.getStratifiedSplitIndices(data, r, getTrainingPortion());
		List<Instances> overallSplit = WekaUtil.realizeSplit(data, overallSplitIndices);
		Instances internalData = overallSplit.get(0);
		Instances testData = overallSplit.get(1);
		System.out.println("Data were split into " + internalData.size() + "/" + testData.size());

		/* create actual classifier */
		System.out.println("Now configuring classifier ...");
		MulticlassEvaluator eval = new MulticlassEvaluator(new Random(seedId));
		Classifier c = getConfiguredClassifier(seedId, setups[setupId], classifiers[algoId], timeouts[timeoutId]);
		System.out.println("Classifier configured. Determining result files.");

		/* now search for the best pipeline */
		long start = System.currentTimeMillis();
		ObjectMapper om = new ObjectMapper();
		ArrayNode an = om.createArrayNode();
		overallSplitIndices[0].stream().sorted().forEach(v -> an.add(v));
		logExperimentStart(data.relationName(), an, classifiers[algoId], seedId, timeouts[timeoutId], getNumberOfCPUS(), setups[setupId]);
		System.out.println(
				"Invoking " + getExperimentDescription(datasetFolder, datasetId, c, seedId) + " with setup " + setups[setupId] + " and timeout " + timeouts[timeoutId] + "s");
		try {
			c.buildClassifier(internalData);
			long end = System.currentTimeMillis();
			System.out.println("Search has finished. Runtime: " + (end - start) / 1000f + " s");

			/* check performance of the pipeline */
			int loss = (int) (eval.loss(c, testData) * 100);
			System.out.println("Sending error Rate " + loss + " to logger.");

			logExperimentResult(data.relationName(), an, classifiers[algoId], seedId, timeouts[timeoutId], getNumberOfCPUS(), setups[setupId], c, loss);
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Sending error Rate -10000 to logger.");
			logExperimentResult(data.relationName(), an, classifiers[algoId], seedId, timeouts[timeoutId], getNumberOfCPUS(), setups[setupId], c, -10000);
		}
	}

	public String getExperimentDescription(File folder, int datasetId, Classifier algorithm, int seed) {
		if (folder == null)
			throw new IllegalArgumentException("Folder must not be null");
		if (algorithm == null)
			throw new IllegalArgumentException("Algorithm must not be null");
		try {
			return algorithm.getClass().getName() + "-" + getAvailableDatasets(folder).get(datasetId).getName() + "-" + seed;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
		// inst.setRelationName((folder + File.separator + f.getName()).replace(File.separator, "/"));
		return inst;
	}
}
