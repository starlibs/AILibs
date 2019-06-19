package ai.libs.reduction.single.homogeneous.bestofkatrandom;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import ai.libs.jaicore.basic.SQLAdapter;
import ai.libs.jaicore.ml.classification.multiclass.reduction.splitters.RandomSplitter;
import ai.libs.reduction.single.BestOfKAtRandomExperiment;
import ai.libs.reduction.single.ExperimentRunner;
import ai.libs.reduction.single.MySQLReductionExperiment;

public class MySQLReductionExperimentRunnerWrapper {

	private static final String KEY_CLASSIFIER = "classifier";
	private static final String TABLE_NAME = "reductionstumps_homogeneous_random_bestofk";
	private final SQLAdapter adapter;
	private final Collection<MySQLReductionExperiment> knownExperiments = new HashSet<>();
	private final int k;
	private final int mccvrepeats;

	public MySQLReductionExperimentRunnerWrapper(final String host, final String user, final String password, final String database, final int k, final int mccvRepeats) throws SQLException {
		this.adapter = new SQLAdapter(host, user, password, database);
		this.k = k;
		this.mccvrepeats = mccvRepeats;
		this.knownExperiments.addAll(this.getConductedExperiments());
	}

	public Collection<MySQLReductionExperiment> getConductedExperiments() throws SQLException {
		Collection<MySQLReductionExperiment> experiments = new HashSet<>();
		ResultSet rs = this.adapter.getRowsOfTable(TABLE_NAME);
		while (rs.next()) {
			experiments.add(new MySQLReductionExperiment(rs.getInt("evaluation_id"), new BestOfKAtRandomExperiment(rs.getInt("seed"), rs.getString("dataset"),  rs.getString(KEY_CLASSIFIER),  rs.getString(KEY_CLASSIFIER), rs.getString(KEY_CLASSIFIER), rs.getInt("k"), rs.getInt("mccvrepeats"))));
		}
		return experiments;
	}

	public MySQLReductionExperiment createAndGetExperimentIfNotConducted(final int seed, final File dataFile, final String nameOfClassifier) throws SQLException {

		/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
		BestOfKAtRandomExperiment exp = new BestOfKAtRandomExperiment(seed, dataFile.getAbsolutePath(), nameOfClassifier, nameOfClassifier, nameOfClassifier, this.k, this.mccvrepeats);
		Optional<MySQLReductionExperiment> existingExperiment = this.knownExperiments.stream().filter(e -> e.getExperiment().equals(exp)).findAny();
		if (existingExperiment.isPresent()) {
			return null;
		}

		Map<String, Object> map = new HashMap<>();
		map.put("seed", seed);
		map.put("dataset", dataFile.getAbsolutePath());
		map.put(KEY_CLASSIFIER, nameOfClassifier);
		map.put("k", this.k);
		map.put("mccvrepeats", this.mccvrepeats);
		int id = this.adapter.insert(TABLE_NAME, map);
		return new MySQLReductionExperiment(id, exp);
	}

	private void updateExperiment(final MySQLReductionExperiment exp, final Map<String,? extends Object> values) throws SQLException {
		Map<String,String> where = new HashMap<>();
		where.put("evaluation_id", String.valueOf(exp.getId()));
		this.adapter.update(TABLE_NAME, values, where);
	}

	public void conductExperiment(final MySQLReductionExperiment exp) throws Exception {
		ExperimentRunner<RandomSplitter> runner = new ExperimentRunner<>(this.k, this.mccvrepeats, seed -> new RandomSplitter(new Random(seed)));
		Map<String,Object> results = runner.conductSingleOneStepReductionExperiment(exp.getExperiment());
		this.updateExperiment(exp, results);
	}

	public void markExperimentAsUnsolvable(final MySQLReductionExperiment exp) throws SQLException {
		Map<String, String> values = new HashMap<>();
		values.put("errorRate", "-1");
		this.updateExperiment(exp, values);
	}

	public void associateExperimentWithException(final MySQLReductionExperiment exp, final Throwable e) throws SQLException {
		Map<String, String> values = new HashMap<>();
		values.put("errorRate", "-1");
		values.put("exception", e.getClass().getName() + "\n" + e.getMessage());
		this.updateExperiment(exp, values);
	}
}
