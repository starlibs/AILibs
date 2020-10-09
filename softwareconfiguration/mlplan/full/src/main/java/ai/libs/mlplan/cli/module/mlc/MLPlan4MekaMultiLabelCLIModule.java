package ai.libs.mlplan.cli.module.mlc;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.ExactMatch;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.F1MacroAverageL;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.F1MicroAverage;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.Hamming;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.InstanceWiseF1;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.JaccardScore;
import ai.libs.mlplan.cli.MLPlanCLI;
import ai.libs.mlplan.cli.module.AMLPlanCLIModule;
import ai.libs.mlplan.cli.module.IMLPlanCLIModule;
import ai.libs.mlplan.cli.module.UnsupportedModuleConfigurationException;
import ai.libs.mlplan.meka.ML2PlanMekaBuilder;

// Multi-Label: EXACT_MATCH, INSTANCE_F1, LABEL_F1, MICRO_F1, HAMMING, JACCARD, RANK

public class MLPlan4MekaMultiLabelCLIModule extends AMLPlanCLIModule implements IMLPlanCLIModule {

	public static final String M_MEKA = "meka";

	public static final String L_HAMMING = "HAMMING";
	public static final String L_JACCARD = "JACCARD";
	public static final String L_RANK = "RANK";
	public static final String L_EXACT_MATCH = "EXACT_MATCH";
	public static final String L_INSTANCE_F1 = "INSTANCE_F1";
	public static final String L_LABEL_F1 = "LABEL_F1";
	public static final String L_MICRO_F1 = "MICRO_F1";

	public MLPlan4MekaMultiLabelCLIModule() {
		super(Arrays.asList(M_MEKA), M_MEKA, Arrays.asList(L_EXACT_MATCH, L_INSTANCE_F1, L_LABEL_F1, L_MICRO_F1), L_INSTANCE_F1);
	}

	@Override
	public ML2PlanMekaBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		ML2PlanMekaBuilder builder = new ML2PlanMekaBuilder();

		switch (cl.getOptionValue(MLPlanCLI.O_MODULE)) {
		case L_INSTANCE_F1:
			builder.withPerformanceMeasure(new InstanceWiseF1());
			break;
		case L_LABEL_F1:
			builder.withPerformanceMeasure(new F1MacroAverageL());
			break;
		case L_MICRO_F1:
			builder.withPerformanceMeasure(new F1MicroAverage());
			break;
		case L_EXACT_MATCH:
			builder.withPerformanceMeasure(new ExactMatch());
			break;
		case L_HAMMING:
			builder.withPerformanceMeasure(new Hamming());
			break;
		case L_JACCARD:
			builder.withPerformanceMeasure(new JaccardScore());
			break;
		default:
			throw new UnsupportedModuleConfigurationException("Performance measure is not available for ML2-Plan");
		}

		return builder;
	}

	@Override
	public String getRunReportAsString(final ISupervisedLearner learner, final ILearnerRunReport runReport) {
		return null;
	}

}
