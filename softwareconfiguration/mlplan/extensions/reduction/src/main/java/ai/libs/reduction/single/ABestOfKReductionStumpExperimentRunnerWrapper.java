package ai.libs.reduction.single;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter.RandomSplitter;

public abstract class ABestOfKReductionStumpExperimentRunnerWrapper {

	private final IDatabaseAdapter adapter;
	private final String tableName;
	private final int k;

	private final int mccvrepeats;

	protected ABestOfKReductionStumpExperimentRunnerWrapper(final IDatabaseAdapter adapter, final String tableName, final int k, final int mccvrepeats) {
		this.adapter = adapter;
		this.tableName = tableName;
		this.k = k;
		this.mccvrepeats = mccvrepeats;
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

	public void conductExperiment(final MySQLReductionExperiment exp) throws Exception {
		ExperimentRunner<RandomSplitter> runner = new ExperimentRunner<>(this.k, this.mccvrepeats, seed -> new RandomSplitter(new Random(seed)));
		Map<String, Object> results = runner.conductSingleOneStepReductionExperiment(exp.getExperiment());
		this.updateExperiment(exp, results);
	}

	protected void updateExperiment(final MySQLReductionExperiment exp, final Map<String, ? extends Object> values) throws SQLException {
		Map<String, String> where = new HashMap<>();
		where.put("evaluation_id", String.valueOf(exp.getId()));
		this.adapter.update(this.tableName, values, where);
	}

	public int getK() {
		return this.k;
	}

	public int getMCCVRepeats() {
		return this.mccvrepeats;
	}

	public IDatabaseAdapter getAdapter() {
		return this.adapter;
	}
}
