package autofe.algorithm.hasco.evaluation;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.SQLAdapter;

public abstract class AbstractHASCOFEObjectEvaluator extends AbstractHASCOFEEvaluator implements IObjectEvaluator<FilterPipeline, Double> {

	private SQLAdapter adapter;
	private int experimentID;
	private String evalTable;

	public SQLAdapter getAdapter() {
		return this.adapter;
	}

	public void setAdapter(final SQLAdapter adapter) {
		this.adapter = adapter;
	}

	public int getExperimentID() {
		return this.experimentID;
	}

	public void setExperimentID(final int experimentID) {
		this.experimentID = experimentID;
	}

	public String getEvalTable() {
		return this.evalTable;
	}

	public void setEvalTable(final String evalTable) {
		this.evalTable = evalTable;
	}

	protected void storeResult(final FilterPipeline pipe, final Double score, final long timeToCompute) throws SQLException {
		Map<String, Object> data = new HashMap<>();
		data.put("run_id", this.experimentID);
		data.put("errorRate", score);
		data.put("preprocessor", pipe.toString());
		data.put("classifier", "-");
		data.put("time_train", (int) timeToCompute);
		data.put("time_predict", -1);
		this.adapter.insert(this.evalTable, data);
	}

}
