package autofe.algorithm.hasco.evaluation;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.IObjectEvaluator;
import ai.libs.jaicore.basic.SQLAdapter;

public abstract class AbstractHASCOFEObjectEvaluator extends AbstractHASCOFEEvaluator
		implements IObjectEvaluator<FilterPipeline, Double> {

	private Logger logger = LoggerFactory.getLogger(AbstractHASCOFEObjectEvaluator.class);

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

	protected void storeResult(final FilterPipeline pipe, final Double score, final long timeToCompute) {
		logger.debug("Currently, the result for the given {} with pipe {} (time to compute: {}) is not stored.", pipe, score, timeToCompute);
	}

}
