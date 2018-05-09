package de.upb.crc901.mlplan.acml2018;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.common.util.concurrent.AtomicDouble;

import de.upb.crc901.automl.hascowekaml.HASCOForMEKA;
import de.upb.crc901.automl.hascowekaml.HASCOForMEKA.HASCOForMEKASolution;
import de.upb.crc901.mlplan.multilabel.MultiLabelMySQLHandle;
import hasco.serialization.ComponentLoader;
import jaicore.basic.StringUtil;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.ml.experiments.Experiment;
import jaicore.ml.multilabel.evaluators.ExactMatchMultilabelEvaluator;
import jaicore.ml.multilabel.evaluators.F1AverageMultilabelEvaluator;
import jaicore.ml.multilabel.evaluators.HammingMultilabelEvaluator;
import jaicore.ml.multilabel.evaluators.JaccardMultilabelEvaluator;
import jaicore.ml.multilabel.evaluators.MultilabelEvaluator;
import jaicore.ml.multilabel.evaluators.RankMultilabelEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;
import meka.classifiers.multilabel.AbstractMultiLabelClassifier;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Instances;
import weka.core.OptionHandler;

public class ACML2018 {

	// @Test
	public void MLPlanTest() throws Exception {

		/* read data and split */
		Instances data = new Instances(new BufferedReader(new FileReader(new File("../ML-Plan/testrsc/multilabel/flags.arff"))));
		Collections.shuffle(data);
		try {
			MLUtils.prepareData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Instances> split = WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, new Random(0), .7f));

		HASCOForMEKA hasco = new HASCOForMEKA();
		// mlplan.registerListener(new HASCOSQLEventLogger<>(new MySQLAdapter("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "hasco")));
		// mlplan.registerListener(new HASCOSQLEventLogger<>(new MySQLAdapter("hsqldb:hsql", "localhost", "SA", "", "testdb", new Properties())));

		new SimpleGraphVisualizationWindow<Node<TFDNode, Double>>(hasco).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
		// hasco.setPreferredNodeEvaluator(n -> n.externalPath().size() * 1.0);

		hasco.setNumberOfCPUs(8);
		hasco.gatherSolutions(split.get(0), 60 * 1000 * 1);
		HASCOForMEKASolution bestSolution = hasco.getCurrentlyBestSolution();
		System.out.println("Best solution: " + bestSolution.getClassifier().getClass().getName() + Arrays.toString(bestSolution.getClassifier().getOptions())
				+ " with base learner " + ((SingleClassifierEnhancer) bestSolution.getClassifier()).getClassifier().getClass().getName() + ". Score: " + bestSolution.getScore());

		MultilabelEvaluator eval = new F1AverageMultilabelEvaluator(new Random(0));
		double f1 = eval.getErrorRateForSplit(bestSolution.getClassifier(), split.get(0), split.get(1));
		System.out.println("External f1 error of this solution: " + f1);

		while (true)
			;
	}

	@Test
	public void RandomSearchTest() throws Exception {
		conductRandomSearch("../ML-Plan/testrsc/multilabel/flags.arff", 0, 1, 4 * 1024, 60);
	}

	public void conductRandomSearch(final String dataFile, final int seed, final int numCPUs, final int assumedMemoryOverhead, final int timeout) throws Exception {

		/* prepare data */
		Instances data = new Instances(new BufferedReader(new FileReader(new File(dataFile))));
		Collections.shuffle(data);
		MultiLabelMySQLHandle handle = new MultiLabelMySQLHandle("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "acml2018");

		try {
			MLUtils.prepareData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Evaluating dataset: " + data.relationName());

		Collection<Integer>[] overallSplitDecision = WekaUtil.getArbitrarySplit(data, new Random(seed), .7f);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.size(); i++) {
			sb.append(overallSplitDecision[0].contains(i) ? "1" : "0");
		}
		List<Instances> outerSplit = WekaUtil.realizeSplit(data, overallSplitDecision);
		String rows_for_search = StringUtil.fromBinary(sb.toString());

		/* load components */
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(new File("testrsc/acml2018/mlplan-multilabel.json"));
		Collection<String> componentNames = cl.getComponents().stream().map(c -> c.getName()).collect(Collectors.toList());

		Map<String, MultilabelEvaluator> evaluators = new HashMap<>();
		evaluators.put("f1", new F1AverageMultilabelEvaluator(new Random(seed)));
		evaluators.put("jaccard", new JaccardMultilabelEvaluator(new Random(seed)));
		evaluators.put("hamming", new HammingMultilabelEvaluator(new Random(seed)));
		evaluators.put("exact", new ExactMatchMultilabelEvaluator(new Random(seed)));
		evaluators.put("rank", new RankMultilabelEvaluator(new Random(seed)));

		List<String> mekaBaseClassifiers = componentNames.stream().filter(c -> c.contains("meka") && !c.contains("meta")).collect(Collectors.toList());
		List<String> mekaMetaClassifiers = componentNames.stream().filter(c -> c.contains("meka") && c.contains("meta")).collect(Collectors.toList());
		List<String> wekaBaseClassifiers = componentNames.stream().filter(c -> c.contains("weka") && !c.contains("meta") && !c.contains("attributeSelection") && !c.contains("supportVector"))
				.collect(Collectors.toList());
		List<String> wekaMetaClassifiers = componentNames.stream().filter(c -> c.contains("weka") && c.contains("meta")).collect(Collectors.toList());
		mekaMetaClassifiers.add(null);
		wekaMetaClassifiers.add(null);

		 System.out.println("\\item MEKA BASE");
		 mekaBaseClassifiers.forEach(s -> System.out.println("\t" + s.substring(s.lastIndexOf(".") + 1) + ","));
		//
		 System.out.println("\\item MEKA META");
		 mekaMetaClassifiers.forEach(s -> { if (s != null) System.out.println("\t" + s.substring(s.lastIndexOf(".") + 1) + ","); });
		//
		 System.out.println("\\item WEKA BASE ");
		 wekaBaseClassifiers.forEach(s -> System.out.println("\t" + s.substring(s.lastIndexOf(".") + 1) + ","));
		//
		 System.out.println("\\item WEKA META");
		 wekaMetaClassifiers.forEach(s -> { if (s != null) System.out.println("\t" + s.substring(s.lastIndexOf(".") + 1) + ","); });

		/* config */
		final int memoryInMB = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024) + assumedMemoryOverhead;

		int n1 = mekaBaseClassifiers.size();
		int n2 = mekaMetaClassifiers.size();
		int n3 = wekaBaseClassifiers.size();
		int n4 = wekaMetaClassifiers.size();

		int n = n1 * n2 * n3 * n4;
		System.out.println(n1 + " * " + n2 + " * " + n3 + " * " + n4 + " = " + n);

		long start = System.currentTimeMillis();

		AtomicDouble bestScore = new AtomicDouble(Double.MAX_VALUE);
		AtomicInteger bestSolution = new AtomicInteger(-1);
		AtomicInteger timeOfBestSolution = new AtomicInteger(-1);

		Experiment exp = new Experiment(data.relationName(), "RandomSearch", "0/1-Loss-70-30", seed, timeout, numCPUs, memoryInMB, "F1-Avg Instance Wise");
		int runId = handle.createRunIfDoesNotExist(exp);
		Map<String, String> rowsForTrainingMap = new HashMap<>();
		rowsForTrainingMap.put("rows_for_training", rows_for_search);
		handle.updateExperiment(exp, rowsForTrainingMap);

		Semaphore tickets = new Semaphore(0);
		int workers = Math.max(numCPUs - 1, 1);
		System.out.println("Searching with " + workers + " workers ...");
		for (int i = 0; i < workers; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					while ((System.currentTimeMillis() - start + timeOfBestSolution.get() * 1.4f) / 1000f < timeout) {
						int k = (int) Math.floor(Math.random() * n);
						try {

							/* train classifier */
							MultiLabelClassifier classifier = (MultiLabelClassifier) getClassifierForId(componentNames, k);

							System.out.println(classifier.getClass().getName());
							long start_train = System.currentTimeMillis();
							List<Instances> innerSplit = WekaUtil.realizeSplit(outerSplit.get(0), WekaUtil.getArbitrarySplit(outerSplit.get(0), new Random(seed), .7f));
							Map<String, Double> performances = evaluateSolution(classifier, innerSplit.get(0), innerSplit.get(1), evaluators);
							int time_train_ms = (int) (System.currentTimeMillis() - start_train);

							/* prepare database insertion */
							Map<String, String> databaseInsertion = new HashMap<>(getMapForExperimentEvaluation(classifier, performances));
							databaseInsertion.put("run_id", String.valueOf(runId));
							databaseInsertion.put("time_train_ms", String.valueOf(time_train_ms));
							System.out.println(databaseInsertion);
							handle.insert("evaluations_multilabel", databaseInsertion);

							/* check whether this is the new best */
							double loss = performances.get("f1");
							System.out.println(loss);
							synchronized (bestScore) {
								if (loss < bestScore.get()) {
									bestSolution.set(k);
									bestScore.set(loss);
									timeOfBestSolution.set(time_train_ms);
								}
							}
						} catch (Throwable e) {
							System.err.println(e.getMessage());
						}

					}
					tickets.release();
				}
			}).start();
		}

		System.out.println("Awaiting responses: ");
		tickets.tryAcquire(workers, timeout, TimeUnit.SECONDS);

		try {

			if (bestSolution.get() >= 0) {

				MultiLabelClassifier choice = (MultiLabelClassifier) getClassifierForId(componentNames, bestSolution.get());
				System.out.println("Beste Lösung: " + choice.getClass() + ". Training on all data.");

				Map<String, Double> performances = evaluateSolution(choice, outerSplit.get(0), outerSplit.get(1), evaluators);
				double testScore = performances.get("f1");
				System.out.println("Error Rate on test data: " + testScore);
				Map<String, String> databaseInsertion = new HashMap<>(getMapForExperimentEvaluation(choice, performances));
				databaseInsertion.put("run_id", String.valueOf(runId));
				handle.insert("results", databaseInsertion);
			} else {
				Map<String, Double> performances = new HashMap<>();
				evaluators.keySet().forEach(k -> performances.put(k, -1.0));
				System.out.println("No solution was found in the given time.");
				Map<String, String> databaseInsertion = new HashMap<>(getMapForExperimentEvaluation(null, performances));
				databaseInsertion.put("run_id", String.valueOf(runId));
				handle.insert("results", databaseInsertion);
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void conductGridSearch(final String dataFile, final int seed, final int numCPUs, final int assumedMemoryOverhead) throws Exception {

		/* prepare data */
		Instances data = new Instances(new BufferedReader(new FileReader(new File(dataFile))));
		Collections.shuffle(data);
		MultiLabelMySQLHandle handle = new MultiLabelMySQLHandle("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "acml2018");

		try {
			MLUtils.prepareData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Evaluating dataset: " + data.relationName());

		Collection<Integer>[] overallSplitDecision = WekaUtil.getArbitrarySplit(data, new Random(seed), .7f);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.size(); i++) {
			sb.append(overallSplitDecision[0].contains(i) ? "1" : "0");
		}
		List<Instances> outerSplit = WekaUtil.realizeSplit(data, overallSplitDecision);
		String rows_for_search = StringUtil.fromBinary(sb.toString());

		/* load components */
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(new File("testrsc/acml2018/mlplan-multilabel.json"));
		Collection<String> componentNames = cl.getComponents().stream().map(c -> c.getName()).collect(Collectors.toList());

		Map<String, MultilabelEvaluator> evaluators = new HashMap<>();
		evaluators.put("f1", new F1AverageMultilabelEvaluator(new Random(seed)));
		evaluators.put("jaccard", new JaccardMultilabelEvaluator(new Random(seed)));
		evaluators.put("hamming", new HammingMultilabelEvaluator(new Random(seed)));
		evaluators.put("exact", new ExactMatchMultilabelEvaluator(new Random(seed)));
		evaluators.put("rank", new RankMultilabelEvaluator(new Random(seed)));

		List<String> mekaBaseClassifiers = componentNames.stream().filter(c -> c.contains("meka") && !c.contains("meta")).collect(Collectors.toList());
		List<String> mekaMetaClassifiers = new ArrayList<>();
		List<String> wekaBaseClassifiers = componentNames.stream().filter(c -> c.contains("weka") && (c.contains("RandomForest") || c.contains("SMO")))
				.collect(Collectors.toList());
		List<String> wekaMetaClassifiers = new ArrayList<>();
		mekaMetaClassifiers.add(null);
		wekaMetaClassifiers.add(null);

		// System.out.println("MEKA BASE");
		// mekaBaseClassifiers.forEach(s -> System.out.println("\t" + s));
		//
		// System.out.println("MEKA META");
		// mekaMetaClassifiers.forEach(s -> System.out.println("\t" + s));
		//
		System.out.println("WEKA BASE ");
		wekaBaseClassifiers.forEach(s -> System.out.println("\t" + s));
		//
		// System.out.println("WEKA META");
		// wekaMetaClassifiers.forEach(s -> System.out.println("\t" + s));

		/* config */
		final int memoryInMB = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024) + assumedMemoryOverhead;

		int n1 = mekaBaseClassifiers.size();
		int n2 = mekaMetaClassifiers.size();
		int n3 = wekaBaseClassifiers.size();
		int n4 = wekaMetaClassifiers.size();

		int n = n1 * n2 * n3 * n4;

		long start = System.currentTimeMillis();

		AtomicDouble bestScore = new AtomicDouble(Double.MAX_VALUE);
		AtomicInteger bestSolution = new AtomicInteger(-1);
		AtomicInteger timeOfBestSolution = new AtomicInteger(-1);

		Experiment exp = new Experiment(data.relationName(), "SmallGridSearch", "0/1-Loss-70-30", seed, 30 * 3600, numCPUs, memoryInMB, "F1-Avg Instance Wise");
		int runId = handle.createRunIfDoesNotExist(exp);
		Map<String, String> rowsForTrainingMap = new HashMap<>();
		rowsForTrainingMap.put("rows_for_training", rows_for_search);
		handle.updateExperiment(exp, rowsForTrainingMap);

		Semaphore tickets = new Semaphore(0);
		int workers = Math.max(numCPUs - 1, 1);
		System.out.println("Searching grid of size " + n + " with " + workers + " workers ...");
		Set<Integer> solvedIndices = new HashSet<>();
		for (int i = 0; i < workers; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (true) {

							/* determine the k of this round */
							int k = 0;
							synchronized (solvedIndices) {
								while (solvedIndices.contains(k))
									k++;
							}
							if (k >= n)
								break;

							try {

								/* train classifier */
								MultiLabelClassifier classifier = (MultiLabelClassifier) getClassifierForId(componentNames, k);

								System.out.println(classifier.getClass().getName());
								long start_train = System.currentTimeMillis();
								List<Instances> innerSplit = WekaUtil.realizeSplit(outerSplit.get(0), WekaUtil.getArbitrarySplit(outerSplit.get(0), new Random(seed), .7f));
								Map<String, Double> performances = evaluateSolution(classifier, innerSplit.get(0), innerSplit.get(1), evaluators);
								int time_train_ms = (int) (System.currentTimeMillis() - start_train);

								/* prepare database insertion */
								Map<String, String> databaseInsertion = new HashMap<>(getMapForExperimentEvaluation(classifier, performances));
								databaseInsertion.put("run_id", String.valueOf(runId));
								databaseInsertion.put("time_train_ms", String.valueOf(time_train_ms));
								System.out.println(databaseInsertion);
								handle.insert("evaluations_multilabel", databaseInsertion);

								/* check whether this is the new best */
								double loss = performances.get("f1");
								System.out.println(loss);
								synchronized (bestScore) {
									if (loss < bestScore.get()) {
										bestSolution.set(k);
										bestScore.set(loss);
										timeOfBestSolution.set(time_train_ms);
									}
								}
							} catch (Throwable e) {
								System.err.println(e.getMessage());
							}

						}
					} finally {
						tickets.release();
					}
				}
			}).start();
		}

		System.out.println("Awaiting responses: ");
		tickets.acquire(workers);

		try {

			if (bestSolution.get() >= 0) {

				MultiLabelClassifier choice = (MultiLabelClassifier) getClassifierForId(componentNames, bestSolution.get());
				System.out.println("Beste Lösung: " + choice.getClass() + ". Training on all data.");

				Map<String, Double> performances = evaluateSolution(choice, outerSplit.get(0), outerSplit.get(1), evaluators);
				double testScore = performances.get("f1");
				System.out.println("Error Rate on test data: " + testScore);
				Map<String, String> databaseInsertion = new HashMap<>(getMapForExperimentEvaluation(choice, performances));
				databaseInsertion.put("run_id", String.valueOf(runId));
				handle.insert("results", databaseInsertion);
			} else {
				Map<String, Double> performances = new HashMap<>();
				evaluators.keySet().forEach(k -> performances.put(k, -1.0));
				System.out.println("No solution was found in the given time.");
				Map<String, String> databaseInsertion = new HashMap<>(getMapForExperimentEvaluation(null, performances));
				databaseInsertion.put("run_id", String.valueOf(runId));
				handle.insert("results", databaseInsertion);
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static Classifier getClassifierForId(Collection<String> componentNames, int k) throws Throwable {
		List<String> mekaBaseClassifiers = componentNames.stream().filter(c -> c.contains("meka") && !c.contains("meta")).collect(Collectors.toList());
		List<String> mekaMetaClassifiers = componentNames.stream().filter(c -> c.contains("meka") && c.contains("meta")).collect(Collectors.toList());
		List<String> wekaBaseClassifiers = componentNames.stream().filter(c -> c.contains("weka") && !c.contains("meta") && !c.contains("attributeSelection"))
				.collect(Collectors.toList());
		List<String> wekaMetaClassifiers = componentNames.stream().filter(c -> c.contains("weka") && c.contains("meta")).collect(Collectors.toList());
		mekaMetaClassifiers.add(null);
		wekaMetaClassifiers.add(null);

		// System.out.println("MEKA BASE");
		// mekaBaseClassifiers.forEach(s -> System.out.println("\t" + s));
		//
		// System.out.println("MEKA META");
		// mekaMetaClassifiers.forEach(s -> System.out.println("\t" + s));
		//
		// System.out.println("WEKA BASE ");
		// wekaBaseClassifiers.forEach(s -> System.out.println("\t" + s));
		//
		// System.out.println("WEKA META");
		// wekaMetaClassifiers.forEach(s -> System.out.println("\t" + s));

		int n1 = mekaBaseClassifiers.size();
		int n2 = mekaMetaClassifiers.size();
		int n3 = wekaBaseClassifiers.size();
		int n4 = wekaMetaClassifiers.size();

		int c = 0;
		for (int i1 = 0; i1 < n1; i1++) {
			for (int i2 = 0; i2 < n2; i2++) {
				for (int i3 = 0; i3 < n3; i3++) {
					for (int i4 = 0; i4 < n4; i4++) {
						if (c == k) {

							/* build the respective classifier */
							MultiLabelClassifier mekaBaseClassifier = (MultiLabelClassifier) AbstractMultiLabelClassifier.forName(mekaBaseClassifiers.get(i1), new String[] {});
							MultiLabelClassifier mekaMetaClassifier = mekaMetaClassifiers.get(i2) != null
									? (MultiLabelClassifier) AbstractMultiLabelClassifier.forName(mekaMetaClassifiers.get(i2), new String[] {})
									: null;
							Classifier wekaBaseClassifier = AbstractClassifier.forName(wekaBaseClassifiers.get(i3), new String[] {});
							Classifier wekaMetaClassifier = wekaMetaClassifiers.get(i4) != null ? AbstractClassifier.forName(wekaMetaClassifiers.get(i4), new String[] {}) : null;

							MultiLabelClassifier classifier = mekaBaseClassifier;
							if (mekaMetaClassifier != null) {
								classifier = mekaMetaClassifier;
								((SingleClassifierEnhancer) classifier).setClassifier(mekaBaseClassifier);
							}
							if (mekaBaseClassifier instanceof SingleClassifierEnhancer) {
								if (wekaMetaClassifier != null) {
									((SingleClassifierEnhancer) mekaBaseClassifier).setClassifier(wekaMetaClassifier);
									((SingleClassifierEnhancer) wekaMetaClassifier).setClassifier(wekaBaseClassifier);
								} else
									((SingleClassifierEnhancer) mekaBaseClassifier).setClassifier(wekaBaseClassifier);
							}
							return classifier;
						}
						c++;
					}
				}
			}
		}
		return null;

	}

	/**
	 * Evaluates a solution against all relevant performance measure
	 * 
	 * @param c
	 * @return
	 * @throws Exception
	 */
	private Map<String, Double> evaluateSolution(MultiLabelClassifier c, Instances train, Instances test, Map<String, MultilabelEvaluator> evaluators) throws Exception {
		c.buildClassifier(train);
		Map<String, Double> losses = new HashMap<>();
		for (String mlEvaluatorName : evaluators.keySet()) {
			losses.put(mlEvaluatorName, evaluators.get(mlEvaluatorName).loss(c, test));
		}
		return losses;
	}

	private Map<String, String> getMapForExperimentEvaluation(MultiLabelClassifier c, Map<String, Double> performances) {
		Map<String, String> map = new HashMap<>();
		String mekaClassifier = null;
		String mekaParams = null;
		String baseClassifier = null;
		String baseClassifierParams = null;

		if (c != null) {
			mekaClassifier = c.getClass().getName();
			mekaParams = Arrays.toString(c.getOptions());
			if (c instanceof SingleClassifierEnhancer) {
				Classifier baseClassifierObj = ((SingleClassifierEnhancer) c).getClassifier();
				if (baseClassifierObj instanceof MultiLabelClassifier) {
					mekaClassifier += "/" + baseClassifierObj.getClass().getName();
					mekaParams += "/" + Arrays.toString(((MultiLabelClassifier) baseClassifierObj).getOptions());
					if (baseClassifierObj instanceof SingleClassifierEnhancer) {
						baseClassifierObj = ((SingleClassifierEnhancer) baseClassifierObj).getClassifier();
						baseClassifier = baseClassifierObj.getClass().getName();
						if (baseClassifierObj instanceof OptionHandler)
							baseClassifierParams = Arrays.toString(((OptionHandler) baseClassifierObj).getOptions());
						if (baseClassifierObj instanceof SingleClassifierEnhancer) {
							Classifier baseBaseClassifier = ((SingleClassifierEnhancer) baseClassifierObj).getClassifier();
							baseClassifier += "/" + baseBaseClassifier.getClass().getName();
							baseClassifierParams += "/" + (baseBaseClassifier instanceof OptionHandler ? Arrays.toString(((OptionHandler) baseBaseClassifier).getOptions()) : "");
						}
					}
				} else {
					baseClassifier = baseClassifierObj.getClass().getName();
					if (baseClassifierObj instanceof OptionHandler)
						baseClassifierParams = Arrays.toString(((OptionHandler) baseClassifierObj).getOptions());
					if (baseClassifierObj instanceof SingleClassifierEnhancer) {
						Classifier baseBaseClassifier = ((SingleClassifierEnhancer) baseClassifierObj).getClassifier();
						baseClassifier += "/" + baseBaseClassifier.getClass().getName();
						baseClassifierParams += "/" + (baseBaseClassifier instanceof OptionHandler ? Arrays.toString(((OptionHandler) baseBaseClassifier).getOptions()) : "");
					}
				}
			}
		}

		/* put results */
		map.put("mmclassifier", mekaClassifier);
		map.put("mmclassifierparams", mekaParams);
		map.put("classifier", baseClassifier);
		map.put("classifierparams", baseClassifierParams);
		map.put("pipeline", "");
		for (String key : performances.keySet()) {
			map.put("loss_" + key, !performances.get(key).isNaN() ? String.valueOf(performances.get(key)) : null);
		}
		return map;
	}


}
