package ai.libs.mlplan.cli.report;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

public class OpenMLAutoMLBenchmarkReport {

	private final ILearnerRunReport runReport;

	public OpenMLAutoMLBenchmarkReport(final ILearnerRunReport runReport) {
		this.runReport = runReport;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (this.runReport.getTestSet().getLabelAttribute() instanceof ICategoricalAttribute) { // classification data
			List<String> labels = ((ICategoricalAttribute) this.runReport.getTestSet().getLabelAttribute()).getLabels();

			// write headers of csv format.
			sb.append(labels.stream().collect(Collectors.joining(","))).append(",").append("predictions").append(",").append("truth").append("\n");
			IPredictionAndGroundTruthTable<Integer, ISingleLabelClassification> castedReport = this.runReport.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class);

			// add all the prediction rows.
			for (int i = 0; i < castedReport.size(); i++) {
				ISingleLabelClassification pred = castedReport.getPrediction(i);
				sb.append(IntStream.range(0, labels.size()).mapToObj(x -> pred.getProbabilityOfLabel(x) + "").collect(Collectors.joining(","))).append(",").append(labels.get(pred.getPrediction())).append(",")
						.append(labels.get((int) this.runReport.getTestSet().get(i).getLabel())).append("\n");
			}
		} else { // regression data
			this.runReport.getPredictionDiffList().getCastedView(Double.class, IRegressionPrediction.class);
			sb.append("predictions").append(",").append("truth").append("\n");
			IPredictionAndGroundTruthTable<Double, IRegressionPrediction> castedReport = this.runReport.getPredictionDiffList().getCastedView(Double.class, IRegressionPrediction.class);
			for (int i = 0; i < castedReport.size(); i++) {
				sb.append(castedReport.getPrediction(i).getDoublePrediction()).append(",").append(castedReport.getGroundTruth(i)).append("\n");
			}
		}

		return sb.toString();
	}

}
