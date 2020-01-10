package ai.libs.reduction.ensemble.simple;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.db.sql.SQLAdapter;
import ai.libs.reduction.Util;

public class MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner {

	private static final String KEY_ERROR_RATE = "errorRate";
	private static final String TABLE_NAME = "homogeneousensemblesofreductionstumps";
	private final SQLAdapter adapter;
	private final Collection<MySQLEnsembleOfSimpleOneStepReductionsExperiment> knownExperiments = new HashSet<>();
	private final Logger logger = LoggerFactory.getLogger(MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner.class);

	public MySQLEnsembleOfSimpleOneStepReductionsExperimentRunner(final String host, final String user, final String password, final String database) throws SQLException {
		this.adapter = new SQLAdapter(host, user, password, database);
		this.knownExperiments.addAll(this.getConductedExperiments());
	}

	public Collection<MySQLEnsembleOfSimpleOneStepReductionsExperiment> getConductedExperiments() throws SQLException {
		Collection<MySQLEnsembleOfSimpleOneStepReductionsExperiment> experiments = new HashSet<>();
		List<IKVStore> rs = this.adapter.getRowsOfTable(TABLE_NAME);
		for (IKVStore store : rs) {
			experiments.add(new MySQLEnsembleOfSimpleOneStepReductionsExperiment(store.getAsInt("evaluation_id"), new EnsembleOfSimpleOneStepReductionsExperiment(store.getAsInt("seed"), store.getAsString("dataset"),
					store.getAsString("classifier"), store.getAsInt("size"), store.getAsDouble(KEY_ERROR_RATE), store.getAsString("exception"))));
		}
		return experiments;
	}

	public MySQLEnsembleOfSimpleOneStepReductionsExperiment createAndGetExperimentIfNotConducted(final int seed, final File dataFile, final String nameOfClassifier, final int size) throws SQLException {

		/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
		EnsembleOfSimpleOneStepReductionsExperiment exp = new EnsembleOfSimpleOneStepReductionsExperiment(seed, dataFile.getAbsolutePath(), nameOfClassifier, size);
		Optional<MySQLEnsembleOfSimpleOneStepReductionsExperiment> existingExperiment = this.knownExperiments.stream().filter(e -> e.getExperiment().equals(exp)).findAny();
		if (existingExperiment.isPresent()) {
			return null;
		}

		/* otherwise, check if the same classifier combination has been tried before */
		if (this.canInfeasibilityBeDerived(this.knownExperiments, exp)) {
			return null;
		}

		Map<String, Object> map = new HashMap<>();
		map.put("seed", String.valueOf(seed));
		map.put("dataset", dataFile.getAbsolutePath());
		map.put("classifier", nameOfClassifier);
		map.put("size", size);
		int[] id = this.adapter.insert(TABLE_NAME, map);
		return new MySQLEnsembleOfSimpleOneStepReductionsExperiment(id[0], exp);
	}

	private void updateExperiment(final MySQLEnsembleOfSimpleOneStepReductionsExperiment exp, final Map<String, ? extends Object> values) throws SQLException {
		Map<String, String> where = new HashMap<>();
		where.put("evaluation_id", String.valueOf(exp.getId()));
		this.adapter.update(TABLE_NAME, values, where);
	}

	public void conductExperiment(final MySQLEnsembleOfSimpleOneStepReductionsExperiment exp) throws Exception {
		List<Map<String, Object>> mccvResults = Util.conductEnsembleOfOneStepReductionsExperiment(exp.getExperiment());
		DescriptiveStatistics errorRate = new DescriptiveStatistics();
		DescriptiveStatistics runtime = new DescriptiveStatistics();
		for (Map<String, Object> result : mccvResults) {
			errorRate.addValue((double) result.get(KEY_ERROR_RATE));
			runtime.addValue((long) result.get("trainTime"));
		}

		/* prepapre values for experiment update */
		Map<String, Object> values = new HashMap<>();
		values.put(KEY_ERROR_RATE, errorRate.getMean());
		this.updateExperiment(exp, values);
	}

	public void markExperimentAsUnsolvable(final MySQLEnsembleOfSimpleOneStepReductionsExperiment exp) throws SQLException {
		Map<String, String> values = new HashMap<>();
		for (String key : new String[] { KEY_ERROR_RATE }) {
			values.put(key, "-1");
		}
		this.updateExperiment(exp, values);
	}

	public void associateExperimentWithException(final MySQLEnsembleOfSimpleOneStepReductionsExperiment exp, final Throwable e) throws SQLException {
		Map<String, String> values = new HashMap<>();
		for (String key : new String[] { KEY_ERROR_RATE }) {
			values.put(key, "-1");
		}
		values.put("exception", e.getClass().getName() + "\n" + e.getMessage());
		this.updateExperiment(exp, values);
	}

	private boolean canInfeasibilityBeDerived(final Collection<MySQLEnsembleOfSimpleOneStepReductionsExperiment> experimentsWithResults, final EnsembleOfSimpleOneStepReductionsExperiment experimentInQuestion) {
		for (MySQLEnsembleOfSimpleOneStepReductionsExperiment knownExperiment : experimentsWithResults) {
			if (!knownExperiment.getExperiment().getDataset().equals(experimentInQuestion.getDataset())) {
				continue;
			}
			EnsembleOfSimpleOneStepReductionsExperiment re = knownExperiment.getExperiment();
			if (re.getException() != null && re.getNameOfClassifier().equals(experimentInQuestion.getNameOfClassifier())) {
				this.logger.debug("Skipping because {} is known to be problematic as classifier on {} due to {}", experimentInQuestion.getNameOfClassifier(), re.getDataset(), re.getException());
				return true;
			}
		}
		return false;
	}
}
