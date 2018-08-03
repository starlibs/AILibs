package de.upb.crc901.mlplan.multiclass;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import de.upb.crc901.automl.pipeline.service.MLPipelinePlan;
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import jaicore.basic.MathExt;
import jaicore.ml.evaluation.ClassifierMeasurementEvent;
import jaicore.ml.experiments.MLExperiment;
import jaicore.ml.experiments.MySQLExperimentDatabaseHandle;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

@SuppressWarnings("serial")
public class MLPlanMySQLConnector extends MySQLExperimentDatabaseHandle {

	private static final Logger logger = LoggerFactory.getLogger(MLPlanMySQLConnector.class);
	private int jobId;

	/**
	 * Initialize connection to database
	 * 
	 * @param host
	 * @param user
	 * @param password
	 * @param database
	 */
	public MLPlanMySQLConnector(String host, String user, String password, String database) {
		super(host, user, password, database);
	}

	public void addEvaluationEntry(Classifier parentSearchAlgorithm, MLPipeline identifiedPipeline, Double errorRate, Object errorObject) {

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
			if (errorRate == null) {
				values.add(null);
				values.add(String.valueOf(identifiedPipeline.getTimeForTrainingPreprocessor()));
				values.add(String.valueOf(identifiedPipeline.getTimeForTrainingClassifier()));
				values.add(null);
				values.add(null);
			} else {
				values.add(String.valueOf(errorRate));
				values.add(String.valueOf(identifiedPipeline.getTimeForTrainingPreprocessor()));
				values.add(String.valueOf(identifiedPipeline.getTimeForTrainingClassifier()));
				values.add(String.valueOf(identifiedPipeline.getTimeForExecutingPreprocessor().getMean()));
				values.add(String.valueOf(identifiedPipeline.getTimeForExecutingClassifier().getMean()));
			}
			values.add(getErrorObjectEncoding(errorObject));

			insert("INSERT INTO `evaluations` (run_id, searcher, searcherparams, evaluator, evaluatorparams, classifier, classifierparams, errorRate, time_train_preprocessors, time_train_classifier, time_execute_preprocessors, time_execute_classifier, exception) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
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

	public void addEvaluationEntry(Classifier parentSearchAlgorithm, MLServicePipeline identifiedPipeline, Double score, Object errorObject) {
		String preprocessor = "";
		MLPipelinePlan plan = identifiedPipeline.getConstructionPlan();
		if (!plan.getAttrSelections().isEmpty()) {
			preprocessor = plan.getAttrSelections().get(0).getQualifiedName() + plan.getAttrSelections().get(0).getOptions().toString();
		}
		String classifierName = plan.getClassifierPipe().getName() + plan.getClassifierPipe().getOptions().toString();

		PreparedStatement stmt = null;
		try {
			List<String> values = new ArrayList<>();
			values.add(String.valueOf(getRunIdOfClassifier(parentSearchAlgorithm)));
			values.add(preprocessor);
			values.add(classifierName);
			values.add(score != null ? String.valueOf(score / 100) : null);
			values.add(String.valueOf(identifiedPipeline.getTimeForTrainingPipeline()));
			values.add((score != null && identifiedPipeline.getTimesForPrediction().getN() > 0) ? String.valueOf(identifiedPipeline.getTimesForPrediction().getMean()) : null);
			values.add(getErrorObjectEncoding(errorObject));
			insert("INSERT INTO `evaluations_mls` (run_id, preprocessor, classifier, errorRate, time_train, time_predict,exception) VALUES (?,?,?,?,?,?,?)", values);
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

	private String getErrorObjectEncoding(Object errorObject) {
		if (errorObject == null) {
			return null;
		} else {
			if (!(errorObject instanceof Throwable))
				return errorObject.toString();
			else {
				StringBuilder sb = new StringBuilder();
				Throwable e = (Throwable) errorObject;
				sb.append(errorObject.getClass().getName() + ": " + e.getMessage() + ". Stack Trace:");
				for (StackTraceElement ste : e.getStackTrace()) {
					sb.append("\n\t" + ste.toString());
				}
				return sb.toString();
			}
		}
	}

	public void addResultEntry(int runId, String preprocessor, String classifier, double score, double believedScore) {
		try {
			List<String> values = new ArrayList<>();
			values.add(String.valueOf(runId));
			values.add(preprocessor);
			values.add(classifier);
			values.add(String.valueOf(score));
			values.add(String.valueOf(believedScore));
			insert("INSERT INTO `results` (run_id, preprocessor, classifier, errorRate,believedErrorRate) VALUES (?,?,?,?,?)", values);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addResultEntry(int runId, double score) {

		Classifier classifier = getClassifierOfRun(runId);

		if (!(classifier instanceof MLPlanWEKAClassifier)) {
			throw new UnsupportedOperationException("Currently no support for logging results for classifiers of class " + classifier.getClass().getName());
		}
		Classifier chosenModel = ((MLPlanWEKAClassifier) classifier).getSelectedClassifier();
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
			preprocessor = plan.getAttrSelections().get(0).getQualifiedName() + plan.getAttrSelections().get(0).getOptions().toString();
		}
		String classifierName = plan.getClassifierPipe().getName() + plan.getClassifierPipe().getOptions().toString();

		String plKey = pipeline.toString();
		// Map<String, DescriptiveStatistics> stats = measurePLValues.containsKey(plKey) ? measurePLValues.get(plKey) : null;

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

			Classifier c = event.getClassifier();

			/* if the event suggests an error, process it */
			logger.info("Received measurement event for classifier type {} with error {} and score {}", c.getClass().getName(), event.getError(), event.getScore());

			/* normal pipelines */
			if (c instanceof MLPipeline) {
				MLPipeline pl = (MLPipeline) c;
				addEvaluationEntry(getClassifierOfRun(jobId), pl, event.getScore(), event.getError());
			}

			/* service pipelines */
			else if (c instanceof MLServicePipeline) {
				MLServicePipeline pl = (MLServicePipeline) c;
				addEvaluationEntry(getClassifierOfRun(jobId), pl, event.getScore(), event.getError());
			} else {
				throw new UnsupportedOperationException("Cannot process events for pipelines of type \"" + c.getClass().getName() + "\".");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void afterCreateRun(MLExperiment e, int jobId) {
		logger.info("Setting new job id which will be used to associate incoming messages.");
		this.jobId = jobId;
	}
}