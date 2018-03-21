package de.upb.crc901.mlplan.landscapeanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import autoweka.ClassParams;
import autoweka.Conditional;
import autoweka.Parameter;
import de.upb.crc901.mlplan.services.MLPipelinePlan;
import de.upb.crc901.mlplan.services.MLServicePipeline;
import de.upb.crc901.services.core.HttpServiceServer;
import jaicore.basic.FileUtil;
import jaicore.basic.MathExt;
import jaicore.basic.MySQLAdapter;
import jaicore.basic.SetUtil;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class GridSearcher {
	
	private static final Logger logger = LoggerFactory.getLogger(GridSearcher.class);

	private static final MySQLAdapter dbAdapter = new MySQLAdapter("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "landscapeanalysises");

	private static class JobResult {
		ArrayNode trainingData;
		Classifier c;
		int tp, tn, fp, fn;
		double rankError;
		int time;
		Throwable e;

		public JobResult(ArrayNode trainingData, Classifier c, int tp, int tn, int fp, int fn, double rankError, int time, Throwable e) {
			super();
			this.trainingData = trainingData;
			this.c = c;
			this.tp = tp;
			this.tn = tn;
			this.fp = fp;
			this.fn = fn;
			this.rankError = rankError;
			this.time = time;
			this.e = e;
		}

	}

	private static class Job implements Runnable {

		private String preprocessorAlgorithm, preprocessorOptions, classifierAlgorithm, classifierOptions;
		private int dataSplitId;
		private int seed;
		private Instances data;
		private Semaphore computationTickets;

		public Job(String preprocessorAlgorithm, String preprocessorOptions, String classifierAlgorithm, String classifierOptions, int dataSplitId, int seed, Instances data, Semaphore computationTickets) {
			super();
			this.preprocessorAlgorithm = preprocessorAlgorithm;
			this.preprocessorOptions = preprocessorOptions;
			this.classifierAlgorithm = classifierAlgorithm;
			this.classifierOptions = classifierOptions;
			this.dataSplitId = dataSplitId;
			this.seed = seed;
			this.data = data;
			this.computationTickets = computationTickets;
		}

		@Override
		public void run() {
			try {

				String entry = encodeEntry(dataSplitId, seed, preprocessorAlgorithm, preprocessorOptions, classifierAlgorithm, classifierOptions);

				/* create experiment */
				int id = createExperimentIfDoesNotExist(dataSplitId, seed, preprocessorAlgorithm, preprocessorOptions, classifierAlgorithm, classifierOptions);
				JobResult result = executeRun(this.data, seed, preprocessorAlgorithm, preprocessorOptions, classifierAlgorithm, classifierOptions);

				updateExperiment(id, result.trainingData, result.tp, result.tn, result.fp, result.fn, result.rankError, result.time, result.e);
				if (result.c != null) {

					/* serialize trained classifier */
					File folder = new File("serializations");
					folder.mkdirs();
					FileUtil.serializeObject(result.c, folder + File.separator + id + ".classifier");
				}
			}

			catch (Throwable e) {
				e.printStackTrace();
			}
			finally {
				computationTickets.release();
			}
		}
	}

	private static JobResult executeRun(Instances data, int seed, String preprocessorAlgorithm, String preprocessorOptions, String classifierAlgorithm, String classifierOptions)
			throws Exception {

		/* prepare data and create split */
		Collection<Integer>[] splitDecision = WekaUtil.getStratifiedSplitIndices(data, new Random(seed), .7f);
		List<Instances> split = WekaUtil.realizeSplit(data, splitDecision);
		ObjectMapper om = new ObjectMapper();
		ArrayNode an = om.createArrayNode();
		splitDecision[0].stream().sorted().forEach(v -> an.add(v));

		/* create service base classifier */
		MLPipelinePlan plan = new MLPipelinePlan();
		plan.onHost("localhost:8000");
		if (!preprocessorAlgorithm.isEmpty())
			plan.addAttributeSelection(preprocessorAlgorithm).addOptions(preprocessorOptions);
		plan.setClassifier(classifierAlgorithm).addOptions(classifierOptions);
		long timeStart = System.currentTimeMillis();
		try {
			logger.info("Create pipeline for plan {}", plan);
			MLServicePipeline c = new MLServicePipeline(plan);
			
			/* run experiment */
			System.out.println("Computing performance of " + c + " for seed " + seed + " ... ");
			TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
			ts.interruptMeAfterMS(1000 * 60 * 60);
			logger.info("Building classifier");
			c.buildClassifier(split.get(0));
			logger.info("ready");
			long timeEnd = System.currentTimeMillis();

			Instances testData = split.get(1);
			double[] predictions = c.classifyInstances(testData);
			
			/* compute basic metrics and the probabilities to predict 0 for each instance */
			int tp = 0, fp = 0, tn = 0, fn = 0;
			double[] predictedProbabilitiesForOne = new double[testData.size()];
			boolean[] actualIsOne = new boolean[testData.size()];
			for (int i = 0; i < testData.size(); i++) {
				Instance inst = testData.get(i);
				actualIsOne[i] = (inst.classValue() == 1);
				double prediction = predictions[i];
				predictedProbabilitiesForOne[i] = prediction;
				if (inst.classValue() == 0) {
					if (prediction == 0)
						tn++;
					else
						fp++;
				} else {
					if (prediction == 0)
						fn++;
					else
						tp++;
				}
			}
			
			/* compute rank loss */
			List<Set<Integer>> instancePairs = SetUtil.getAllPossibleSubsetsWithSize(ContiguousSet.create(Range.closed(0, testData.size() - 1), DiscreteDomain.integers()).asList(),
					2);
			double mistakes = 0;
			int differentPairs = 0;
			for (Set<Integer> pair : instancePairs) {
				Iterator<Integer> it = pair.iterator();
				int x = it.next();
				int y = it.next();
				double xProb = predictedProbabilitiesForOne[x];
				double yProb = predictedProbabilitiesForOne[y];
				boolean xTrue = actualIsOne[x];
				boolean yTrue = actualIsOne[y];
				if (xTrue == yTrue)
					continue;
				differentPairs++;
				if (Math.abs(xProb - yProb) < .000001)
					mistakes += 0.5;
				else if (xTrue && xProb < yProb)
					mistakes++;
				else if (yTrue && yProb < xProb)
					mistakes++;
			}
			double rankLoss = MathExt.round(mistakes * 1f / differentPairs, 4);
			logger.info("Computed tp, tn, fp, fn: {}, {}, {}, {}", tp, tn, fp, fn);
			return new JobResult(an, c, tp, tn, fp, fn, rankLoss, (int) (timeEnd - timeStart), null);
		} catch (Throwable e) {
			long timeEnd = System.currentTimeMillis();
			logger.error("Received an exception: {}, {}, {}", e.getClass().getName(), e.getMessage(), e.getCause());
			return new JobResult(an, null, -1, -1, -1, -1, -1, (int) (timeEnd - timeStart), e instanceof RuntimeException ? ((RuntimeException)e).getCause() : e);

		}

	}

	public static void main(String[] args) throws Throwable {

		Collection<String> classifierNames = WekaUtil.getBasicLearners();

//		classifierNames.clear();
//		classifierNames.add("weka.classifiers.functions.SMO");
		
		List<File> datasets = getAvailableDatasets(new File(args[0]));
		Collections.shuffle(datasets);
		
		/* launch JASE Server */
		int port = 8000;
		HttpServiceServer server = new HttpServiceServer(port, "testrsc/conf/classifiers.json", "testrsc/conf/others.json", "testrsc/conf/preprocessors.json");
		MLPipelinePlan.hostJASE = "localhost:" + port;
		MLPipelinePlan.hostPASE = "localhost:5000";

		/* get used keys to directly skip resolved ones */
		File configFolder = new File(args[1]);
		Set<String> usedKeys = getUsedKeys();
		Set<String> failedKeys = getFailedKeys();
		System.out.println("Considering " + failedKeys.size() + " keys of failed runs");
		System.out.println(failedKeys);
		int experiments = 0;
		ExecutorService pool = Executors.newFixedThreadPool(8);
		Semaphore computationTickets = new Semaphore(8);
		
		for (File dataset : datasets) {
			Instances allInstances = new Instances(new BufferedReader(new FileReader(dataset)));
			allInstances.setClassIndex(allInstances.numAttributes() - 1);

			for (int seed = 0; seed < 1; seed++) {

				Collection<Integer>[] splitDecision = WekaUtil.getArbitrarySplit(allInstances, new Random(seed), .7f);
				System.out.println("Considering dataset " + dataset.getName());
				int splitId = getOuterSplit(dataset.getName(), WekaUtil.splitToJsonArray(splitDecision));
				Instances inst = WekaUtil.realizeSplit(allInstances, splitDecision).get(0);
				
				/* get update of used keys */
				for (String preprocessor : new String[] { "" }) {
					for (String classifierName : classifierNames) {
						File configFile = new File(configFolder + "/" + classifierName + ".params");

//						Collection<Map<String, String>> combos = getAllConsideredParamCombos(configFile, configFolder, 10000);
						Collection<Map<String, String>> combos = new ArrayList<>();
						combos.add(new HashMap<>());
						for (Map<String, String> classifierParamsAsMap : combos) {
							experiments ++;
							String classifierParams = paramMapToString(classifierParamsAsMap);
							String entry = encodeEntry(splitId, seed, preprocessor, "", classifierName, classifierParams);
							if (usedKeys.contains(entry)) {
								// System.out.println("Skipping " + entry);
								continue;
							}
							String dsEntry = encodeDSEntry(dataset.getName(), preprocessor, "", classifierName, classifierParams);
							if (failedKeys.contains(dsEntry)) {
								logger.info("Skipping {} because a similar one has raised an issue", dsEntry);
								continue;
							}
							System.out.print("Scheduling job ...");
							computationTickets.acquire();
							System.out.println(" done");
							pool.submit(new Job(preprocessor, "", classifierName, classifierParams, splitId, seed, inst, computationTickets));
						}
					}
				}
			}
		}
		pool.shutdown();
		System.out.println("Waiting one month for termination of " + experiments + " jobs.");
		pool.awaitTermination(30, TimeUnit.DAYS);
		System.out.println("Ready");
		dbAdapter.close();
	}

	private static String encodeEntry(int splitId, int seed, String preprocessorAlgorithm, String preprocessorOptions, String classifierAlgorithm, String classifierOptions) {
		return splitId + ";" + ";" + seed + ";" + preprocessorOptions + ";" + preprocessorAlgorithm + ";" + classifierAlgorithm + ";" + classifierOptions;
	}

	private static String encodeDSEntry(String dataset, String preprocessorAlgorithm, String preprocessorOptions, String classifierAlgorithm, String classifierOptions) {
		return dataset + ";" + preprocessorOptions + ";" + preprocessorAlgorithm + ";" + classifierAlgorithm + ";" + classifierOptions;
	}

	private static Set<String> getUsedKeys() throws SQLException {
		ResultSet rs = dbAdapter.getResultsOfQuery("SELECT split_id, seed, preprocessor_algorithm, preprocessor_options, classifier_algorithm, classifier_options FROM gridsearch_mc");
		Set<String> keys = new HashSet<>();
		while (rs.next()) {
			keys.add(encodeEntry(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)));
		}
		return keys;
	}

	private static Set<String> getFailedKeys() throws SQLException {
		ResultSet rs = dbAdapter.getResultsOfQuery(
				"SELECT dataset,preprocessor_algorithm, preprocessor_options, classifier_algorithm, classifier_options, exception FROM gridsearch_mc JOIN outerdatasetsplits USING(split_id) WHERE exception <> \"\"");
		Set<String> keys = new HashSet<>();
		while (rs.next()) {
			keys.add(encodeDSEntry(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
		}
		return keys;
	}

	private static void updateExperiment(int id, ArrayNode trainingInstances, int tp, int tn, int fp, int fn, double rankError, int trainingTimeInMS, Throwable e)
			throws SQLException {
		String[] values = { trainingInstances.toString(), String.valueOf(tp), String.valueOf(tn), String.valueOf(fp), String.valueOf(fn), String.valueOf(rankError),
				String.valueOf(trainingTimeInMS), e != null ? e.getClass().getName() + "\n" + e.getMessage() : "", String.valueOf(id) };
		dbAdapter.update("UPDATE `gridsearch_mc` SET training_indices = ?, tp = ?, tn = ?, fp = ?, fn = ?, rank_error = ?, training_time_ms = ?, exception = ? WHERE id = ?",
				values);
	}

	private static int getOuterSplit(String dataset, ArrayNode trainingIndices) throws SQLException {

		/* first determine whether the split exists */
		String[] values = { dataset, trainingIndices.toString() };
		ResultSet rs1 = dbAdapter.getResultsOfQuery("SELECT `split_id` FROM `outerdatasetsplits` WHERE dataset = ? AND training_indices = ?", values);
		if (rs1.next()) {
			return rs1.getInt(1);
		} else {
			return dbAdapter.insert("INSERT INTO `outerdatasetsplits` (dataset, training_indices) VALUES (?,?)", values);
		}
	}

	private static int createExperimentIfDoesNotExist(int splitId, int seed, String preprocessorAlgorithm, String preprocessorOptions, String classifierAlgorithm,
			String classifierOptions) throws SQLException {
		String[] values = { String.valueOf(splitId), String.valueOf(seed), preprocessorAlgorithm, preprocessorOptions, classifierAlgorithm, classifierOptions };
		return dbAdapter.insert(
				"INSERT INTO `gridsearch_mc` (split_id, seed, preprocessor_algorithm, preprocessor_options, classifier_algorithm, classifier_options) VALUES (?,?,?,?,?,?)",
				values);
	}

	public static List<File> getAvailableDatasets(File folder) throws IOException {
		List<File> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(folder.toPath())) {
			paths.filter(f -> f.getParent().toFile().equals(folder) && f.toFile().getAbsolutePath().endsWith(".arff")).forEach(f -> files.add(f.toFile()));
		}
		return files.stream().sorted().collect(Collectors.toList());
	}

	private static Instances turnToSingleClassProblem(Instances data, int labelIndex) {
		int numberOfLabels = data.classIndex();
		Instances copy = new Instances(data);
		for (int j = 0; j < numberOfLabels; j++) {
			if (j < labelIndex)
				copy.deleteAttributeAt(0);
			else if (j > labelIndex)
				copy.deleteAttributeAt(1);
		}
		copy.setClassIndex(0);
		return copy;
	}

	private static Collection<Map<String, String>> getAllConsideredParamCombos(File configFile, File configFolder, int maxExperiments) {

		Collection<Map<String, String>> combos = new ArrayList<>();

		if (!configFile.exists()) {
			System.out.println("no param file defined!");
			combos.add(new HashMap<>());
			return combos;
		}

		final int discretizationLevels = 20;

		/* compute all parameters */
		// Map<Parameter,String> paramKeys = new HashMap<>();
		ClassParams cp = new ClassParams(configFile.getAbsolutePath());
		List<Parameter> allParameters = cp.getParameters();
		// allParameters.forEach(p -> paramKeys.put(p.name));
		if (allParameters.isEmpty()) {
			System.out.println("Algorithm has no parameters!");
			combos.add(new HashMap<>());
			return combos;
		}

		/* split parameters into hidden, conditional, and normal ones */
		List<Conditional> conditionals = cp.getConditionals();
		Map<Parameter, Conditional> conditionMap = new HashMap<>();
		conditionals.forEach(c -> conditionMap.put(c.parameter, c));
		List<Parameter> hiddenParameters = allParameters.stream().filter(p -> p.name.toLowerCase().contains("hidden")).collect(Collectors.toList());
		List<Parameter> conditionalParameters = allParameters.stream().filter(p -> conditionMap.containsKey(p)).collect(Collectors.toList());
		List<Parameter> normalParameters = new ArrayList<>(SetUtil.difference(allParameters, SetUtil.union(hiddenParameters, conditionalParameters)));

		/* compute choices for hidden parameters */
		Collection<List<String>> hiddenParamCombinations = new ArrayList<>();
		if (hiddenParameters.isEmpty()) {
			hiddenParamCombinations.add(new ArrayList<>());
		} else {
			List<Collection<String>> values = new ArrayList<>();
			hiddenParameters.forEach(p -> values.add(getValuesOfParam(p, discretizationLevels, configFolder)));
			hiddenParamCombinations.clear();
			hiddenParamCombinations.addAll(SetUtil.cartesianProduct(values));
		}

		/* now compute the actual parameter arrays for any choice of the hidden parameters */
		List<String> normalParamKeys = normalParameters.stream().map(p -> p.name.substring(p.name.lastIndexOf("_") + 1)).collect(Collectors.toList());
		List<Collection<String>> normalParamValues = normalParameters.stream().map(p -> getValuesOfParam(p, discretizationLevels, configFolder)).collect(Collectors.toList());
		for (List<String> hiddenParamValues : hiddenParamCombinations) {

			/* make map of values of hidden parameters explicit */
			Map<Parameter, String> hiddenParameterMap = new HashMap<>();
			for (int i = 0; i < hiddenParameters.size(); i++) {
				hiddenParameterMap.put(hiddenParameters.get(i), hiddenParamValues.get(i));
			}

			/* for this specific choice of hidden parameters, compute sets of possible values of conditional and normal parameters */
			List<Collection<String>> values = new ArrayList<>();
			List<String> paramKeys = new ArrayList<>();
			for (Parameter p : conditionalParameters) {
				Conditional c = conditionMap.get(p);
				if (c.domain.contains(hiddenParameterMap.get(c.parent))) {
					paramKeys.add(p.name.substring(p.name.lastIndexOf("_") + 1));
					values.add(getValuesOfParam(p, discretizationLevels, configFolder));
				}
			}
			paramKeys.addAll(normalParamKeys);
			values.addAll(normalParamValues);

			/* now compute the actual combinations */
			Collection<List<String>> cartesianProduct = SetUtil.cartesianProduct(values);
			final int n = paramKeys.size();
			for (List<String> valueCombo : cartesianProduct) {
				Map<String, String> candidate = new HashMap<>();
				for (int i = 0; i < n; i++) {
					candidate.put(paramKeys.get(i), valueCombo.get(i));
				}
				combos.add(candidate);
			}
		}
		return combos;
	}

	private static Collection<String> getValuesOfParam(Parameter p, int discretizationLevel, File folderOfSubQuoteSpecifications) {
		if (p.name.startsWith("SUBPARAMSQUOTED_")) {
			File folder = new File(folderOfSubQuoteSpecifications + File.separator + p.categoricalInnards.get(0));

			/* iterate over each option in the folder */
			List<String> values = new ArrayList<>();
			for (File file : FileUtil.getFilesOfFolder(folder)) {
				String algoName = file.getName();
				algoName = algoName.substring(0, algoName.lastIndexOf("."));
				for (Map<String, String> subParametrization : getAllConsideredParamCombos(file, folderOfSubQuoteSpecifications, 1000)) {
					values.add("\"" + algoName + " " + paramMapToString(subParametrization) + "\"");
				}
			}
			return values;
		} else {
			switch (p.type) {
			case CATEGORICAL:
				if (p.categoricalInnards.get(0).toLowerCase().contains("remove")) {
					return p.categoricalInnards.stream().map(s -> s.equals("REMOVED") ? "true" : "false").collect(Collectors.toList());
				}
				return p.categoricalInnards;

			case INTEGER:
			case LOG_INTEGER: {
				return p.getDiscretization((int) Math.min(discretizationLevel, p.maxNumeric - p.minNumeric + 1));
			}
			case NUMERIC:
			case LOG_NUMERIC: {
				return p.getDiscretization(discretizationLevel);
			}
			default:
				throw new IllegalArgumentException("No support for parameter " + p.name + " of type " + p.type);
			}
		}
	}

	public static String paramMapToString(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		for (String s : paramMapToArray(map)) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public static String[] paramMapToArray(Map<String, String> map) {
		List<String> optionList = new ArrayList<>();
		for (String key : map.keySet()) {
			String val = map.get(key);
			if (val.equals("false"))
				continue;
			if (val.equals("true"))
				optionList.add("-" + key);
			else {
				optionList.add("-" + key);
				optionList.add(val);
			}
		}
		return optionList.toArray(new String[] {});
	}
}
