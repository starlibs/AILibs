package ai.libs.mlplan.cli.module;

import static ai.libs.mlplan.cli.MLPlanCLI.getDefault;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;

import ai.libs.jaicore.ml.classification.loss.dataset.AreaUnderROCCurve;
import ai.libs.jaicore.ml.classification.loss.dataset.AveragedInstanceLoss;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.loss.dataset.ErrorRate;
import ai.libs.jaicore.ml.classification.loss.dataset.F1Measure;
import ai.libs.jaicore.ml.classification.loss.dataset.Precision;
import ai.libs.jaicore.ml.classification.loss.dataset.Recall;
import ai.libs.jaicore.ml.classification.loss.instance.LogLoss;
import ai.libs.mlplan.cli.MLPlanCLI;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlan4WekaClassificationCLIModule implements IMLPlanCLIModule {

	public MLPlan4WekaClassificationCLIModule() {

	}

	@Override
	public MLPlanWekaBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		MLPlanWekaBuilder builder;
		if (!(fitDataset.getLabelAttribute() instanceof ICategoricalAttribute)) {
			throw new UnsupportedModuleConfigurationException("ML-Plan for classification requires a categorical target attribute.");
		}
		ICategoricalAttribute labelAtt = (ICategoricalAttribute) fitDataset.getLabelAttribute();

		switch (cl.getOptionValue(MLPlanCLI.O_MODULE, getDefault(MLPlanCLI.O_MODULE))) {
		case "weka":
			builder = MLPlanWekaBuilder.forClassification();
			break;
		case "weka-tiny":
			builder = MLPlanWekaBuilder.forClassificationWithTinySearchSpace();
			break;
		default:
			throw new UnsupportedModuleConfigurationException("The selected module is not available via this CLI module.");
		}

		int positiveClassIndex = Integer.parseInt(cl.getOptionValue(MLPlanCLI.O_POS_CLASS_INDEX, getDefault(MLPlanCLI.O_POS_CLASS_INDEX)));
		if (cl.hasOption(MLPlanCLI.O_POS_CLASS_NAME)) {
			positiveClassIndex = labelAtt.getLabels().indexOf(cl.getOptionValue(MLPlanCLI.O_POS_CLASS_NAME));
			if (positiveClassIndex < 0) {
				throw new UnsupportedModuleConfigurationException("The provided name of the positive class is not contained in the list of class labels");
			}
		}

		if (cl.hasOption(MLPlanCLI.O_LOSS)) {
			String performanceMeasure = cl.getOptionValue(MLPlanCLI.O_LOSS, "ERRORRATE");
			List<String> binaryMeasures = Arrays.asList("AUC", "PRECISION", "F1", "RECALL");
			if (binaryMeasures.contains(performanceMeasure) && labelAtt.getLabels().size() > 2) {
				throw new UnsupportedModuleConfigurationException("Cannot use binary performance measure for non-binary classification dataset.");
			}

			switch (cl.getOptionValue(MLPlanCLI.O_LOSS, "ERRORRATE")) {
			case "ERRORRATE":
				builder.withPerformanceMeasure(EClassificationPerformanceMeasure.ERRORRATE);
				break;
			case "LOGLOSS":
				builder.withPerformanceMeasure(new AveragedInstanceLoss(new LogLoss()));
				break;
			case "AUC":
				builder.withPerformanceMeasure(new AreaUnderROCCurve(positiveClassIndex));
				break;
			case "F1":
				builder.withPerformanceMeasure(new F1Measure(positiveClassIndex));
				break;
			case "PRECISION":
				builder.withPerformanceMeasure(new Precision(positiveClassIndex));
				break;
			case "RECALL":
				builder.withPerformanceMeasure(new Recall(positiveClassIndex));
				break;
			}
		}

		return builder;
	}

	@Override
	public List<String> getSettingOptionValues() {
		return Arrays.asList("weka", "weka-tiny");
	}

	@Override
	public String getRunReportAsString(final ILearnerRunReport runReport) {
		StringBuilder sb = new StringBuilder();
		sb.append("Error-Rate: ").append(new ErrorRate().loss(runReport.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class)));
		return sb.toString();
	}

}
