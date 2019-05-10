package de.upb.crc901.reduction.single.homogeneous.bestofkatrandom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import de.upb.crc901.reduction.single.BestOfKAtRandomExperiment;
import de.upb.crc901.reduction.single.ExperimentRunner;
import de.upb.crc901.reduction.single.MySQLReductionExperiment;
import jaicore.basic.SQLAdapter;
import jaicore.ml.classification.multiclass.reduction.splitters.RandomSplitter;

public class MySQLReductionExperimentRunnerWrapper {

	private static final String TABLE_NAME = "reductionstumps_homogeneous_random_bestofk";
	private final SQLAdapter adapter;
	private final Collection<MySQLReductionExperiment> knownExperiments = new HashSet<>();
	private final int k;
	private final int mccvrepeats;

	public MySQLReductionExperimentRunnerWrapper(String host, String user, String password, String database, int k, int mccvRepeats) {
		adapter = new SQLAdapter(host, user, password, database);
		this.k = k;
		this.mccvrepeats = mccvRepeats;
		try {
			knownExperiments.addAll(getConductedExperiments());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Collection<MySQLReductionExperiment> getConductedExperiments() throws SQLException {
		Collection<MySQLReductionExperiment> experiments = new HashSet<>();
		ResultSet rs = adapter.getRowsOfTable(TABLE_NAME);
		while (rs.next()) {
			experiments.add(new MySQLReductionExperiment(rs.getInt("evaluation_id"), new BestOfKAtRandomExperiment(rs.getInt("seed"), rs.getString("dataset"),  rs.getString("classifier"),  rs.getString("classifier"), rs.getString("classifier"), rs.getInt("k"), rs.getInt("mccvrepeats"))));
		}
		return experiments;
	}

	public MySQLReductionExperiment createAndGetExperimentIfNotConducted(int seed, File dataFile, String nameOfClassifier) throws FileNotFoundException, IOException {
		
		/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
		BestOfKAtRandomExperiment exp = new BestOfKAtRandomExperiment(seed, dataFile.getAbsolutePath(), nameOfClassifier, nameOfClassifier, nameOfClassifier, k, mccvrepeats);
		Optional<MySQLReductionExperiment> existingExperiment = knownExperiments.stream().filter(e -> e.getExperiment().equals(exp)).findAny();
		if (existingExperiment.isPresent())
			return null;
		
		Map<String, Object> map = new HashMap<>();
		map.put("seed", seed);
		map.put("dataset", dataFile.getAbsolutePath());
		map.put("classifier", nameOfClassifier);
		map.put("k", k);
		map.put("mccvrepeats", mccvrepeats);
		try {
			int id = adapter.insert(TABLE_NAME, map);
			return new MySQLReductionExperiment(id, exp);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	private void updateExperiment(MySQLReductionExperiment exp, Map<String,? extends Object> values) throws SQLException {
		Map<String,String> where = new HashMap<>();
		where.put("evaluation_id", String.valueOf(exp.getId()));
		adapter.update(TABLE_NAME, values, where);
	}
	
	public void conductExperiment(MySQLReductionExperiment exp) throws Exception {
		ExperimentRunner<RandomSplitter> runner = new ExperimentRunner<RandomSplitter>(k, mccvrepeats, (seed) -> new RandomSplitter(new Random(seed)));
		Map<String,Object> results = runner.conductSingleOneStepReductionExperiment(exp.getExperiment());
		updateExperiment(exp, results);
	}
	
	public void markExperimentAsUnsolvable(MySQLReductionExperiment exp) throws SQLException {
		Map<String, String> values = new HashMap<>();
		values.put("errorRate", "-1");
		updateExperiment(exp, values);
	}
	
	public void associateExperimentWithException(MySQLReductionExperiment exp, Throwable e) throws SQLException {
		Map<String, String> values = new HashMap<>();
		 values.put("errorRate", "-1");
		values.put("exception", e.getClass().getName() + "\n" + e.getMessage());
		updateExperiment(exp, values);
	}
}
