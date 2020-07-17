package ai.libs.mlplan.cli.module.slc;

import static ai.libs.mlplan.cli.MLPlanCLI.getDefault;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.jaicore.ml.classification.loss.dataset.AreaUnderROCCurve;
import ai.libs.jaicore.ml.classification.loss.dataset.AveragedInstanceLoss;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.loss.dataset.ErrorRate;
import ai.libs.jaicore.ml.classification.loss.dataset.F1Measure;
import ai.libs.jaicore.ml.classification.loss.dataset.Precision;
import ai.libs.jaicore.ml.classification.loss.dataset.Recall;
import ai.libs.jaicore.ml.classification.loss.instance.LogLoss;
import ai.libs.mlplan.cli.MLPlanCLI;
import ai.libs.mlplan.cli.module.AMLPlanCLIModule;
import ai.libs.mlplan.cli.module.IMLPlanCLIModule;
import ai.libs.mlplan.cli.module.UnsupportedModuleConfigurationException;
import ai.libs.mlplan.core.AMLPlanBuilder;

public abstract class AMLPlan4ClassificationCLIModule extends AMLPlanCLIModule implements IMLPlanCLIModule {

	// binary only
	private static final String L_AUC = "AUC";
	private static final String L_F1 = "F1";
	private static final String L_PRECISION = "PRECISION";
	private static final String L_RECALL = "RECALL";
	// binary + multinomial
	private static final String L_ERRORRATE = "ERRORRATE";
	private static final String L_LOGLOSS = "LOGLOSS";

	private static final List<String> BINARY_ONLY_MEASURES = Arrays.asList(L_AUC, L_F1, L_PRECISION, L_RECALL);

	protected AMLPlan4ClassificationCLIModule(final List<String> subModules, final String defaultModule) {
		super(subModules, defaultModule, Arrays.asList(L_AUC, L_F1, L_PRECISION, L_RECALL, L_ERRORRATE, L_LOGLOSS), L_ERRORRATE);
	}

	protected void configureLoss(final CommandLine cl, final ICategoricalAttribute labelAtt, final AMLPlanBuilder builder) {
		int positiveClassIndex = Integer.parseInt(cl.getOptionValue(MLPlanCLI.O_POS_CLASS_INDEX, getDefault(MLPlanCLI.O_POS_CLASS_INDEX)));
		if (cl.hasOption(MLPlanCLI.O_POS_CLASS_NAME)) {
			positiveClassIndex = labelAtt.getLabels().indexOf(cl.getOptionValue(MLPlanCLI.O_POS_CLASS_NAME));
			if (positiveClassIndex < 0) {
				throw new UnsupportedModuleConfigurationException("The provided name of the positive class is not contained in the list of class labels");
			}
		}

		String performanceMeasure = cl.getOptionValue(MLPlanCLI.O_LOSS, L_ERRORRATE);
		if (BINARY_ONLY_MEASURES.contains(performanceMeasure) && labelAtt.getLabels().size() > 2) {
			throw new UnsupportedModuleConfigurationException("Cannot use binary performance measure for non-binary classification dataset.");
		}

		switch (performanceMeasure) {
		case L_ERRORRATE:
			builder.withPerformanceMeasure(EClassificationPerformanceMeasure.ERRORRATE);
			break;
		case L_LOGLOSS:
			builder.withPerformanceMeasure(new AveragedInstanceLoss(new LogLoss()));
			break;
		case L_AUC:
			builder.withPerformanceMeasure(new AreaUnderROCCurve(positiveClassIndex));
			break;
		case L_F1:
			builder.withPerformanceMeasure(new F1Measure(positiveClassIndex));
			break;
		case L_PRECISION:
			builder.withPerformanceMeasure(new Precision(positiveClassIndex));
			break;
		case L_RECALL:
			builder.withPerformanceMeasure(new Recall(positiveClassIndex));
			break;
		default:
			throw new UnsupportedModuleConfigurationException("Unsupported measure " + performanceMeasure);
		}
	}

	public ICategoricalAttribute getLabelAttribute(final ILabeledDataset fitDataset) {
		if (!(fitDataset.getLabelAttribute() instanceof ICategoricalAttribute)) {
			throw new UnsupportedModuleConfigurationException("ML-Plan for classification requires a categorical target attribute.");
		}
		return (ICategoricalAttribute) fitDataset.getLabelAttribute();
	}

	@Override
	public String getRunReportAsString(final ISupervisedLearner learner, final ILearnerRunReport runReport) {
		StringBuilder sb = new StringBuilder();
		sb.append(learner).append("\n");
		sb.append("Error-Rate: ").append(new ErrorRate().loss(runReport.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class)));
		return sb.toString();
	}

}
