package autofe.experiments.test;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.HASCOFE;
import autofe.experiments.RankingExperimentEvaluator;
import autofe.test.AutoFETest;
import autofe.util.DataSetUtils;
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
	private static final int DATASET = DataSetUtils.MNIST_ID;
	private static final int DATASET_GENERATION_TIMEOUT = 60 * 1000;
	private static final String DATASET_NAME_PREFIX = "mnist";
	private static final double USED_DATASET_RATIO = 0.1;
	private static final int MAX_PIPELINE_SIZE = 15;
	private static final int MAX_DATASET_CREATIONS = 10;

	/* ML-Plan ranking parameters */
	private static final int MLPLAN_EVAL_TIMEOUT = 300;
	private static final double MLPLAN_SPLIT_RATIO = 0.75;

	private static final String RANKING_DELIMITER = ",";
	private static final String RANKING_DIR = "D:\\Data\\Ranking";
	private static final String RANKING_FILE_PATH = RANKING_DIR + "\\" + "ranking.txt";

	@Test
	public void executeRankingExperiments() throws Exception {

		logger.info("Starting ranking experiments...");

		// TODO: Make sure that all of the generated data sets exist and a ranking was
		// produced by applying ML-Plan
		double[] ranking = null;
		List<Instances> dataSets = null;
		FileUtils.createDirIfNotExists(RANKING_DIR);
		if (FileUtils.checkIfDirIsEmpty(RANKING_DIR)) {
			// Randomly generated new data sets out of one origin data set
			dataSets = HASCOFE.generateRandomDataSets(DATASET, USED_DATASET_RATIO, MAX_DATASET_CREATIONS,
					MAX_PIPELINE_SIZE, DATASET_GENERATION_TIMEOUT);
			FileUtils.saveInstances(dataSets, RANKING_DIR, DATASET_NAME_PREFIX);

			// Generate a new data set ranking using ML-Plan if necessary
			// if (!new File(RANKING_DIR + RANKING_FILE_PATH).exists()) {
			ranking = getMLPlanScores(dataSets);
			FileUtils.writeDoubleArrayToFile(ranking, RANKING_FILE_PATH, RANKING_DELIMITER);
			// }
		} else {
			// Read existing data sets and the ML-Plan ranking
			dataSets = FileUtils.readInstances(RANKING_DIR, DATASET_NAME_PREFIX);

			// Regenerate ranking if it does not exist
			if (!(new File(RANKING_FILE_PATH).exists())) {
				ranking = getMLPlanScores(dataSets);
				FileUtils.writeDoubleArrayToFile(ranking, RANKING_FILE_PATH, RANKING_DELIMITER);
			} else
				ranking = FileUtils.readDoubleArrayFromFile(RANKING_FILE_PATH, RANKING_DELIMITER);
		}
		if (dataSets == null)
			throw new IllegalStateException("Could not use any data sets for the ranking experiments.");

		// Execute experiments
		ExperimentRunner runner = new ExperimentRunner(new RankingExperimentEvaluator(ranking, dataSets));
		runner.randomlyConductExperiments(true);

		logger.info("Finished ranking experiments.");
	}

	private static double[] getMLPlanScores(final List<Instances> dataSets) throws Exception {
		// Map<Integer, Double> resultMap = new HashMap<>();
		// for (int i = 0; i < dataSets.size(); i++) {
		// resultMap.put(i, AutoFETest.evaluateMLPlan(MLPLAN_EVAL_TIMEOUT,
		// dataSets.get(i).getInstances(),
		// MLPLAN_SPLIT_RATIO, logger));
		// }
		// List<Map.Entry<Integer, Double>> results = new
		// ArrayList<>(resultMap.entrySet());
		// results.sort(Map.Entry.comparingByValue());
		//
		// return results.stream().mapToInt(entry -> entry.getKey()).toArray();

		double[] scores = new double[dataSets.size()];
		for (int i = 0; i < dataSets.size(); i++) {
			scores[i] = AutoFETest.evaluateMLPlan(MLPLAN_EVAL_TIMEOUT, dataSets.get(i), MLPLAN_SPLIT_RATIO, logger);
		}
		return scores;
	}

}
