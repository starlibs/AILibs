package de.upb.crc901.mlplan.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import jaicore.ml.WekaUtil;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.OneRAttributeEval;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.attributeSelection.SymmetricalUncertAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SGD;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.M5Rules;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;

public class BaseLearnerEvaluator {
	
	final static class EvaluationJob {
		String name;
		Instances train, test;
		Classifier c;
		int seed;

		public EvaluationJob(final String name, final Instances train, final Instances test, final Classifier c, int seed) {
			super();
			this.name = name;
			this.train = train;
			this.test = test;
			this.c = c;
			this.seed = seed;
		}
	}

	// public static void main(String[] args) throws Exception {
	public static void main(final String[] args) throws Exception {

		/* define basic benchmark */
		List<String> datasetNames = new ArrayList<>();
		Path folder = Paths.get(args[0]);

		try (Stream<Path> paths = Files.walk(folder)) {
			paths.forEach(filePath -> {
				if (!filePath.toFile().isDirectory()) {
					datasetNames.add(filePath.toFile().getAbsolutePath());
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		Classifier[] portfolio = { new BayesNet(), new NaiveBayes(), new NaiveBayesMultinomial(), new GaussianProcesses(), new LinearRegression(), new Logistic(),
				new MultilayerPerceptron(), new SGD(), new SimpleLinearRegression(), new SimpleLogistic(), new VotedPerceptron(), new IBk(), new KStar(),
				new DecisionTable(),
				new JRip(), new M5Rules(), new OneR(),
				new PART(),
				new ZeroR(), new DecisionStump(), new J48(),
				new LMT(),
				new M5P(), new RandomForest(), new RandomTree(),
				new REPTree() };

		/* feature selection algorithms */
		ASEvaluation[] evaluators = { new CfsSubsetEval(), new CorrelationAttributeEval(), new GainRatioAttributeEval(), new InfoGainAttributeEval(), new OneRAttributeEval(),
				new PrincipalComponents(), new ReliefFAttributeEval(), new SymmetricalUncertAttributeEval() };
		ASSearch[] searchers = { new BestFirst(), new GreedyStepwise(), new Ranker() };

		/* determine k-th job */
		final ConcurrentHashMap<EvaluationJob, Double> results = new ConcurrentHashMap<>();
		List<EvaluationJob> jobs = new ArrayList<>();
		
		int k = Integer.parseInt(args[1]);
		System.out.println("This is job " + (k+1) + "/" + (datasetNames.size() * portfolio.length * 10));
		EvaluationJob job = getKthJob(datasetNames, portfolio, k);
		String datasetname = job.name.substring(job.name.lastIndexOf(File.separator) + 1);
		datasetname = datasetname.substring(0, datasetname.indexOf("."));
		File resultsfolder = new File("results/" + datasetname);
		resultsfolder.mkdirs();
		try (FileWriter fw = new FileWriter("results/" + datasetname + "/" + job.c.getClass().getName() + ".csv", true)) {
			double score = evalModel(job);
			fw.write(job.seed +"; " + String.valueOf(score).replace(".", ",") + "\n");
		}
		

		/* no evaluate the algorithms in parallel */
//		jobs.stream().parallel().forEach(job -> {
//			results.put(job, evalModel(job));
//			System.out.println(MathExt.round(results.size() * 100f / jobs.size(), 2) + "%");
//		});

		/* get all the tokens */
//		System.out.println("Finished");
//		printTable(results);
	}
	
	private static EvaluationJob getKthJob(List<String> datasetNames, Classifier[] portfolio, int k) {
		int j = 0;
		for (String datasetName : datasetNames) {
			Instances dataset = null;
			for (Classifier c : portfolio) {
				for (int i = 0; i < 10; i++) {
					if (j == k) {
						if (dataset == null)
							dataset = getDataset(datasetName);
						Random random = new Random(i);
						List<Instances> split = WekaUtil.getStratifiedSplit(dataset, random, .9f);
						Instances train = split.get(0);
						Instances test = split.get(1);
						if (train.size() + test.size() != dataset.size())
							throw new IllegalStateException("Invalid split");
						return new EvaluationJob(datasetName, train, test, c, i);
					}
					j++;
				}
			}
		}
		return null;
	}

	public static void printTable(final Map<EvaluationJob, Double> results) {
		Map<String, Map<String, DescriptiveStatistics>> tableAsMap = new HashMap<>();
		for (EvaluationJob job : results.keySet()) {
			if (!tableAsMap.containsKey(job.name)) {
				tableAsMap.put(job.name, new HashMap<>());
			}
			Map<String, DescriptiveStatistics> row = tableAsMap.get(job.name);
			if (!row.containsKey(job.c.getClass().getName()))
				row.put(job.c.getClass().getName(), new DescriptiveStatistics());
			row.get(job.c.getClass().getName()).addValue(results.get(job));
		}

		StringBuilder sb = new StringBuilder();
		boolean headersWritten = false;
		for (String rowKey : tableAsMap.keySet()) {
			List<String> algos = tableAsMap.get(rowKey).keySet().stream().sorted().collect(Collectors.toList());
			
			if (!headersWritten) {
				algos.stream().forEach(m -> {
					String name = m.substring(m.lastIndexOf(".") + 1);
					sb.append(";");
					sb.append(name);
				});
				sb.append("\n");
				headersWritten = true;
			}
			sb.append(rowKey.substring(rowKey.lastIndexOf("/") + 1));
			sb.append(";");
			for (int i = 0; i < algos.size(); i++) {
				Double val = tableAsMap.get(rowKey).get(algos.get(i)).getMean();
				sb.append(String.valueOf(Math.round(100 * val) / 100f).replace(".", ","));
				if (i < algos.size() - 1) {
					sb.append("; ");
				}
			}
			sb.append("\n");
		}

		/* print output and write it */
		try (FileWriter fw = new FileWriter("baselearnerresults.csv")) {
			fw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(sb.toString());
	}

	private static Instances getDataset(final String datasetname) {
		try {
			System.out.print("Loading model of " + datasetname + " ...");
			long start = System.currentTimeMillis();
			Instances data = new Instances(new BufferedReader(new FileReader(datasetname)));
			data.setClassIndex(data.numAttributes() - 1);
			long end = System.currentTimeMillis();
			int mSize = data.size() * data.numAttributes();
			System.out.println("done (" + (end - start) + "ms). Matrix size is " + (mSize / 1000f) + "k");
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// private static int[] getRelevanceAttributeIndices(final Instances data, final ASSearch search, final ASEvaluation eval) {
	// weka.attributeSelection.AttributeSelection attsel = new weka.attributeSelection.AttributeSelection();
	// attsel.setEvaluator(eval);
	// attsel.setSearch(search);
	// int[] indices = null;
	// try {
	// attsel.SelectAttributes(data);
	// indices = attsel.selectedAttributes();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return indices;
	// }
	//
	// private static Instances reduceAttributes(final Instances data, final int[] indices) {
	// Remove af = new Remove();
	// af.setAttributeIndicesArray(indices);
	// try {
	// af.setOptions(new String[] { "-V" });
	// af.setInputFormat(data);
	// return Filter.useFilter(data, af);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// private static Instances reduceDataset(final Instances data, final int examplesToKeepPerClass) {
	//
	// /* reduce features */
	// int mSize = data.size() * data.numAttributes();
	// if (mSize > 100 * 100) {
	// System.out.print("performing feature selection to reduce from " + mSize / 1000 + "k ...");
	// // PrincipalComponents pca = new PrincipalComponents();
	// // pca.setInputFormat(data1);
	// // pca.setMaximumAttributes(100);
	// // pca.setOptions(new String[]{});
	// // Ranker r = new Ranker();
	//
	// int examplesToKeep = data.numClasses() * examplesToKeepPerClass;
	// double share = Math.min(examplesToKeep / (data.size() * 1f), 1);
	//
	// System.out.print("Removing " + Math.round(data.size() * (1 - share)) + " examples ...");
	// Instances newData = WekaUtil.getStratifiedSplit(data, new Random(0), share).get(0);
	// System.out.print(newData.size() + " items remain. Applying feature selection ... ");
	//
	// return newData;
	// }
	// return new Instances(data);
	// }

	public static Double evalModel(final EvaluationJob job) {
		Evaluation eval = null;
		try {
			final Instances train = job.train;
			final Instances test = job.test;
			final long start = System.currentTimeMillis();
			eval = new Evaluation(train);
			System.out.println(job.c.getClass().getName() + ": Starting evaluation (m = " + (train.size() + test.size()) * train.numAttributes() / 1000f + "k)");
//			Optional<?> cOpt = SubProcessWrapper.runWithTimeout("de.upb.crc901.taskconfigurator.core.MLUtil", "buildClassifier", "", 60000, job.c, train);
			Classifier c = job.c;
			c.buildClassifier(train);
			// job.c.buildClassifier(train);
//			if (!cOpt.isPresent()) {
//				System.out.println(job.c.getClass().getName() + ": Evaluation timed out");
//				return -2.0;
//			}
//			Classifier c = (Classifier) cOpt.get();
			eval.evaluateModel(c, test);
			final long end = System.currentTimeMillis();
			System.out.println(c.getClass().getName() + " (" + (end - start) + "ms): " + eval.pctCorrect());
		} catch (Throwable e) {
			eval = null;
			System.out.println(job.c.getClass().getName() + ": no evaluation possible");
			e.printStackTrace();
		}
		return eval != null ? eval.pctCorrect() : -1;
	}

}
