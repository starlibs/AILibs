package de.upb.crc901.automl.hascocombinedml;

import java.sql.PreparedStatement;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.SQLAdapter;
import jaicore.logging.LoggerUtil;
import jaicore.ml.WekaUtil;
import weka.core.Instances;

public class MLServiceBenchmark implements IObjectEvaluator<MLServicePipeline, Double> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MLServiceBenchmark.class);

	private static final HASCOForCombinedMLConfig CONFIG = ConfigCache.getOrCreate(HASCOForCombinedMLConfig.class);
	private static final Timer TIMEOUT_TIMER = new Timer();

	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ? (`evaluation_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,`run_id` int(11) NOT NULL,`pipeline` text COLLATE utf8_bin NOT NULL,`errorRate` double NOT NULL,`timeToSolution` int(11) NOT NULL,`evaluationTime` int(11) NOT NULL,`createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`evaluation_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin";

	private Instances data;
	private int maximumSeeds;
	private Double splitSize;
	private int repetitions;
	private SQLAdapter mysql;
	private int timeoutInMS = -1;
	private int experimentID = -1;
	private String mysqlLogTable;

	private List<Instances> trainTestSplit = null;

	public MLServiceBenchmark() {
	}

	public MLServiceBenchmark(final Instances data, final int repetitions, final double splitSize,
			final int timeoutInMS, final int maximumSeeds, final SQLAdapter mysql, final String mysqlLogTable,
			final int experimentID) throws SQLException {
		this.data = data;
		this.splitSize = splitSize;
		this.maximumSeeds = maximumSeeds;
		this.repetitions = repetitions;
		this.mysql = mysql;
		this.timeoutInMS = timeoutInMS;
		this.experimentID = experimentID;
		this.mysqlLogTable = mysqlLogTable;

		if (this.mysql != null) {
			PreparedStatement createTableStmt = this.mysql.getPreparedStatement("CREATE TABLE IF NOT EXISTS "
					+ mysqlLogTable
					+ " (`evaluation_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,`run_id` int(11) NOT NULL,`pipeline` text COLLATE utf8_bin NOT NULL,`errorRate` double NOT NULL,`timeToSolution` int(11) NOT NULL,`evaluationTime` int(11) NOT NULL,`createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`evaluation_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
			createTableStmt.execute();
		}
	}

	public MLServiceBenchmark(final List<Instances> trainTestSplit, final SQLAdapter mysql, final String mysqlLogTable,
			final int experimentID) throws SQLException {
		this.trainTestSplit = trainTestSplit;
		this.mysql = mysql;
		this.experimentID = experimentID;
		this.mysqlLogTable = mysqlLogTable;

		if (this.mysql != null) {
			PreparedStatement createTableStmt = this.mysql.getPreparedStatement("CREATE TABLE IF NOT EXISTS "
					+ mysqlLogTable
					+ " (`evaluation_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,`run_id` int(11) NOT NULL,`pipeline` text COLLATE utf8_bin NOT NULL,`errorRate` double NOT NULL,`timeToSolution` int(11) NOT NULL,`evaluationTime` int(11) NOT NULL,`createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`evaluation_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
			createTableStmt.execute();
		}
	}

	public Double evaluateFixedSplit(final MLServicePipeline object) throws Exception {
		long startTime = System.currentTimeMillis();
		Double errorRate = this.evaluate(object, this.trainTestSplit.get(0), this.trainTestSplit.get(1));

		if (this.mysql != null) {
			try {
				Map<String, String> valueMap = new HashMap<>();
				valueMap.put("run_id", this.experimentID + "");
				valueMap.put("errorRate", errorRate + "");
				valueMap.put("timeToSolution", (System.currentTimeMillis() - CONFIG.getRunStartTimestamp()) + "");
				valueMap.put("pipeline", object.getConstructionPlan().toString());
				valueMap.put("evaluationTime", (System.currentTimeMillis() - startTime) + "");

				this.mysql.insert(this.mysqlLogTable, valueMap);
			} catch (SQLException e) {
				LOGGER.error("An SQLException occurred with message " + e.getMessage());
			}
		}
		return errorRate;
	}

	private Double evaluate(final MLServicePipeline pipeline, final Instances train, final Instances test)
			throws Exception {
		pipeline.buildClassifier(train);
		double[] prediction = pipeline.classifyInstances(test);

		double errorCounter = 0d;
		for (int i = 0; i < test.size(); i++) {
			if (prediction[i] != test.get(i).classValue()) {
				errorCounter++;
			}
		}
		return errorCounter / test.size();
	}

	@Override
	public Double evaluate(final MLServicePipeline object) throws Exception {
		if (Thread.interrupted()) {
			throw new InterruptedException("Got interrupted");
		}

		if (this.splitSize == null) {
			throw new IllegalArgumentException("Inappropriate use of MLServiceBenchmark. No split size provided.");
		}

		if (this.trainTestSplit != null) {
			return this.evaluateFixedSplit(object);
		}

		EvaluationTimeout timeout = new EvaluationTimeout(Thread.currentThread());
		if (this.timeoutInMS > 0) {
			TIMEOUT_TIMER.schedule(timeout, this.timeoutInMS);
		}
		double returnValue = 10000d;

		long startTime = System.currentTimeMillis();
		try {
			List<Double> errorRates = new LinkedList<>();
			for (int i = 0; i < ((this.maximumSeeds > 0) ? Math.min(this.maximumSeeds, this.repetitions)
					: this.repetitions); i++) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Thread got interrupted, so stop evaluation of composition object");
				}
				long splitSeed = (this.maximumSeeds <= 0) ? new Random().nextLong()
						: new Random().nextInt(this.maximumSeeds);

				List<Instances> mccvSplit = WekaUtil.getStratifiedSplit(this.data, new Random(splitSeed),
						this.splitSize);

				try {
					double errorRate = this.evaluate(object, mccvSplit.get(0), mccvSplit.get(1));
					if (errorRate >= 0 && errorRate <= 1) {
						errorRates.add(errorRate);
					}
				} catch (Exception e) {
					LOGGER.debug("Classifier could not be evaluated");
					LoggerUtil.logException(e);
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
				// System.out.println("ErrorRate: " + returnValue + " " +
				// object.getPipelineCode());
			}

		} catch (InterruptedException e) {
			returnValue = 10000d;
		}

		if (this.mysql != null) {
			try {
				Map<String, String> valueMap = new HashMap<>();
				valueMap.put("run_id", this.experimentID + "");
				valueMap.put("errorRate", returnValue + "");
				valueMap.put("timeToSolution", (System.currentTimeMillis() - CONFIG.getRunStartTimestamp()) + "");
				valueMap.put("pipeline", object.toString());
				valueMap.put("evaluationTime", (System.currentTimeMillis() - startTime) + "");

				this.mysql.insert(this.mysqlLogTable, valueMap);
			} catch (SQLException e) {
				LOGGER.error("An SQLException occurred with message " + e.getMessage());
			}
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
