package de.upb.crc901.mlplan.icml2018;

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

import de.upb.crc901.mlplan.classifiers.BRAutoWeka;
import de.upb.crc901.mlplan.classifiers.MultiLabelGraphBasedPipelineSearcher;
import de.upb.crc901.mlplan.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import de.upb.crc901.mlplan.core.MySQLMultiLabelExperimentLogger;
import de.upb.crc901.mlplan.search.evaluators.BalancedRandomCompletionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MonteCarloCrossValidationEvaluator;
import de.upb.crc901.mlplan.search.evaluators.multilabel.ExactMatchMultilabelEvaluator;
import de.upb.crc901.mlplan.search.evaluators.multilabel.F1AverageMultilabelEvaluator;
import de.upb.crc901.mlplan.search.evaluators.multilabel.HammingMultilabelEvaluator;
import de.upb.crc901.mlplan.search.evaluators.multilabel.JaccardMultilabelEvaluator;
import de.upb.crc901.mlplan.search.evaluators.multilabel.MultilabelEvaluator;
import de.upb.crc901.mlplan.search.evaluators.multilabel.RankMultilabelEvaluator;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ICML2018MLTest {

	private final File datasetFolder;
	private final MySQLMultiLabelExperimentLogger expLogger = new MySQLMultiLabelExperimentLogger("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "mlplan_multilabel_results");

	public ICML2018MLTest(File datasetFolder) {
		this.datasetFolder = datasetFolder;
	}

	protected String[] getClassifierNames() {
		return new String[] { "MLPlan-Multilabel-", "BR-Auto-WEKA" };
//		return new String[] { "MLPlan-Multilabel-" };
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

	protected void logExperimentResult(String dataset, ArrayNode rowsForSearch, String algoName, int seed, int timeout, int numCPUs, String setupName, Classifier c,
			double loss_f1, double loss_hamming, double loss_exact, double loss_jaccard, double loss_rank) {
		expLogger.addResultEntry(c, loss_f1, loss_hamming, loss_exact, loss_jaccard, loss_rank);
		expLogger.close();
	}

	protected MultiLabelClassifier getConfiguredClassifier(int seed, String setupName, String algoName, int timeout, MultilabelEvaluator evaluator) {
		try {
			switch (algoName) {
			case "MLPlan-Multilabel-": {

				// timeout = 60 * 6;
				Random random = new Random(seed);
				// TwoPhasePipelineSearcher<Double> bs = new BalancedSearcher(random, 1000 * timeout);
				TwoPhaseHTNBasedPipelineSearcher<Double> bs = new TwoPhaseHTNBasedPipelineSearcher<>();

				// MultilabelEvaluator evaluator = new HammingMultilabelEvaluator(random);

				bs.setHtnSearchSpaceFile(new File("testrsc/multilabel/mlplan-multilabel.searchspace"));
				// bs.setHtnSearchSpaceFile(new File("testrsc/automl3.testset"));
				// bs.setEvaluablePredicateFile(new File("testrsc/automl-reduction.evaluablepredicates"));
				bs.setRandom(random);
				bs.setTimeout(1000 * timeout);
				bs.setNumberOfCPUs(4);
				bs.setSolutionEvaluatorFactory4Search(() -> new MonteCarloCrossValidationEvaluator(evaluator, 3, .7f));
				bs.setSolutionEvaluatorFactory4Selection(() -> new MonteCarloCrossValidationEvaluator(evaluator, 10, .7f));
				bs.setRce(new BalancedRandomCompletionEvaluator(random, 3, new MonteCarloCrossValidationEvaluator(evaluator, 3, .7f)));
				bs.setTimeoutPerNodeFComputation(1000 * (timeout == 60 ? 15 : 300 ));
//				bs.setTooltipGenerator(new TFDTooltipGenerator<>());
				bs.setPortionOfDataForPhase2(.3f);
				// BR br = new BR();
				// br.setClassifier(bs);
				return new MultiLabelGraphBasedPipelineSearcher<>(bs);
				// return br;
			}
			case "BR-Auto-WEKA":
				return new BRAutoWeka(seed, timeout, 16 * 1024); // give 9gb to AutoWEKA
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		File folder = new File(args[0]);
		int k = Integer.valueOf(args[1]);
		ICML2018MLTest runner = new ICML2018MLTest(folder);
		runner.run(k, "de.upb.crc901.mlplan.search.evaluators.multilabel.F1AverageMultilabelEvaluator");
		System.exit(0);
	}

	protected int[] getTimeouts() {
		return new int[] { 3600, 86400 / 2 };
	}

	protected int getNumberOfCPUS() {
		return 4;
	}

	public void run(int k, String evalName) throws Exception {

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
		MLUtils.prepareData(data);
		Collection<Integer>[] overallSplitIndices = WekaUtil.getArbitrarySplit(data, r, getTrainingPortion());
		List<Instances> overallSplit = WekaUtil.realizeSplit(data, overallSplitIndices);
		Instances internalData = overallSplit.get(0);
		Instances testData = overallSplit.get(1);
		System.out.println("Data were split into " + internalData.size() + "/" + testData.size());

		/* create actual classifier */
		System.out.println("Now configuring classifier ...");
		MultilabelEvaluator eval = (MultilabelEvaluator) Class.forName(evalName).getConstructor(Random.class).newInstance(new Random(seedId));
		MultiLabelClassifier c = getConfiguredClassifier(seedId, setups[setupId], classifiers[algoId], timeouts[timeoutId], eval);
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
			int loss_f1  = (int) (new F1AverageMultilabelEvaluator(r).loss(c, testData) * 100);
			int loss_hamming  = (int) (new HammingMultilabelEvaluator(r).loss(c, testData) * 100);
			int loss_exact  = (int) (new ExactMatchMultilabelEvaluator(r).loss(c, testData) * 100);
			int loss_jaccard  = (int) (new JaccardMultilabelEvaluator(r).loss(c, testData) * 100);
			int loss_rank  = (int) (new RankMultilabelEvaluator(r).loss(c, testData) * 100);
			System.out.println("Sending error Rate " + loss_f1 + "/" + loss_hamming + "/" + loss_exact + "/" + loss_jaccard + "/" + loss_rank + " to logger.");

			logExperimentResult(data.relationName(), an, classifiers[algoId], seedId, timeouts[timeoutId], getNumberOfCPUS(), setups[setupId], c, loss_f1, loss_hamming, loss_exact, loss_jaccard, loss_rank);
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Sending error Rate -10000 to logger.");
			logExperimentResult(data.relationName(), an, classifiers[algoId], seedId, timeouts[timeoutId], getNumberOfCPUS(), setups[setupId], c, -10000, -10000, -10000, -10000, -10000);
		}
	}

	public String getExperimentDescription(File folder, int datasetId, Classifier algorithm, int seed) {
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
