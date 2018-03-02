package de.upb.crc901.mlplan.brlandscape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.data.general.Dataset;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.ClassSerializer;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import jaicore.basic.FileUtil;
import jaicore.basic.MathExt;
import jaicore.basic.MySQLAdapter;
import jaicore.basic.SetUtil;
import jaicore.ml.WekaUtil;
import jflex.GeneratorException;
import meka.core.MLUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

public class BRLandscapeAnalyzer {

	private static final MySQLAdapter logger = new MySQLAdapter("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "landscapeanalysises");

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

		private String classifierName;
		private int dataSplitId;
		private int labelIndex;
		private int seed;
		private Instances data;

		public Job(String classifierName, int dataSplitId, int labelIndex, int seed, Instances data) {
			super();
			this.classifierName = classifierName;
			this.dataSplitId = dataSplitId;
			this.labelIndex = labelIndex;
			this.seed = seed;
			this.data = data;
		}

		@Override
		public void run() {
			try {

				Set<String> usedKeys = getUsedKeys();
				String shortClassifierName = classifierName.substring(classifierName.lastIndexOf(".") + 1);
				String entry = encodeEntry(dataSplitId, labelIndex, seed, shortClassifierName);
				if (usedKeys.contains(entry))
					return;

				/* create experiment */
				int id = createExperimentIfDoesNotExist(dataSplitId, labelIndex, seed, shortClassifierName);
				JobResult result = executeRun(this.data, labelIndex, seed, classifierName);
				
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
		}
	}

	private static JobResult executeRun(Instances pData, int labelIndex, int seed, String classifierName) throws Exception {
		
		/* prepare data and create split */
		Instances data = turnToSingleClassProblem(pData, labelIndex);
		Collection<Integer>[] splitDecision = WekaUtil.getStratifiedSplitIndices(data, new Random(seed), .7f);
		List<Instances> split = WekaUtil.realizeSplit(data, splitDecision);
		ObjectMapper om = new ObjectMapper();
		ArrayNode an = om.createArrayNode();
		splitDecision[0].stream().sorted().forEach(v -> an.add(v));

		/* run experiment */
		Classifier c = AbstractClassifier.forName(classifierName, null);
		long timeStart = System.currentTimeMillis();
		try {
			System.out.println("Computing performance of " + classifierName + " on label " + labelIndex + " for seed " + seed + " ... ");
			c.buildClassifier(split.get(0));
			long timeEnd = System.currentTimeMillis();

			Instances testData = split.get(1);

			/* compute basic metrics and the probabilities to predict 0 for each instance */
			int tp = 0, fp = 0, tn = 0, fn = 0;
			double[] predictedProbabilitiesForOne = new double[testData.size()];
			boolean[] actualIsOne = new boolean[testData.size()];
			int k = 0;
			for (Instance i : testData) {
				double prediction = c.classifyInstance(i);
				actualIsOne[k] = (i.classValue() == 1);
				predictedProbabilitiesForOne[k] = c.distributionForInstance(i)[1];
				k++;
				if (i.classValue() == 0) {
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
			return new JobResult(an, c, tp, tn, fp, fn, rankLoss, (int) (timeEnd - timeStart), null);
		} catch (Throwable e) {
			long timeEnd = System.currentTimeMillis();
			return new JobResult(an, null, -1, -1, -1, -1, -1, (int) (timeEnd - timeStart), e);

		}

	}

	public static void main(String[] args) throws Throwable {
		
		Collection<String> classifierNames = WekaUtil.getBasicLearners();

		List<File> datasets = getAvailableDatasets(new File(args[0]));
		Collections.shuffle(datasets);

		/* get used keys to directly skip resolved ones */
		Set<String> usedKeys = getUsedKeys();
		for (File dataset : datasets) {

			ExecutorService pool = Executors.newFixedThreadPool(2);
			int experiments = 0;
			Instances allInstances = new Instances(new BufferedReader(new FileReader(dataset)));
			Collection<Integer>[] splitDecision = WekaUtil.getArbitrarySplit(allInstances, new Random(0), .7f);
			System.out.println("Considering dataset " + dataset.getName());
			int splitId = getOuterSplit(dataset.getName(), WekaUtil.splitToJsonArray(splitDecision));
			Instances inst = WekaUtil.realizeSplit(allInstances, splitDecision).get(0);

			MLUtils.prepareData(inst);
			int numLabels = inst.classIndex();
			for (int labelIndex = 0; labelIndex < numLabels; labelIndex++) {

				/* get update of used keys */
				for (int seed = 0; seed < 5; seed++) {
					for (String classifierName : classifierNames) {
						String shortClassifierName = classifierName.substring(classifierName.lastIndexOf(".") + 1);
						String entry = encodeEntry(splitId, labelIndex, seed, shortClassifierName);
						if (usedKeys.contains(entry)) {
							System.out.println("Skipping " + entry);
							continue;
						}
						pool.submit(new Job(classifierName, splitId, labelIndex, seed, inst));
						experiments++;
					}
				}
			}

			pool.shutdown();
			System.out.println("Waiting one month for termination of " + experiments + " jobs.");
			pool.awaitTermination(30, TimeUnit.DAYS);
		}
		logger.close();
	}

	private static String encodeEntry(int splitId, int labelIndex, int seed, String classifier) {
		return splitId + ";" + labelIndex + ";" + seed + ";" + classifier;
	}

	private static Set<String> getUsedKeys() throws SQLException {
		ResultSet rs = logger.getResultsOfQuery("SELECT split_id, label_index, seed, classifier FROM binaryrelevance");
		Set<String> keys = new HashSet<>();
		while (rs.next()) {
			keys.add(encodeEntry(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4)));
		}
		return keys;
	}

	private static void updateExperiment(int id, ArrayNode trainingInstances, int tp, int tn, int fp, int fn, double rankError, int trainingTimeInMS, Throwable e)
			throws SQLException {
		String[] values = {trainingInstances.toString(), String.valueOf(tp), String.valueOf(tn), String.valueOf(fp), String.valueOf(fn), String.valueOf(rankError), String.valueOf(trainingTimeInMS), e != null ? e.getClass().getName() + "\n" + e.getMessage() : "", String.valueOf(id)};
		logger.update(
				"UPDATE `binaryrelevance` SET training_indices = ?, tp = ?, tn = ?, fp = ?, fn = ?, rank_error = ?, training_time_ms = ?, exception = ? WHERE id = ?", values);
	}

	private static int getOuterSplit(String dataset, ArrayNode trainingIndices) throws SQLException {

		/* first determine whether the split exists */
		String[] values = {dataset, trainingIndices.toString()};
		ResultSet rs1 = logger.getResultsOfQuery("SELECT `split_id` FROM `outerdatasetsplits` WHERE dataset = ? AND training_indices = ?", values);
		if (rs1.next()) {
			return rs1.getInt(1);
		} else {
			return logger.insert("INSERT INTO `outerdatasetsplits` (dataset, training_indices) VALUES (?,?)", values);
		}
	}

	private static int createExperimentIfDoesNotExist(int splitId, int labelIndex, int seed, String classifier) throws SQLException {
		String[] values = {String.valueOf(splitId), String.valueOf(labelIndex), String.valueOf(seed), classifier};
		return logger.insert("INSERT INTO `binaryrelevance` (split_id, label_index, seed, classifier) VALUES (?,?,?,?)", values); 
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
}
