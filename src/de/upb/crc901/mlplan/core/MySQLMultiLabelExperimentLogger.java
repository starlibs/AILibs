package de.upb.crc901.mlplan.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlplan.classifiers.MultiLabelGraphBasedPipelineSearcher;
import de.upb.crc901.mlplan.search.evaluators.ClassifierMeasurementEvent;
import jaicore.basic.MathExt;
import jaicore.basic.MySQLAdapter;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.meta.AutoWEKAClassifier;
import weka.core.OptionHandler;

public class MySQLMultiLabelExperimentLogger extends MySQLAdapter {
	private int runId;
	private Map<String, Map<String, DescriptiveStatistics>> measurePLValues = new HashMap<>();

	public MySQLMultiLabelExperimentLogger(String host, String user, String password, String database) {
		super(host,user,password,database);
	}

	public void createAndSetRun(String dataset, ArrayNode rowsForSearch, String algorithm, int seed, int timeout, int cpus, String evaltechnique) {
		PreparedStatement stmt = null;
		try {
			stmt = getConnect().prepareStatement("INSERT INTO `runs` (dataset, rows_for_search, algorithm, seed, timeout, CPUs, evaltechnique) VALUES (?,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, dataset);
			stmt.setString(2, rowsForSearch.toString());
			stmt.setString(3, algorithm);
			stmt.setInt(4, seed);
			stmt.setInt(5, timeout);
			stmt.setInt(6, cpus);
			stmt.setString(7, evaltechnique);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			int autoIndex = rs.getInt(1);
			runId = autoIndex;
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

	public void addEvaluationEntry(Classifier classifier, double score) {

		if ((classifier instanceof MultilabelMLPipeline)) {
			MLPipeline pipeline = ((MultilabelMLPipeline) classifier).getActualPipeline();

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
			Map<String, DescriptiveStatistics> stats = measurePLValues.containsKey(plKey) ? measurePLValues.get(plKey) : null;

			PreparedStatement stmt = null;
			try {
				stmt = getConnect().prepareStatement(
						"INSERT INTO `evaluations` (run_id, searcher, searcherparams, evaluator, evaluatorparams, classifier, classifierparams, pipeline, errorRate, time_train_preprocessors_mean, time_train_classifier_mean, time_execute_preprocessors_mean, time_execute_classifier_mean, time_train_preprocessors_std, time_train_classifier_std, time_execute_preprocessors_std, time_execute_classifier_std) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				stmt.setInt(1, runId);
				stmt.setString(2, searcherName);
				stmt.setString(3, searcherParams);
				stmt.setString(4, evaluatorName);
				stmt.setString(5, evaluatorParams);
				stmt.setString(6, classifierName);
				stmt.setString(7, classifierParams);
				stmt.setString(8, plKey);
				stmt.setDouble(9, score / 10000);
				if (stats != null) {
					stmt.setInt(10, (int) stats.get("time_train_preprocessors").getMean());
					stmt.setInt(11, (int) stats.get("time_train_classifier").getMean());
					stmt.setInt(12, (int) stats.get("time_execute_preprocessors").getMean());
					stmt.setInt(13, (int) stats.get("time_execute_classifier").getMean());
					stmt.setDouble(14, stats.get("time_train_preprocessors").getStandardDeviation());
					stmt.setDouble(15, stats.get("time_train_classifier").getStandardDeviation());
					stmt.setDouble(16, stats.get("time_execute_preprocessors").getStandardDeviation());
					stmt.setDouble(17, stats.get("time_execute_classifier").getStandardDeviation());
				} else {
					for (int i = 10; i <= 17; i++)
						stmt.setNull(i, Types.NULL);
				}
				stmt.executeUpdate();
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
		} else if (classifier instanceof AutoWEKAClassifier) {

		} else {
			System.out.println("Cannot log information for classifiers of class " + classifier.getClass().getName());
			return;
		}
	}
	
	public void addResultEntry(Classifier classifier, double loss_f1, double loss_hamming, double loss_exact, double loss_jaccard, double loss_rank) {
		addResultEntry(runId, classifier, loss_f1, loss_hamming, loss_exact, loss_jaccard, loss_rank);
	}
	
	public void addResultEntry(int run_id, Classifier classifier, double loss_f1, double loss_hamming, double loss_exact, double loss_jaccard, double loss_rank) {

		if (classifier instanceof MultiLabelGraphBasedPipelineSearcher) {
			MultiLabelGraphBasedPipelineSearcher<?, ?, ?> searchAlgo = (MultiLabelGraphBasedPipelineSearcher<?, ?, ?>) classifier;
			String searcherName = "", searcherParams = "", evaluatorName = "", evaluatorParams = "", mmClassifierName = "", mmClassifierParams = "", baseClassifierName = "", baseClassifierParams = "", plKey = "";
			if (searchAlgo.getSearcher().getSelectedModel() != null) {
				MLPipeline pipeline = ((MultilabelMLPipeline)searchAlgo.getSearcher().getSelectedModel()).getActualPipeline();
				if (!pipeline.getPreprocessors().isEmpty()) {
					ASSearch searcher = pipeline.getPreprocessors().get(0).getSearcher();
					ASEvaluation evaluation = pipeline.getPreprocessors().get(0).getEvaluator();
	
					searcherName = searcher.getClass().getSimpleName();
					searcherParams = searcher instanceof OptionHandler ? Arrays.toString(((OptionHandler) searcher).getOptions()) : "";
					evaluatorName = evaluation.getClass().getSimpleName();
					evaluatorParams = evaluation instanceof OptionHandler ? Arrays.toString(((OptionHandler) evaluation).getOptions()) : "";
				}
				Classifier mmClassifier = pipeline.getBaseClassifier();
				mmClassifierName = mmClassifier.getClass().getSimpleName();
				mmClassifierParams = mmClassifier instanceof OptionHandler ? Arrays.toString(((OptionHandler) mmClassifier).getOptions()) : "";
				if (mmClassifier instanceof SingleClassifierEnhancer) {
					Classifier baseClassifier = ((SingleClassifierEnhancer) mmClassifier).getClassifier();
					baseClassifierName = baseClassifier.getClass().getSimpleName();
					baseClassifierParams = baseClassifier instanceof OptionHandler ? Arrays.toString(((OptionHandler) baseClassifier).getOptions()) : "";
				}
				plKey = pipeline.toString();
			}

			// Map<String,DescriptiveStatistics> stats = measurePLValues.containsKey(plKey) ? measurePLValues.get(plKey) : null;

			PreparedStatement stmt = null;
			try {
				stmt = getConnect().prepareStatement(
						"INSERT INTO `results` (run_id, searcher, searcherparams, evaluator, evaluatorparams, mmclassifier, mmclassifierparams, classifier, classifierparams, pipeline, loss_f1, loss_hamming, loss_exact, loss_jaccard, loss_rank) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				stmt.setInt(1, run_id);
				stmt.setString(2, searcherName);
				stmt.setString(3, searcherParams);
				stmt.setString(4, evaluatorName);
				stmt.setString(5, evaluatorParams);
				stmt.setString(6, mmClassifierName);
				stmt.setString(7, mmClassifierParams);
				stmt.setString(8, baseClassifierName);
				stmt.setString(9, baseClassifierParams);
				stmt.setString(10, plKey);
				stmt.setDouble(11, MathExt.round(loss_f1 / 10000, 4));
				stmt.setDouble(12, MathExt.round(loss_hamming / 10000, 4));
				stmt.setDouble(13, MathExt.round(loss_exact / 10000, 4));
				stmt.setDouble(14, MathExt.round(loss_jaccard / 10000, 4));
				stmt.setDouble(15, MathExt.round(loss_rank / 10000, 4));
				stmt.executeUpdate();
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
		} else {
			PreparedStatement stmt = null;
			try {
				stmt = getConnect().prepareStatement(
						"INSERT INTO `results` (run_id, searcher, searcherparams, evaluator, evaluatorparams, mmclassifier, mmclassifierparams, classifier, classifierparams, pipeline, loss_f1, loss_hamming, loss_exact, loss_jaccard, loss_rank) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				stmt.setInt(1, run_id);
				stmt.setString(2, "");
				stmt.setString(3, "");
				stmt.setString(4, "");
				stmt.setString(5, "");
				stmt.setString(6, "");
				stmt.setString(7, "");
				stmt.setString(8, "");
				stmt.setString(9, "");
				stmt.setString(10, "");
				stmt.setDouble(11, MathExt.round(loss_f1 / 10000, 4));
				stmt.setDouble(12, MathExt.round(loss_hamming / 10000, 4));
				stmt.setDouble(13, MathExt.round(loss_exact / 10000, 4));
				stmt.setDouble(14, MathExt.round(loss_jaccard / 10000, 4));
				stmt.setDouble(15, MathExt.round(loss_rank / 10000, 4));
				// if (stats != null) {
				// stmt.setInt(10, (int)stats.get("time_train_preprocessors").getMean());
				// stmt.setInt(11, (int)stats.get("time_train_classifier").getMean());
				// stmt.setInt(12, (int)stats.get("time_execute_preprocessors").getMean());
				// stmt.setInt(13, (int)stats.get("time_execute_classifier").getMean());
				// stmt.setInt(14, (int)stats.get("time_train_preprocessors").getStandardDeviation());
				// stmt.setInt(15, (int)stats.get("time_train_classifier").getStandardDeviation());
				// stmt.setInt(16, (int)stats.get("time_execute_preprocessors").getStandardDeviation());
				// stmt.setInt(17, (int)stats.get("time_execute_classifier").getStandardDeviation());
				// }
				// else {
				// for (int i = 10; i <= 17; i++)
				// stmt.setNull(i, Types.NULL);
				// }
				stmt.executeUpdate();
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
	}

	@Subscribe
	public void receivePipelineMeasurementEvent(ClassifierMeasurementEvent<Double> event) {
		try {
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet getResults(String viewname) throws SQLException {
		PreparedStatement stmt = getConnect().prepareStatement("SELECT * FROM " + viewname);
		return stmt.executeQuery();
	}
}