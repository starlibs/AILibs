package de.upb.crc901.mlplan.evaluation.ecml2018;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigCache;

import de.upb.crc901.mlplan.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import de.upb.crc901.mlplan.search.evaluators.MulticlassEvaluator;
import jaicore.basic.MathExt;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AutoWEKAClassifier;
import weka.core.Instances;

public class PipelineEvaluation {

	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.println(
					"Benchmark must receive 2 inputs: 1) the folder with the datasets, 2) the run id; from this, we compute the dataset, the random sett, and the algorithm id");
		}

		long experimentId = System.currentTimeMillis();

		/* determine datasets and algorithms possibly used in the experiments */
		IPipelineEvaluationConf conf = ConfigCache.getOrCreate(IPipelineEvaluationConf.class);
		File folder = new File(args[0]);
		int k = Integer.parseInt(args[1]);
		Classifier[] classifiers = new Classifier[] {
//				new AutoWEKAClassifier()
				new TwoPhaseHTNBasedPipelineSearcher()
				};
		List<File> availableDatasets = getAvailableDatasets(folder);
		System.out.println("Available datasets: ");
		final AtomicInteger i = new AtomicInteger();
		availableDatasets.stream().forEach(ds -> System.out.println("\t" + (i.getAndIncrement()) + ": " + ds.getName()));
		System.out.println("Available algorithms: ");
		i.set(0);
		Arrays.asList(classifiers).stream().forEach(c -> System.out.println("\t" + (i.getAndIncrement()) + ": " + c.getClass().getName()));
		int numberOfDatasets = availableDatasets.size();
		int numberOfClassifiers = classifiers.length;
		int numberOfSeeds = conf.getNumberOfRuns();
		System.out.println("Number of runs (seeds) per dataset/algo-combination: " + numberOfSeeds);
		int frameSizeForDataSet = numberOfSeeds * numberOfClassifiers;
		int frameSizeForSeed = numberOfClassifiers;
		
		/* determine exact experiment */
		int datasetId = (int) Math.floor(k / frameSizeForDataSet * 1f);
		String datasetName = getAvailableDatasets(folder).get(datasetId).getName();
		datasetName = datasetName.substring(0, datasetName.lastIndexOf("."));
		int indexWithinDataset = k % frameSizeForDataSet;
		int seedId = (int) Math.floor(indexWithinDataset / frameSizeForSeed * 1f);
		int algoId = indexWithinDataset % frameSizeForSeed;

		System.out.println("Running experiment " + k + "/" + (numberOfDatasets * numberOfClassifiers * numberOfSeeds) + ". The setup is: " + datasetId + "/" + seedId + "/" + algoId + "(dataset/seed/algo)");
		
		/* config */
		int timeoutPerRunInS = conf.getTimeoutTotal();
		int timeoutPerRunInM = (int)Math.max(1, Math.round(timeoutPerRunInS / 60));
		int timeoutForFComputation = conf.getTimeoutPerCandidate();
		int allowedCPUs = conf.getNumberOfAllowedCPUs();
		int maxMemory = conf.getMemoryLimitinMB();
		if (timeoutForFComputation > timeoutPerRunInS)
			throw new IllegalArgumentException("Inconsistent configuration: Timeout for f-value computation is " + timeoutForFComputation + " where total timeout is only " + timeoutPerRunInS);
		if (allowedCPUs > Runtime.getRuntime().availableProcessors() - 1)
			throw new IllegalArgumentException("Too many CPUs requested; " + allowedCPUs + " are requested, but only " + (Runtime.getRuntime().availableProcessors() -2) + " must be used");
		
		/* create random object */
		Random r = new Random(seedId);
		
		/* create actual dataset */
		Instances data = getKthInstances(folder, datasetId);
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> overallSplit = WekaUtil.getStratifiedSplit(data, r, conf.getTrainingPortion() / 100f);
		Instances internalData = overallSplit.get(0);
		Instances testData = overallSplit.get(1);
		System.out.println("Data were split into " + internalData.size() + "/" + testData.size());
		
//		System.out.println("First 10 instances of internal data are: ");
//		for (int j = 0; j < 10; j++)
//			System.out.println("\t " + internalData.get(j));
//		System.out.println("First 10 instances of test data are: ");
//		for (int j = 0; j < 10; j++)
//			System.out.println("\t " + testData.get(j));
		
		/* determine portfolio timeout */
//		Classifier[] portfolio = {
////				new NaiveBayes(), 
////				new SimpleLogistic(),
////				new NaiveBayesMultinomial(),
//				new IBk(),
////				new RandomTree(),
////				new RandomForest(),
////				new J48(),
////				new SMO(),
////				new MultilayerPerceptron()
//				};
//		DescriptiveStatistics runtimeStats = new DescriptiveStatistics();
//		for (Classifier c : portfolio) {
//			long start = System.currentTimeMillis();
//			
//			if (c instanceof MultilayerPerceptron) {
//				((OptionHandler)c).setOptions(new String[]{"-H", "0"});
//			}
//			MLPipeline pl = new MLPipeline(null, null, null, c);
//			System.out.print(pl + ": ");
//			pl.buildClassifier(internalData);
//			long inter = System.currentTimeMillis();
//			System.out.println(inter - start + " train");
//			Evaluation eval = new Evaluation(internalData);
//			eval.evaluateModel(pl, testData);
//			long end = System.currentTimeMillis();
//			System.out.println(end - inter + " eval");
//			runtimeStats.addValue(end - start);
//		}
//		int flexibleRuntimeInMs = (int)runtimeStats.getMax() * 100;
//		int flexibleRuntimeInS = (int)Math.max(1, Math.round(flexibleRuntimeInMs / 1000));
//		System.exit(0);
		
		/* create actual classifier */
		Classifier c = classifiers[algoId];
		if (c instanceof AutoWEKAClassifier) {
			((AutoWEKAClassifier) c).setTimeLimit(timeoutPerRunInM);
			int memoryForAutoWEKARuns = (int)(.9 * (((maxMemory - (int)((Runtime.getRuntime().maxMemory() / 1024 / 1024))) / allowedCPUs) - conf.getAssumedMemoryOverheadPerProcess()));
			System.out.println(memoryForAutoWEKARuns);
			System.out.println(allowedCPUs);
			((AutoWEKAClassifier) c).setMemLimit(memoryForAutoWEKARuns); // They use the memory for each parallel run
			System.out.println("Expected usage: " + ((int)((Runtime.getRuntime().maxMemory() / 1024 / 1024)) + conf.getAssumedMemoryOverheadPerProcess() + allowedCPUs * (memoryForAutoWEKARuns + conf.getAssumedMemoryOverheadPerProcess())));
//			((AutoWEKAClassifier) c).setParallelRuns(allowedCPUs - 2);
		} else if (c instanceof TwoPhaseHTNBasedPipelineSearcher) {
			((TwoPhaseHTNBasedPipelineSearcher) c).setTimeout(timeoutPerRunInS * 1000);
			((TwoPhaseHTNBasedPipelineSearcher) c).setRandom(r);
			((TwoPhaseHTNBasedPipelineSearcher) c).setTimeoutPerNodeFComputation(timeoutForFComputation * 1000);
			((TwoPhaseHTNBasedPipelineSearcher) c).setNumberOfCPUs(allowedCPUs);
			((TwoPhaseHTNBasedPipelineSearcher) c).setTmpDir(conf.getTmpDir());
			((TwoPhaseHTNBasedPipelineSearcher) c).setMemory(maxMemory);
			((TwoPhaseHTNBasedPipelineSearcher) c).setMemoryOverheadPerProcessInMB(conf.getAssumedMemoryOverheadPerProcess());
			((TwoPhaseHTNBasedPipelineSearcher) c).setNumberOfMCIterationsPerSolutionInSelectionPhase(conf.getNumberOfIterationsInSelectionPhase());
			((TwoPhaseHTNBasedPipelineSearcher) c).setNumberOfConsideredSolutions(conf.getNumberOfCandidatesInSelectionPhase());
			((TwoPhaseHTNBasedPipelineSearcher) c).setSolutionLogFile(new File(conf.getSolutionLogDir() + File.separator + "solutions-" + k + "-" + experimentId + "-" + datasetName + "-" + seedId + ".log"));
			((TwoPhaseHTNBasedPipelineSearcher) c).setSolutionEvaluator(new MulticlassEvaluator(r), conf.getValidationAlgorithm());
			((TwoPhaseHTNBasedPipelineSearcher) c).setPortionOfDataForPhase2(conf.getPortionOfDataForPhase2());
//			((TwoPhasePipelineSearcher) c).setTooltipGenerator(new TFDTooltipGenerator());
		}
		
		/* determine result file */
		File resultFolder = new File("results" + File.separator + timeoutPerRunInS);
		if (!resultFolder.exists())
			resultFolder.mkdirs();
		File resultFile = new File(resultFolder + File.separator + c.getClass().getName() + "-" + datasetName + ".csv");
		

		
		
		/* now search for the best pipeline */
		long start = System.currentTimeMillis();
		System.out.println("Invoking " + getExperimentDescription(folder, datasetId, c, seedId) + " with time limit " + timeoutPerRunInS + "s");
		c.buildClassifier(internalData);
		long end = System.currentTimeMillis();
		System.out.println("Search has finished. Runtime: " + (end - start) / 1000f + " s");

		/* check performance of the pipeline */
		Evaluation eval = new Evaluation(internalData);
		eval.evaluateModel(c, testData);
		double error = MathExt.round((eval.pctIncorrect() + eval.pctUnclassified()) / 100f, 4);
		
		try (FileWriter fw = new FileWriter(resultFile, true)) {
			String selection = "n/a";
			String believedError = "";
			String timeUntilSolutionWasFound = "";
			if (c instanceof TwoPhaseHTNBasedPipelineSearcher) {
				TwoPhaseHTNBasedPipelineSearcher castedC = (TwoPhaseHTNBasedPipelineSearcher)c;
//				selection = castedC.getSelectedModel().getCreationPlan().stream().map(a -> a.getEncoding()).collect(Collectors.toList()).toString();
				System.out.println(castedC.getSelectedModel());
				believedError = String.valueOf(castedC.getAnnotation(castedC.getSelectedModel()).getF());
//				timeUntilSolutionWasFound = String.valueOf(castedC.getAnnotation(castedC.getSelectedModel()).get);
			}
			else if (c instanceof AutoWEKAClassifier) {
				AutoWEKAClassifier castedC = (AutoWEKAClassifier)c;
				
				selection = castedC.toString().replaceAll("\n", "\t");
				System.out.println("AutoWEKA report:");
				System.out.println(castedC.toString());
			}
			fw.write(experimentId + ", " + k + ", " + seedId + ", " + error + ", " + believedError + ", " + timeUntilSolutionWasFound+ ", " + (end - start) +", " + selection + "\n");
		}
		
		System.out.println("Wrote an error of " + error + " to stats file. Now enforcing termination.");
		System.exit(0); // kill possibly remaining thread pools
		
//		System.out.println("Error of returned solution: " +  + ".\n-----------------------------");
	}
	
	public static String getExperimentDescription(File folder, int datasetId, Classifier algorithm, int seed) {
		try {
			return algorithm.getClass().getName() + "-" + getAvailableDatasets(folder).get(datasetId).getName() + "-" + seed;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<File> getAvailableDatasets(File folder) throws IOException {
		List<File> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(folder.toPath())) {
			paths.filter(f -> f.getParent().toFile().equals(folder) && f.toFile().getAbsolutePath().endsWith(".arff")).forEach(f -> files.add(f.toFile()));
		}
		return files.stream().sorted().collect(Collectors.toList());
	}

	public static Instances getKthInstances(File folder, int k) throws IOException {
		File f = getAvailableDatasets(folder).get(k);
		System.out.println("Selecting " + f);
		return new Instances(new BufferedReader(new FileReader(f)));
	}
}
