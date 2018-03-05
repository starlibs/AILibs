package de.upb.crc901.mlplan.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlplan.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import de.upb.crc901.mlplan.search.evaluators.ClassifierMeasurementEvent;
import de.upb.crc901.mlplan.services.MLPipelinePlan;
import de.upb.crc901.mlplan.services.MLServicePipeline;
import jaicore.basic.MathExt;
import jaicore.ml.experiments.MySQLExperimentDatabaseHandle;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

@SuppressWarnings("serial")
public class MySQLMLPlanExperimentLogger extends MySQLExperimentDatabaseHandle {

	private Map<String, Map<String, DescriptiveStatistics>> measurePLValues = new HashMap<>();

	/**
	 * Initialize connection to database
	 * 
	 * @param host
	 * @param user
	 * @param password
	 * @param database
	 */
	public MySQLMLPlanExperimentLogger(String host, String user, String password, String database) {
		super(host, user, password, database);
	}

	public void addEvaluationEntry(Classifier parentSearchAlgorithm, Classifier identifiedPipeline, double score) {
		if (identifiedPipeline instanceof MLPipeline)
			addEvaluationEntry(parentSearchAlgorithm, (MLPipeline) identifiedPipeline, score);
		else if (identifiedPipeline instanceof MLServicePipeline)
			addEvaluationEntry(parentSearchAlgorithm, (MLServicePipeline) identifiedPipeline, score);
		else
			throw new IllegalArgumentException("Cannot log evaluation entries for pipelines of class " + identifiedPipeline.getClass().getName());
	}

	public void addEvaluationEntry(Classifier parentSearchAlgorithm, MLPipeline identifiedPipeline, double score) {

		String searcherName = "", searcherParams = "", evaluatorName = "", evaluatorParams = "";
		if (!identifiedPipeline.getPreprocessors().isEmpty()) {
			ASSearch searcher = identifiedPipeline.getPreprocessors().get(0).getSearcher();
			ASEvaluation evaluation = identifiedPipeline.getPreprocessors().get(0).getEvaluator();

			searcherName = searcher.getClass().getSimpleName();
			searcherParams = searcher instanceof OptionHandler ? Arrays.toString(((OptionHandler) searcher).getOptions()) : "";
			evaluatorName = evaluation.getClass().getSimpleName();
			evaluatorParams = evaluation instanceof OptionHandler ? Arrays.toString(((OptionHandler) evaluation).getOptions()) : "";
		}
		Classifier baseClassifier = identifiedPipeline.getBaseClassifier();
		String classifierName = baseClassifier.getClass().getSimpleName();
		String classifierParams = baseClassifier instanceof OptionHandler ? Arrays.toString(((OptionHandler) baseClassifier).getOptions()) : "";

		String plKey = identifiedPipeline.toString();
		Map<String, DescriptiveStatistics> stats = measurePLValues.containsKey(plKey) ? measurePLValues.get(plKey) : null;

		PreparedStatement stmt = null;
		try {
			List<String> values = new ArrayList<>();
			values.add(String.valueOf(getRunIdOfClassifier(parentSearchAlgorithm)));
			values.add(searcherName);
			values.add(searcherParams);
			values.add(evaluatorName);
			values.add(evaluatorParams);
			values.add(classifierName);
			values.add(classifierParams);
			values.add(plKey);
			values.add(String.valueOf(score / 10000));
			if (stats != null) {
				values.add(String.valueOf(stats.get("time_train_preprocessors").getMean()));
				values.add(String.valueOf(stats.get("time_train_classifier").getMean()));
				values.add(String.valueOf(stats.get("time_execute_preprocessors").getMean()));
				values.add(String.valueOf(stats.get("time_execute_classifier").getMean()));
				values.add(String.valueOf(stats.get("time_train_preprocessors").getStandardDeviation()));
				values.add(String.valueOf(stats.get("time_train_classifier").getStandardDeviation()));
				values.add(String.valueOf(stats.get("time_execute_preprocessors").getStandardDeviation()));
				values.add(String.valueOf(stats.get("time_execute_classifier").getStandardDeviation()));
			} else {
				for (int i = 10; i <= 17; i++)
					values.add(null);
			}

			insert("INSERT INTO `evaluations` (run_id, searcher, searcherparams, evaluator, evaluatorparams, classifier, classifierparams, pipeline, errorRate, time_train_preprocessors_mean, time_train_classifier_mean, time_execute_preprocessors_mean, time_execute_classifier_mean, time_train_preprocessors_std, time_train_classifier_std, time_execute_preprocessors_std, time_execute_classifier_std) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					values);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addEvaluationEntry(Classifier parentSearchAlgorithm, MLServicePipeline identifiedPipeline, double score) {
		String preprocessor = "";
		MLPipelinePlan plan = identifiedPipeline.getConstructionPlan();
		if (!plan.getAttrSelections().isEmpty()) {
			preprocessor = plan.getAttrSelections().get(0).getQualifiedName();
			
		}
		String classifierName = plan.getClassifierPipe().getName();

		String plKey = identifiedPipeline.toString();
		Map<String, DescriptiveStatistics> stats = measurePLValues.containsKey(plKey) ? measurePLValues.get(plKey) : null;

		PreparedStatement stmt = null;
		try {
			List<String> values = new ArrayList<>();
			values.add(String.valueOf(getRunIdOfClassifier(parentSearchAlgorithm)));
			values.add(preprocessor);
			values.add(classifierName);
			values.add(plKey);
			values.add(String.valueOf(score / 10000));
			if (stats != null) {
				values.add(String.valueOf(stats.get("time_train").getMean()));
				values.add(String.valueOf(stats.get("time_predict").getMean()));
				values.add(String.valueOf(stats.get("time_train").getStandardDeviation()));
				values.add(String.valueOf(stats.get("time_predict").getStandardDeviation()));
			} else {
				for (int i = 0; i < 4; i++)
					values.add(null);
			}

			insert("INSERT INTO `evaluations_mls` (run_id, preprocessor, classifier, pipeline, errorRate, time_train_mean, time_predict_mean, time_train_std, time_predict_std) VALUES (?,?,?,?,?,?,?,?,?)",
					values);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addResultEntry(int runId, double score) {

		Classifier classifier = getClassifierOfRun(runId);

		if (!(classifier instanceof de.upb.crc901.mlplan.classifiers.TwoPhaseHTNBasedPipelineSearcher)) {
			throw new UnsupportedOperationException("Currently no support for logging results for classifiers of class " + classifier.getClass().getName());
		}
		Classifier chosenModel = ((TwoPhaseHTNBasedPipelineSearcher<?>) classifier).getSelectedModel();
		if (chosenModel instanceof MLPipeline)
			addResultEntry(runId, (MLPipeline) chosenModel, score);
		else if (chosenModel instanceof MLServicePipeline)
			addResultEntry(runId, (MLServicePipeline) chosenModel, score);
		else
			throw new UnsupportedOperationException("Cannot write results for classifiers of type " + chosenModel.getClass());
	}

	private void addResultEntry(int runId, MLPipeline pipeline, double score) {

		String searcherName = "", searcherParams = "", evaluatorName = "", evaluatorParams = "";
		if (!pipeline.getPreprocessors().isEmpty()) {
			ASSearch searcher = pipeline.getPreprocessors().get(0).getSearcher();
			ASEvaluation evaluation = pipeline.getPreprocessors().get(0).getEvaluator();

			searcherName = searcher.getClass().getSimpleName();
			searcherParams = searcher instanceof OptionHandler ? Arrays.toString(((OptionHandler) searcher).getOptions()) : "";
			evaluatorName = evaluation.getClass().getSimpleName();
			evaluatorParams = evaluation instanceof OptionHandler ? Arrays.toString(((OptionHandler) evaluation).getOptions()) : "";
		}
		Classifier baseClassifier = pipeline.getBaseClassifier();
		String classifierName = baseClassifier.getClass().getSimpleName();
		String classifierParams = baseClassifier instanceof OptionHandler ? Arrays.toString(((OptionHandler) baseClassifier).getOptions()) : "";

		String plKey = pipeline.toString();

		try {
			List<String> values = new ArrayList<>();
			values.add(String.valueOf(runId));
			values.add(searcherName);
			values.add(searcherParams);
			values.add(evaluatorName);
			values.add(evaluatorParams);
			values.add(classifierName);
			values.add(classifierParams);
			values.add(plKey);
			values.add(String.valueOf(MathExt.round(score / 10000, 4)));

			insert("INSERT INTO `results` (run_id, searcher, searcherparams, evaluator, evaluatorparams, classifier, classifierparams, pipeline, errorRate) VALUES (?,?,?,?,?,?,?,?,?)",
					values);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void addResultEntry(int runId, MLServicePipeline pipeline, double score) {
		String preprocessor = "";
		MLPipelinePlan plan = pipeline.getConstructionPlan();
		if (!plan.getAttrSelections().isEmpty()) {
			preprocessor = plan.getAttrSelections().get(0).getQualifiedName();
			
		}
		String classifierName = plan.getClassifierPipe().getName();

		String plKey = pipeline.toString();
//		Map<String, DescriptiveStatistics> stats = measurePLValues.containsKey(plKey) ? measurePLValues.get(plKey) : null;

		PreparedStatement stmt = null;
		try {
			List<String> values = new ArrayList<>();
			values.add(String.valueOf(runId));
			values.add(preprocessor);
			values.add(classifierName);
			values.add(plKey);
			values.add(String.valueOf(score / 10000));
			insert("INSERT INTO `results_mls` (run_id, preprocessor, classifier, pipeline, errorRate) VALUES (?,?,?,?,?)", values);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Subscribe
	public void receivePipelineMeasurementEvent(ClassifierMeasurementEvent<Double> event) {
		try {

			/* normal pipelines */
			if (event.getClassifier() instanceof MLPipeline) {
				MLPipeline pl = (MLPipeline) event.getClassifier();
				String plKey = pl.toString();
				if (!measurePLValues.containsKey(plKey)) {
					Map<String, DescriptiveStatistics> stats = new HashMap<>();
					stats.put("time_train_preprocessors", new DescriptiveStatistics());
					stats.put("time_train_classifier", new DescriptiveStatistics());
					stats.put("time_execute_preprocessors", new DescriptiveStatistics());
					stats.put("time_execute_classifier", new DescriptiveStatistics());
					measurePLValues.put(plKey, stats);
				}
				Map<String, DescriptiveStatistics> stats = measurePLValues.get(plKey);
				stats.get("time_train_preprocessors").addValue(pl.getTimeForTrainingPreprocessor());
				stats.get("time_train_classifier").addValue(pl.getTimeForTrainingClassifier());
				if (pl.getTimeForExecutingPreprocessor().getMean() != Double.NaN)
					stats.get("time_execute_preprocessors").addValue(pl.getTimeForExecutingPreprocessor().getMean());
				if (pl.getTimeForExecutingClassifier().getMean() != Double.NaN)
					stats.get("time_execute_classifier").addValue(pl.getTimeForExecutingClassifier().getMean());
			}

			/* service pipelines */
			if (event.getClassifier() instanceof MLServicePipeline) {
				MLServicePipeline pl = (MLServicePipeline) event.getClassifier();
				String plKey = pl.toString();
				if (!measurePLValues.containsKey(plKey)) {
					Map<String, DescriptiveStatistics> stats = new HashMap<>();
					stats.put("time_train", new DescriptiveStatistics());
					stats.put("time_predict", new DescriptiveStatistics());
					measurePLValues.put(plKey, stats);
				}
				Map<String, DescriptiveStatistics> stats = measurePLValues.get(plKey);
				stats.get("time_train").addValue(pl.getTimeForTrainingPipeline());
				if (pl.getTimesForPrediction().getMean() != Double.NaN)
					stats.get("time_predict").addValue(pl.getTimesForPrediction().getMean());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}