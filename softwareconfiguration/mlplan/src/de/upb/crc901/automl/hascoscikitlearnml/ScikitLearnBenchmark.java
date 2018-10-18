package de.upb.crc901.automl.hascoscikitlearnml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.IObjectEvaluator;
import jaicore.basic.SQLAdapter;
import jaicore.basic.chunks.Task;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ScikitLearnBenchmark implements IObjectEvaluator<Classifier, Double> {

	private static final HASCOForScikitLearnMLConfig CONFIG = ConfigCache.getOrCreate(HASCOForScikitLearnMLConfig.class);
	private static final Timer TIMEOUT_TIMER = new Timer();

	private Task runTask;
	private Instances data;
	private int maximumSeeds;
	private Double splitSize;
	private int repetitions;
	private String datasetFilePrefix;
	private SQLAdapter mysql;
	private int timeoutInMS = -1;

	private List<Instances> trainTestSplit = null;

	public ScikitLearnBenchmark(final Instances data, final int repetitions, final double splitSize, final int timeoutInMS, final int maximumSeeds, final String datasetFilePrefix, final SQLAdapter mysql, final Task runTask) {
		this.data = data;
		this.splitSize = splitSize;
		this.maximumSeeds = maximumSeeds;
		this.repetitions = repetitions;
		this.datasetFilePrefix = datasetFilePrefix;
		this.mysql = mysql;
		this.runTask = runTask;
		this.timeoutInMS = timeoutInMS;
	}

	public ScikitLearnBenchmark() {
	}

	public ScikitLearnBenchmark(final List<Instances> trainTestSplit, final String datasetFilePrefix, final SQLAdapter mysql, final Task runTask) {
		this.trainTestSplit = trainTestSplit;
		this.datasetFilePrefix = datasetFilePrefix;
		this.mysql = mysql;
		this.runTask = runTask;
	}

	public Double evaluateFixedSplit(final ScikitLearnComposition object) throws IOException, InterruptedException {
		File trainFile = new File(CONFIG.getTmpFolder().getAbsolutePath() + File.separator + this.datasetFilePrefix + "_train.arff");
		File testFile = new File(CONFIG.getTmpFolder().getAbsolutePath() + File.separator + this.datasetFilePrefix + "_test.arff");

		long startTime = System.currentTimeMillis();
		Double error = ScikitLearnEvaluator.evaluate(trainFile, testFile, object, true);

		Map<String, String> values = new HashMap<>();
		values.put("run_id", this.runTask.getValueAsString("run_id"));
		values.put("pipeline", object.getPipelineCode());
		values.put("pipelineComplexity", object.getComplexity() + "");
		values.put("import", object.getImportCode());
		values.put("errorRate", error + "");
		values.put("timeToSolution", (System.currentTimeMillis() - CONFIG.getRunStartTimestamp()) + "");
		values.put("evaluationTime", (System.currentTimeMillis() - startTime) + "");

		try {
			if (this.mysql != null) {
				int id = this.mysql.insert("test_evaluation", values);
				object.setTestID(id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return error;
	}

	@Override
	public Double evaluate(final Classifier object) throws Exception {
		if (Thread.interrupted()) {
			throw new InterruptedException("Got interrupted");
		}

		if (this.splitSize == null) {
			throw new IllegalArgumentException("Inappropriate use of ScikitLearnBenchmark. No split size provided.");
		}

		if (this.trainTestSplit != null) {
			return this.evaluateFixedSplit((ScikitLearnComposition) object);
		}

		EvaluationTimeout timeout = new EvaluationTimeout(Thread.currentThread());
		if (this.timeoutInMS > 0) {
			TIMEOUT_TIMER.schedule(timeout, this.timeoutInMS);
		}
		double returnValue = 10000d;

		long startTime = System.currentTimeMillis();
		try {
			List<Double> errorRates = new LinkedList<>();
			for (int i = 0; i < ((this.maximumSeeds > 0) ? Math.min(this.maximumSeeds, this.repetitions) : this.repetitions); i++) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Thread got interrupted, so stop evaluation of composition object");
				}
				long splitSeed = (this.maximumSeeds <= 0) ? new Random().nextLong() : new Random().nextInt(this.maximumSeeds);
				File trainFile = new File(CONFIG.getTmpFolder().getAbsolutePath() + File.separator + splitSeed + "_train.arff");
				File testFile = new File(CONFIG.getTmpFolder().getAbsolutePath() + File.separator + splitSeed + "_test.arff");

				if (!trainFile.exists() || !testFile.exists()) {
					List<Instances> mccvSplit = WekaUtil.getStratifiedSplit(this.data, new Random(splitSeed), this.splitSize);

					try (BufferedWriter bw = new BufferedWriter(new FileWriter(trainFile))) {
						bw.write(mccvSplit.get(0).toString());
					}
					try (BufferedWriter bw = new BufferedWriter(new FileWriter(testFile))) {
						bw.write(mccvSplit.get(1).toString());
					}
				}
				Double errorRate = ScikitLearnEvaluator.evaluate(trainFile, testFile, (ScikitLearnComposition) object, true);
				if (errorRate >= 0 && errorRate <= 1) {
					errorRates.add(errorRate);
				} else {
					break;
				}
			}
			timeout.cancel();
			if (errorRates.isEmpty()) {
				returnValue = 20000d;
			} else {
				if (errorRates.size() >= 5) {
					Collections.sort(errorRates);
					int numEvalsToRemove = (int) (Math.floor(errorRates.size() * 0.2));
					for (int i = 0; i < numEvalsToRemove; i++) {
						errorRates.remove(0);
						errorRates.remove(errorRates.size() - 1);
					}
				}
				returnValue = errorRates.stream().mapToDouble(x -> x).average().getAsDouble();
				// System.out.println("ErrorRate: " + returnValue + " " + object.getPipelineCode());
			}

		} catch (InterruptedException e) {
			returnValue = 10000d;
		}

		if (this.mysql != null) {
			Map<String, String> valueMap = new HashMap<>();
			valueMap.put("run_id", this.runTask.getValueAsString("run_id"));
			valueMap.put("pipeline", ((ScikitLearnComposition) object).getPipelineCode());
			valueMap.put("import", ((ScikitLearnComposition) object).getImportCode());
			valueMap.put("errorRate", returnValue + "");
			valueMap.put("timeToSolution", (System.currentTimeMillis() - CONFIG.getRunStartTimestamp()) + "");
			valueMap.put("evaluationTime", (System.currentTimeMillis() - startTime) + "");
			valueMap.put("pipelineComplexity", ((ScikitLearnComposition) object).getComplexity() + "");

			this.mysql.insert(this.datasetFilePrefix + "evaluation", valueMap);
		}
		return returnValue;
	}

	class EvaluationTimeout extends TimerTask {
		Thread threadToInterrupt;

		EvaluationTimeout(final Thread threadToInterrupt) {
			this.threadToInterrupt = threadToInterrupt;
		}

		@Override
		public void run() {
			this.threadToInterrupt.interrupt();
		}

	}

}
