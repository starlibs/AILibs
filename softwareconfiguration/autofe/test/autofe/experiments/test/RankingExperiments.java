package autofe.experiments.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.HASCOFE;
import autofe.experiments.RankingExperimentEvaluator;
import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import autofe.util.FileUtils;
import jaicore.experiments.ExperimentRunner;
import weka.core.Instances;

/**
 * Test class used for ranking experiments. A set of data sets is randomly
 * generated and a ranking is induced by the scores of ML-Plan on these data
 * sets. Then, the benchmark functions are used to evaluate the data sets and
 * the rank correlation is computed as the result.
 * 
 * @author Julian Lienen
 *
 */
public class RankingExperiments {

	private static final Logger logger = LoggerFactory.getLogger(RankingExperiments.class);

	/* Data set properties */
	// private static final int DATASET = DataSetUtils.MNIST_ID;
	private static final int DATASET_GENERATION_TIMEOUT = 60 * 1000; // Milliseconds
	// private static final String DATASET_NAME_PREFIX = "mnist";
	// private static final double USED_DATASET_RATIO = 0.05;
	private static final int MAX_PIPELINE_SIZE = 10;
	private static final int MAX_DATASET_CREATIONS = 10;

	/* ML-Plan ranking parameters */
	private static final int MLPLAN_EVAL_TIMEOUT = 1200; // 1200; // Seconds
	private static final double MLPLAN_SPLIT_RATIO = 0.75;
	private static final int MLPLAN_NUM_CORES = 4;
	private static final boolean MLPLAN_ENABLE_VIS = false;

	private static final String RANKING_DELIMITER = ",";
	private static final String RANKING_DIR = "D:\\Data\\Ranking";
	// private static final String RANKING_FILE_PATH = RANKING_DIR + File.separator
	// + "ranking.txt";
	private static final String RANKING_FILE_POSTFIX = "_ranking.txt";

	private static final List<String> DATASET_NAMES = Arrays.asList("mnist", "fashion-mnist", "cifar10");

	@Test
	public void executeRankingExperiments() throws Exception {

		logger.info("Starting ranking experiments...");

		// TODO: Make sure that all of the generated data sets exist and a ranking was
		// produced by applying ML-Plan
		FileUtils.createDirIfNotExists(RANKING_DIR);

		List<Runnable> tasks = new ArrayList<>();

		for (final String dsName : DATASET_NAMES) {

			// Check if randomly generated data sets exist
			if (FileUtils.checkIfFilesWithPrefixExist(RANKING_DIR, dsName)) {
				logger.debug("Data set variations already exist. Checking for MLPlan ranking...");
				if (!(new File(RANKING_DIR + File.separator + dsName + RANKING_FILE_POSTFIX).exists())) {
					logger.debug("Creating ML-Plan ranking for data set '" + dsName + "'...");

					List<Instances> dataSets = FileUtils.readInstances(RANKING_DIR, dsName, RANKING_FILE_POSTFIX);
					for (int i = 0; i < dataSets.size(); i++) {
						// for(Instances dataSet : dataSets) {
						Instances dataSet = dataSets.get(i);

						final int iIndex = i;

						Runnable mlplanScore = () -> {
							if (new File(RANKING_DIR + File.separator + dsName + "_" + iIndex + RANKING_FILE_POSTFIX)
									.exists()) {
								logger.info("Score for data set " + dsName + " on " + iIndex + " already exits.");
								return;
							}

							try {
								double score = getMLPlanScore(dataSet, dsName, iIndex, 42);
								FileUtils.writeDoubleArrayToFile(new double[] { score },
										RANKING_DIR + File.separator + dsName + "_" + iIndex + RANKING_FILE_POSTFIX,
										RANKING_DELIMITER);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						};
						tasks.add(mlplanScore);
					}

					logger.debug("Calculated ML-Plan ranking.");
				} else
					logger.debug("Found ML-Plan ranking for data set '" + dsName + "'.");
			} else {
				// Create data sets and rankings
				Runnable task = () -> {
					try {
						logger.debug("Creating data set variations for data set '" + dsName + "'.");
						int dataSetID = DataSetUtils.getDataSetIDByName(dsName);
						List<Instances> dataSetVariations = HASCOFE.generateRandomDataSets(dataSetID,
								MAX_DATASET_CREATIONS, MAX_PIPELINE_SIZE, DATASET_GENERATION_TIMEOUT, 42);
						FileUtils.saveInstances(dataSetVariations, RANKING_DIR, dsName);

						logger.debug("Done. Now deriving MLPlan ranking...");

						double ranking[] = getMLPlanScores(dataSetVariations, dsName, 42);
						FileUtils.writeDoubleArrayToFile(ranking,
								RANKING_DIR + File.separator + dsName + RANKING_FILE_POSTFIX, RANKING_DELIMITER);

						logger.debug("Done.");

					} catch (Exception e) {
						e.printStackTrace();
						logger.error("Fatal error. Aborting task...");
						return;
						// System.exit(1);
					}
				};
				tasks.add(task);
			}
		}

		executeParallelTasks(tasks, 3, true);

		// Unify test files
		for (String dsName : DATASET_NAMES) {
			String dsRankingFilePath = RANKING_DIR + File.separator + dsName + RANKING_FILE_POSTFIX;

			if (!new File(dsRankingFilePath).exists()) {

				double[] scores = new double[MAX_DATASET_CREATIONS];
				for (int i = 0; i < MAX_DATASET_CREATIONS; i++) {
					// dsRankingFiles.add(new File(RANKING_DIR + File.separator + dsName + "_" + i +
					// RANKING_FILE_POSTFIX));
					scores[i] = FileUtils.readDoubleArrayFromFile(
							RANKING_DIR + File.separator + dsName + "_" + i + RANKING_FILE_POSTFIX,
							RANKING_DELIMITER)[0];
				}
				FileUtils.writeDoubleArrayToFile(scores, dsRankingFilePath, RANKING_DELIMITER);
			}
		}

		// Execute experiments
		ExperimentRunner runner = new ExperimentRunner(new RankingExperimentEvaluator());
		runner.randomlyConductExperiments(true);

		logger.info("Finished ranking experiments.");
	}

	private static void executeParallelTasks(final List<Runnable> tasks, final int numThreads,
			final boolean awaitTermination) {

		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		for (Runnable task : tasks) {
			executorService.execute(task);
		}
		executorService.shutdown();

		if (awaitTermination) {
			try {
				executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				logger.warn("At least one classifier couldn't finish its training.");
			}
		}
	}

	private static double getMLPlanScore(final Instances dataSet, final String dataSetName, final int dataSetIndex,
			final int seed) throws Exception {

		double score = EvaluationUtils.evaluateMLPlan(MLPLAN_EVAL_TIMEOUT, dataSet, MLPLAN_SPLIT_RATIO, seed, logger,
				MLPLAN_ENABLE_VIS, MLPLAN_NUM_CORES);
		logger.debug("Score for data set " + dataSetIndex + " (" + dataSetName + "): " + score);
		return score;

	}

	private static double[] getMLPlanScores(final List<Instances> dataSets, final String dataSetName, final int seed)
			throws Exception {

		double[] scores = new double[dataSets.size()];
		for (int i = 0; i < dataSets.size(); i++) {
			// List<Instances> split = WekaUtil.getStratifiedSplit(dataSets.get(i), new
			// Random(new Random().nextInt(1000)),
			// .2f);
			scores[i] = EvaluationUtils.evaluateMLPlan(MLPLAN_EVAL_TIMEOUT, dataSets.get(i), MLPLAN_SPLIT_RATIO, seed,
					logger, MLPLAN_ENABLE_VIS, MLPLAN_NUM_CORES);
			logger.debug("Score for data set " + dataSetName + ": " + scores[i]);
		}
		return scores;
	}

}
