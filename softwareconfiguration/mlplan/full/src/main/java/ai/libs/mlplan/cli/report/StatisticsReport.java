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
				probabilities.add(IntStream.range(0, labels.size()).mapToDouble(x -> pred.getProbabilityOfLabel(x)).toArray());
				predictions.add(pred.getIntPrediction());
			}

			root.put("predictions", predictions);
			root.put("probabilities", probabilities);
			root.put("truth", castedReport.getGroundTruthAsList());
		} else { // regression data
			IPredictionAndGroundTruthTable<Double, IRegressionPrediction> castedReport = this.runReport.getPredictionDiffList().getCastedView(Double.class, IRegressionPrediction.class);
			root.put("predictions", castedReport.getPredictionsAsList().stream().map(x -> x.getDoublePrediction()).collect(Collectors.toList()));
			root.put("truth", castedReport.getGroundTruthAsList());
		}
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
