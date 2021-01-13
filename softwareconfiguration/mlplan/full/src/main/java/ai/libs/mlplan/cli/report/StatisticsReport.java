package ai.libs.mlplan.cli.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.ml.classification.loss.dataset.AreaUnderPrecisionRecallCurve;
import ai.libs.jaicore.ml.classification.loss.dataset.AveragedInstanceLoss;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.loss.dataset.F1Measure;
import ai.libs.jaicore.ml.classification.loss.instance.LogLoss;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanAbsoluteError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanAbsolutePercentageError;
import ai.libs.jaicore.ml.regression.loss.dataset.R2;
import ai.libs.jaicore.ml.regression.loss.dataset.RootMeanSquaredError;

public class StatisticsReport {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final IComponentInstance selectedSolution;
	private final StatisticsListener statsListener;
	private final ILearnerRunReport runReport;

	public StatisticsReport(final StatisticsListener statsListener, final IComponentInstance selectedSolution, final ILearnerRunReport runReport) {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		this.statsListener = statsListener;
		this.selectedSolution = selectedSolution;
		this.runReport = runReport;
	}

	@Override
	public String toString() {
		Map<String, Object> root = new HashMap<>();
		root.put("selected_solution", ComponentInstanceUtil.getComponentInstanceString(this.selectedSolution));
		root.put("num_evaluations", this.statsListener.getNumModelsEvaluated());
		root.put("model_evaluation_stats", this.statsListener.getRootLearnerStatistics());
		root.put("final_candidate_predict_time_ms", this.runReport.getTestEndTime() - this.runReport.getTestStartTime());

		if (this.runReport.getTestSet().getLabelAttribute() instanceof ICategoricalAttribute) { // classification data
			List<String> labels = ((ICategoricalAttribute) this.runReport.getTestSet().getLabelAttribute()).getLabels();
			root.put("probabilities_labels", labels);
			// write headers of csv format.
			IPredictionAndGroundTruthTable<Integer, ISingleLabelClassification> castedReport = this.runReport.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class);

			List<double[]> probabilities = new ArrayList<>(castedReport.size());
			List<Integer> predictions = new ArrayList<>(castedReport.size());

			// add all the prediction rows.
			for (int i = 0; i < castedReport.size(); i++) {
				ISingleLabelClassification pred = castedReport.getPrediction(i);
				probabilities.add(IntStream.range(0, labels.size()).mapToDouble(pred::getProbabilityOfLabel).toArray());
				predictions.add(pred.getIntPrediction());
			}

			root.put("predictions", predictions.stream().map(labels::get).collect(Collectors.toList()));
			root.put("probabilities", probabilities);
			root.put("truth", castedReport.getGroundTruthAsList().stream().map(labels::get).collect(Collectors.toList()));
			root.put("m_error_rate", EClassificationPerformanceMeasure.ERRORRATE.loss(castedReport));
			if (labels.size() == 2) {
				root.put("m_auc_0", new AreaUnderPrecisionRecallCurve(0).score(castedReport));
				root.put("m_auc_1", new AreaUnderPrecisionRecallCurve(1).score(castedReport));
				root.put("m_f1_0", new F1Measure(0).score(castedReport));
				root.put("m_f1_1", new F1Measure(1).score(castedReport));
			} else {
				root.put("m_logloss", new AveragedInstanceLoss(new LogLoss()).loss(castedReport));
			}
		} else { // regression data
			IPredictionAndGroundTruthTable<Double, IRegressionPrediction> castedReport = this.runReport.getPredictionDiffList().getCastedView(Double.class, IRegressionPrediction.class);
			root.put("predictions", castedReport.getPredictionsAsList().stream().map(IRegressionPrediction::getDoublePrediction).collect(Collectors.toList()));
			root.put("truth", castedReport.getGroundTruthAsList());
			root.put("m_rmse", new RootMeanSquaredError().loss(castedReport));
			root.put("m_mae", new MeanAbsoluteError().loss(castedReport));
			root.put("m_rmse", new MeanAbsolutePercentageError().loss(castedReport));
			root.put("m_r2", new R2().loss(castedReport));
		}
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
