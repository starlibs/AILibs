package autofe.algorithm.hasco.evaluation;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.IObjectEvaluator;
import ai.libs.jaicore.basic.SQLAdapter;
import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import autofe.algorithm.hasco.AutoFEWekaPipeline;
import autofe.util.DataSet;
import weka.classifiers.Classifier;

public class AutoFEMLMCCVBenchmark implements IObjectEvaluator<AutoFEWekaPipeline, Double> {
	private static final Logger logger = LoggerFactory.getLogger(AutoFEMLMCCVBenchmark.class);

	private final IObjectEvaluator<Classifier, Double> internalEvaluator;

	private SQLAdapter adapter;
	private int experimentID;
	private String evalTable;

	public SQLAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(final SQLAdapter adapter) {
		this.adapter = adapter;
	}

	public int getExperimentID() {
		return experimentID;
	}

	public void setExperimentID(final int experimentID) {
		this.experimentID = experimentID;
	}

	public String getEvalTable() {
		return evalTable;
	}

	public void setEvalTable(final String evalTable) {
		this.evalTable = evalTable;
	}

	public AutoFEMLMCCVBenchmark(final DataSet data, final long seed, final int repeats, final double trainingPortion) {
		internalEvaluator = new MonteCarloCrossValidationEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss()), repeats, data.getInstances(), trainingPortion, seed);
	}

	@Override
	public Double evaluate(final AutoFEWekaPipeline object) throws InterruptedException, ObjectEvaluationFailedException {
		long startTimestamp = System.currentTimeMillis();
		Double evalScore = internalEvaluator.evaluate(object);
		logger.info("Eval score of AutoFEWekaPipeline {} was {}.", object, evalScore);

		if (!evalScore.toString().equals("NaN")) {
			try {
				storeResult(object, evalScore, (System.currentTimeMillis() - startTimestamp));
			} catch (SQLException e) {
				logger.error("Could not store result due to {}: {}", e.getClass().getName(), e.getMessage());
			}
		}
		return evalScore;
	}

	protected void storeResult(final AutoFEWekaPipeline pipe, final Double score, final long timeToCompute) throws SQLException {
		Map<String, Object> resultData = new HashMap<>();
		resultData.put("run_id", experimentID);
		resultData.put("errorRate", score);
		resultData.put("preprocessor", pipe.getFilterPipeline().toString());
		resultData.put("classifier", pipe.getMLPipeline().toString());
		resultData.put("time_train", (int) timeToCompute);
		resultData.put("time_predict", -1);
		adapter.insert(evalTable, resultData);
	}

}
