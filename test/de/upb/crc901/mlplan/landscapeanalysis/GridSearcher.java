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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import autoweka.ClassParams;
import autoweka.Conditional;
import autoweka.Parameter;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import de.upb.crc901.automl.pipeline.service.MLPipelinePlan;
import de.upb.crc901.services.core.HttpServiceServer;
import jaicore.basic.FileUtil;
import jaicore.basic.MySQLAdapter;
import jaicore.basic.SetUtil;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class GridSearcher {
	
	private static final Logger logger = LoggerFactory.getLogger(GridSearcher.class);

	private static final MySQLAdapter dbAdapter = new MySQLAdapter("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "landscapeanalysises");

	private static class JobResult {
		ArrayNode trainingData;
		Classifier c;
		double errorRate;
		double rankError;
		int time;
		Throwable e;

		public JobResult(ArrayNode trainingData, Classifier c, double errorRate, double rankError, int time, Throwable e) {
			super();
			this.trainingData = trainingData;
			this.c = c;
			this.errorRate = errorRate;
			this.rankError = rankError;
			this.time = time;
			this.e = e;
		}

	}

	private static class Job implements Runnable {

		private String searcherAlgorithm, searcherOptions, evaluatorAlgorithm, evaluatorOptions, classifierAlgorithm, classifierOptions;
		private int dataSplitId;
		private int seed;
		private Instances data;

		public Job(String searcherAlgorithm, String searcherOptions, String evaluatorAlgorithm, String evaluatorOptions, String classifierAlgorithm, String classifierOptions, int dataSplitId, int seed, Instances data) {
			super();
			this.searcherAlgorithm = searcherAlgorithm;
			this.searcherOptions = searcherOptions;
			this.evaluatorAlgorithm = evaluatorAlgorithm;
			this.evaluatorOptions = evaluatorOptions;
			this.classifierAlgorithm = classifierAlgorithm;
			this.classifierOptions = classifierOptions;
			this.dataSplitId = dataSplitId;
			this.seed = seed;
			this.data = data;
		}

		@Override
		public void run() {
			try {

				String entry = encodeEntry(dataSplitId, seed, searcherAlgorithm, searcherOptions, evaluatorAlgorithm, evaluatorOptions, classifierAlgorithm, classifierOptions);

				/* create experiment */
				int id = createExperimentIfDoesNotExist(dataSplitId, seed, searcherAlgorithm, searcherOptions, evaluatorAlgorithm, evaluatorOptions, classifierAlgorithm, classifierOptions);
				JobResult result = executeRun(this.data, seed, searcherAlgorithm, searcherOptions, evaluatorAlgorithm, evaluatorOptions, classifierAlgorithm, classifierOptions);

				updateExperiment(id, result.trainingData, result.errorRate, result.rankError, result.time, result.e);
				if (result.c != null) {

					/* serialize trained classifier */
//					File folder = new File("serializations");
//					folder.mkdirs();
//					FileUtil.serializeObject(result.c, folder + File.separator + id + ".classifier");
				}
			}

			catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	private static int port;

	private static JobResult executeRun(Instances data, int seed, String searcherAlgorithm, String searcherOptions, String evaluatorAlgorithm, String evaluatorOptions, String classifierAlgorithm, String classifierOptions)
			throws Exception {

		/* prepare data and create split */
		Collection<Integer>[] splitDecision = WekaUtil.getStratifiedSplitIndices(data, new Random(seed), .7f);
		List<Instances> split = WekaUtil.realizeSplit(data, splitDecision);
		ObjectMapper om = new ObjectMapper();
		ArrayNode an = om.createArrayNode();
		splitDecision[0].stream().sorted().forEach(v -> an.add(v));

		/* create service base classifier */
//		MLPipelinePlan plan = new MLPipelinePlan();
//		plan.onHost("localhost:" + port);
		ASSearch searcher = null;
		ASEvaluation evaluation = null;
		if (!searcherAlgorithm.isEmpty()) {
			searcher = ASSearch.forName(searcherAlgorithm, new String[] {});
			evaluation = ASEvaluation.forName(evaluatorAlgorithm, new String[] {});
//			plan.addWekaAttributeSelection(searcher, evaluation);
		}
//		plan.setClassifier(classifierAlgorithm).addOptions(classifierOptions);
		Classifier baseClassifier = AbstractClassifier.forName(classifierAlgorithm, new String[] {});
		long timeStart = System.currentTimeMillis();
		try {
//			logger.info("Create pipeline for plan {}", plan);
			MLPipeline c = new MLPipeline(searcher, evaluation, baseClassifier);
			
			/* run experiment */
			System.out.println("Computing performance of " + c + " for seed " + seed + " ... ");
			TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
			ts.interruptMeAfterMS(1000 * 60 * 60);
			logger.info("Building classifier");
			c.buildClassifier(split.get(0));
			logger.info("ready");
			long timeEnd = System.currentTimeMillis();

			MulticlassEvaluator eval = new MulticlassEvaluator(new Random(seed));
			double errorRate = eval.loss(c, split.get(1));
			double rankLoss = -1;
			logger.info("Computed error rate: {}", errorRate);
			return new JobResult(an, c, errorRate, rankLoss, (int) (timeEnd - timeStart), null);
		} catch (Throwable e) {
			long timeEnd = System.currentTimeMillis();
			logger.error("Received an exception: {}, {}, {}", e.getClass().getName(), e.getMessage(), e.getCause());
			e.printStackTrace();
			return new JobResult(an, null, -1, -1, (int) (timeEnd - timeStart), e instanceof RuntimeException ? ((RuntimeException)e).getCause() : e);

		}

	}

	public static void main(String[] args) throws Throwable {

		List<String> classifierNames = new ArrayList<>(WekaUtil.getBasicLearners());
		Collections.shuffle(classifierNames);

//		classifierNames.clear();
//		classifierNames.add("weka.classifiers.functions.SMO");
		
		List<File> datasets = getAvailableDatasets(new File(args[0]));
		Collections.shuffle(datasets);
		
		/* launch JASE Server */
		port = 8000 + (int)(Math.random() * 1000);
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
		
		List<Collection<?>> objects = new ArrayList<>();
		objects.add(datasets);
		objects.add(classifierNames);
		
		System.out.println(datasets.size() * classifierNames.size() + " many experiments.");
		
		for (File dataset : datasets) {
			Instances allInstances = new Instances(new BufferedReader(new FileReader(dataset)));
			allInstances.setClassIndex(allInstances.numAttributes() - 1);

			for (int seed = 0; seed < 20; seed++) {

				Collection<Integer>[] splitDecision = WekaUtil.getArbitrarySplit(allInstances, new Random(seed), .7f);
				System.out.println("Considering dataset " + dataset.getName());
				int splitId = getOuterSplit(dataset.getName(), WekaUtil.splitToJsonArray(splitDecision));
				Instances inst = WekaUtil.realizeSplit(allInstances, splitDecision).get(0);
				
				/* get update of used keys */
				List<String> evaluators = new ArrayList<>(WekaUtil.getFeatureEvaluators());
				Collections.shuffle(evaluators);
				String searcher = "";
				for (String evaluator : evaluators) {
					if (evaluator != null) {
						switch (evaluator) {
						case "weka.attributeSelection.PrincipalComponents":
						case "weka.attributeSelection.CorrelationAttributeEval":
						case "weka.attributeSelection.GainRatioAttributeEval":
						case "weka.attributeSelection.InfoGainAttributeEval":
						case "weka.attributeSelection.OneRAttributeEval":
						case "weka.attributeSelection.ReliefFAttributeEval":
						case "weka.attributeSelection.SymmetricalUncertAttributeEval":
							searcher = "weka.attributeSelection.Ranker";
							break;
						case "weka.attributeSelection.CfsSubsetEval":
							searcher = Math.random() < 0.5 ? "weka.attributeSelection.BestFirst" : "weka.attributeSelection.GreedyStepwise";
							break;
						default:
						}
					}
					
					for (String classifierName : classifierNames) {
//						File configFile = new File(configFolder + "/" + classifierName + ".params");
//						Collection<Map<String, String>> combos = getAllConsideredParamCombos(configFile, configFolder, 10000);
						Collection<Map<String, String>> combos = new ArrayList<>();
						combos.add(new HashMap<>());
						for (Map<String, String> classifierParamsAsMap : combos) {
							String classifierParams = paramMapToString(classifierParamsAsMap);
							String entry = encodeEntry(splitId, seed, searcher, "", evaluator, "", classifierName, classifierParams);
							if (usedKeys.contains(entry)) {
								// System.out.println("Skipping " + entry);
								continue;
							}
							String dsEntry = encodeDSEntry(dataset.getName(), searcher, "", evaluator, "", classifierName, classifierParams);
							if (failedKeys.contains(dsEntry)) {
								logger.info("Skipping {} because a similar one has raised an issue", dsEntry);
								continue;
							}
							new Job(searcher, "", evaluator, "", classifierName, classifierParams, splitId, seed, inst).run();
						}
					}
				}
			}
		}
		System.out.println("Ready");
		dbAdapter.close();
	}

	private static String encodeEntry(int splitId, int seed, String searcherAlgorithm, String searcherOptions, String evaluatorAlgorithm, String evaluatorOptions, String classifierAlgorithm, String classifierOptions) {
		return splitId + ";" + ";" + seed + ";" + searcherAlgorithm + ";" + searcherOptions + ";" + evaluatorAlgorithm + ";" + evaluatorOptions + ";" + classifierAlgorithm + ";" + classifierOptions;
	}

	private static String encodeDSEntry(String dataset,String searcherAlgorithm, String searcherOptions, String evaluatorAlgorithm, String evaluatorOptions, String classifierAlgorithm, String classifierOptions) {
		return dataset + ";" +searcherAlgorithm + ";" + searcherOptions + ";" + evaluatorAlgorithm + ";" + evaluatorOptions + ";" + classifierAlgorithm + ";" + classifierOptions;
	}

	private static Set<String> getUsedKeys() throws SQLException {
		ResultSet rs = dbAdapter.getResultsOfQuery("SELECT split_id, seed, searcher_algorithm, searcher_options, evaluator_algorithm, evaluator_options, classifier_algorithm, classifier_options FROM gridsearch_mc");
		Set<String> keys = new HashSet<>();
		while (rs.next()) {
			keys.add(encodeEntry(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)));
		}
		return keys;
	}

	private static Set<String> getFailedKeys() throws SQLException {
		ResultSet rs = dbAdapter.getResultsOfQuery(
				"SELECT dataset,searcher_algorithm, searcher_options, evaluator_algorithm, evaluator_options, classifier_algorithm, classifier_options, exception FROM gridsearch_mc JOIN outerdatasetsplits USING(split_id) WHERE exception <> \"\"");
		Set<String> keys = new HashSet<>();
		while (rs.next()) {
			keys.add(encodeDSEntry(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)));
		}
		return keys;
	}

	private static void updateExperiment(int id, ArrayNode trainingInstances, double errorRate, double rankError, int trainingTimeInMS, Throwable e)
			throws SQLException {
		String[] values = { String.valueOf(errorRate), String.valueOf(rankError),
				String.valueOf(trainingTimeInMS), e != null ? e.getClass().getName() + "\n" + e.getMessage() : "", String.valueOf(id) };
		dbAdapter.update("UPDATE `gridsearch_mc` SET errorRate = ?, rank_error = ?, training_time_ms = ?, exception = ? WHERE id = ?",
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

	private static int createExperimentIfDoesNotExist(int splitId, int seed, String searcherAlgorithm, String searcherOptions, String evaluatorAlgorithm, String evaluatorOptions, String classifierAlgorithm,
			String classifierOptions) throws SQLException {
		String[] values = { String.valueOf(splitId), String.valueOf(seed), searcherAlgorithm, searcherOptions, evaluatorAlgorithm
				, evaluatorOptions, classifierAlgorithm, classifierOptions };
		return dbAdapter.insert(
				"INSERT INTO `gridsearch_mc` (split_id, seed, searcher_algorithm, searcher_options, evaluator_algorithm, evaluator_options, classifier_algorithm, classifier_options) VALUES (?,?,?,?,?,?,?,?)",
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
