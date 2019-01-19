package de.upb.crc901.mlplan.metamining;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.basic.SQLAdapter;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;

public class DyadDatasetGenerator {

	private static final int datasetCount = 34240;

	private static final int[] allowedDatasetIds = { 12, 14, 16, 18, 20, 21, 22, 23, 24, 26, 28, 3, 30, 32, 36, 38, 44,
			46, 5, 6 };

	private static String user = "";

	private static String password = "";

	private static String db = "";

	private static String db_host = "";

	private static final Pattern arrayDeserializer = Pattern.compile(" ");

	private enum X_METRIC {
		X_LANDMARKERS("X_LANDMARKERS"), X_OTHERS("X_OTHERS");

		X_METRIC(String dbColumn) {
			this.dbColumn = dbColumn;
		}

		protected String dbColumn;

		@Override
		public String toString() {
			return dbColumn;
		}
	}

	private static X_METRIC xMetric = X_METRIC.X_LANDMARKERS;

	/**
	 * Queries the DB to extract the dyad and its' perfomance score.
	 * 
	 * @param id specifies the db entry
	 * @return the dyad
	 * @throws SQLException
	 */
	private static Pair<Dyad, Double> getDyadAndScoreWithId(int id, SQLAdapter adapter) throws SQLException {
		ResultSet res = adapter
				.getResultsOfQuery("SELECT " + xMetric + ", y, score FROM `dyad_datasets` WHERE id=" + id);
		if (res.wasNull())
			throw new IllegalArgumentException("No entry with id " + id);

		res.first();
		String serializedX = res.getString(1);
		String serializedY = res.getString(2);
		Double score = res.getDouble(3);

		double[] xArray = arrayDeserializer.splitAsStream(serializedX).mapToDouble(Double::parseDouble).toArray();
		double[] yArray = arrayDeserializer.splitAsStream(serializedY).mapToDouble(Double::parseDouble).toArray();

		Dyad dyad = new Dyad(new DenseDoubleVector(xArray), new DenseDoubleVector(yArray));
		return new Pair<Dyad, Double>(dyad, score);
	}

	/**
	 * Generates a {@link DyadRankingInstance} in the following manner: <code>
	 * while there aren't enough dyads
	 *   draw a random dyad using the seed
	 * sort the dyads
	 * return the dyad ranking instance
	 * </code>
	 * 
	 * @return
	 * @throws SQLException
	 */
	private static DyadRankingInstance getDyadInstanceForSeedAndLength(int seed, int length, SQLAdapter adapter)
			throws SQLException {
		// now draw the dyads
		List<Pair<Dyad, Double>> dyads = new ArrayList<>(length);
		Random random = new Random(seed);
		for (int i = 0; i < length; i++) {
			int randomDyadIndex = random.nextInt(datasetCount) + 1;
			dyads.add(getDyadAndScoreWithId(randomDyadIndex, adapter));
		}

		// sort the dyads and extract the sparse instance

		List<Dyad> sortedDyads = dyads.stream().sorted((pair1, pair2) -> Double.compare(pair1.getY(), pair2.getY()))
				.map(Pair::getX).collect(Collectors.toList());
		return new DyadRankingInstance(sortedDyads);
	}

	/**
	 * Generates a {@link SparseDyadRankingInstance} in the following manner: <code>
	 * while there aren't enough dyads
	 *   collect all dyads with the specified dataset id
	 *   draw a random dyad using the seed
	 * sort the dyads
	 * return the sparse instance
	 * </code>
	 * 
	 * @return
	 * @throws SQLException
	 */
	private static SparseDyadRankingInstance getSparseDyadInstance(int datasetId, int seed, int length,
			SQLAdapter adapter) throws SQLException {
		// get all indices that have the correct dataset id
		// count the datasets
		ResultSet res = adapter.getResultsOfQuery("SELECT COUNT(id) FROM `dyad_datasets` WHERE dataset = " + datasetId);
		res.next();
		int indicesAmount = res.getInt(1);
		if (indicesAmount == 0)
			throw new IllegalArgumentException("No performance samples for for the dataset-id: " + datasetId);
		int[] dyadIndicesWithDataset = new int[indicesAmount];
		// collect the indices
		res = adapter.getResultsOfQuery("SELECT id FROM `dyad_datasets` WHERE dataset = " + datasetId);
		int counter = 0;
		while (res.next()) {
			dyadIndicesWithDataset[counter++] = res.getInt(1);
		}

		// now draw the dyads
		List<Pair<Dyad, Double>> dyads = new ArrayList<>(length);
		Random random = new Random(seed);

		for (int i = 0; i < length; i++) {
			int randomIndexOfArray = random.nextInt(indicesAmount);
			int randomIndexInDb = dyadIndicesWithDataset[randomIndexOfArray];
			dyads.add(getDyadAndScoreWithId(randomIndexInDb, adapter));
		}

		// sort the dyads and extract the sparse instance
		Vector singleX = dyads.iterator().next().getX().getInstance();
		List<Vector> sortedAlternatives = dyads.stream()
				.sorted((pair1, pair2) -> Double.compare(pair1.getY(), pair2.getY())).map(Pair::getX)
				.map(Dyad::getAlternative).collect(Collectors.toList());
		return new SparseDyadRankingInstance(singleX, sortedAlternatives);
	}

	public static DyadRankingDataset getSparseDyadDataset(int seed, int amountOfDyadInstances, int alternativeLength,
			int[] allowedDatasetsInSplit) throws SQLException {
		SQLAdapter adapter = new SQLAdapter(db_host, user, password, db);

		List<IDyadRankingInstance> sparseDyadRankingInstances = new ArrayList<>();
		for (int i = 0; i < amountOfDyadInstances; i++) {
			int intermediateSeed = seed + i;
			int randomDataset = getRandomDatasetId(intermediateSeed, allowedDatasetsInSplit);
			sparseDyadRankingInstances
					.add(getSparseDyadInstance(randomDataset, intermediateSeed, alternativeLength, adapter));
		}
		adapter.close();
		return new DyadRankingDataset(sparseDyadRankingInstances);
	}

	public static DyadRankingDataset getDyadDataset(int seed, int amountOfDyadInstances, int alternativeLength)
			throws SQLException {
		SQLAdapter adapter = new SQLAdapter(db_host, user, password, db);

		List<IDyadRankingInstance> sparseDyadRankingInstances = new ArrayList<>();
		for (int i = 0; i < amountOfDyadInstances; i++) {
			int intermediateSeed = seed + i;
			sparseDyadRankingInstances
					.add(getDyadInstanceForSeedAndLength(intermediateSeed, alternativeLength, adapter));
		}
		adapter.close();
		return new DyadRankingDataset(sparseDyadRankingInstances);
	}

	public static void main(String... args) throws SQLException, TrainingException {
		user = args[0];
		password = args[1];
		db_host = args[2];
		db = args[3];
		int[] trainSeeds = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		int[] testSeeds = new int[] { 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
		SQLAdapter resultAdapter = new SQLAdapter(db_host, user, password, db);
		String resultTableName = "results_automl_metadata_plnet";
		try {
			ResultSet rs = resultAdapter.getResultsOfQuery("SHOW TABLES");
			boolean hasPerformanceTable = false;
			while (rs.next()) {
				String tableName = rs.getString(1);
				if (tableName.equals(resultTableName))
					hasPerformanceTable = true;
			}

//			if there is no table, create one
			if (!hasPerformanceTable) {
				resultAdapter.update("CREATE TABLE `" + resultTableName + "` (\r\n"
						+ " `evaluation_id` int(10) NOT NULL AUTO_INCREMENT,\r\n"
						+ " `ranking_length_train` int(10) NOT NULL,\r\n"
						+ " `ranking_length_test` int(10) NOT NULL,\r\n" + " `training_instances` int(10) NOT NULL,\r\n"
						+ " `test_instances` int(10) NOT NULL,\r\n" + " `kendall_tau` double NOT NULL,\r\n"
						+ " `train_seed` int(10) NOT NULL,\r\n" + " `test_seed` int(10) NOT NULL,\r\n"
						+ "`evaluation_date` timestamp NULL DEFAULT NULL," + " PRIMARY KEY (`evaluation_id`)\r\n"
						+ ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin", new ArrayList<>());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		int numTrainingInstances = Integer.parseInt(args[4]);
		int dyadRankingLengthTrain = Integer.parseInt(args[5]);
		int numTestInstances = Integer.parseInt(args[6]);
		int dyadRankingLengthTest = Integer.parseInt(args[7]);

		for (int i = 0; i < testSeeds.length; i++) {
			int trainSeed = trainSeeds[i];
			int testSeed = testSeeds[i];
				TrainTestDatasetIds split = getTrainTestSplit(0.7d);
				DyadRankingDataset trainDataset = getSparseDyadDataset(trainSeed, numTrainingInstances,
						dyadRankingLengthTrain, split.trainDatasetIds);
				DyadRankingDataset testDataset = getSparseDyadDataset(testSeed, numTestInstances, dyadRankingLengthTest,
						split.testDatasetIds);
				try {
					PLNetDyadRanker ranker = new PLNetDyadRanker();
					ranker.train(trainDataset);
					double kendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
							testDataset, ranker);
					Map<String, String> valueMap = new HashMap<>();
					valueMap.put("ranking_length_train", Integer.toString(dyadRankingLengthTrain));
					valueMap.put("ranking_length_test", Integer.toString(dyadRankingLengthTest));
					valueMap.put("training_instances", Integer.toString(numTrainingInstances));
					valueMap.put("test_instances", Integer.toString(numTestInstances));
					valueMap.put("kendall_tau", Double.toString(kendallTau));
					valueMap.put("train_seed", Integer.toString(trainSeed));
					valueMap.put("test_seed", Integer.toString(testSeed));
					valueMap.put("evaluation_date",
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(Instant.now())));
					resultAdapter.insert(resultTableName, valueMap);
				} catch (PredictionException e) {
					e.printStackTrace();
				}
		}
		resultAdapter.close();
	}

	private static TrainTestDatasetIds getTrainTestSplit(double ratio) {
		List<Integer> allDatasets = Arrays.stream(allowedDatasetIds).mapToObj(i -> (Integer) i)
				.collect(Collectors.toList());
		Collections.shuffle(allDatasets);
		int randomIndex = (int) Math.floor(allDatasets.size() * ratio);
		List<Integer> trainList = allDatasets.subList(0, randomIndex);
		List<Integer> testList = allDatasets.subList(randomIndex, allDatasets.size());
		TrainTestDatasetIds toReturn = new TrainTestDatasetIds();
		toReturn.trainDatasetIds = trainList.stream().mapToInt(i -> i).toArray();
		toReturn.testDatasetIds = testList.stream().mapToInt(i -> i).toArray();
		return toReturn;
	}

	private static int getRandomDatasetId(int intermediateSeed, int[] allowedDatasetsInSplit) {
		Random random = new Random(intermediateSeed);
		int index = random.nextInt(allowedDatasetsInSplit.length);
		return allowedDatasetsInSplit[index];
	}

	private static class TrainTestDatasetIds {
		int[] trainDatasetIds;
		int[] testDatasetIds;
	}

}