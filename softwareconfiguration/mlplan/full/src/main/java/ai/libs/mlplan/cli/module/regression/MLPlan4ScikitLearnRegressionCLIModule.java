package ai.libs.mlplan.cli.module.regression;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.mlplan.cli.MLPlanCLI;
import ai.libs.mlplan.cli.module.IMLPlanCLIModule;
import ai.libs.mlplan.multiclass.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlan4ScikitLearnRegressionCLIModule extends AMLPlan4RegressionCLIModule implements IMLPlanCLIModule {

	public static final String M_RUL = "sklearn-rul";

	public MLPlan4ScikitLearnRegressionCLIModule() {
		super(Arrays.asList(M_RUL), M_RUL, AMLPlan4RegressionCLIModule.L_AL);
	}

	@Override
	public MLPlanScikitLearnBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		MLPlanScikitLearnBuilder builder = null;
		switch (cl.getOptionValue(MLPlanCLI.O_MODULE)) {
		case "sklearn-rul":
			builder = MLPlanScikitLearnBuilder.forRUL();
			break;
		}
		this.configureLoss(cl, builder);
		return builder;
	}

	@Override
	public String getRunReportAsString(final ISupervisedLearner learner, final ILearnerRunReport runReport) {
		return "";
	}
}
