package ai.libs.mlplan.cli.module.regression;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.mlplan.cli.MLPlanCLI;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlan4WEKARegressionCLIModule extends AMLPlan4RegressionCLIModule {

	private static final String M_WEKA = "weka-regression";

	public MLPlan4WEKARegressionCLIModule() {
		super(Arrays.asList(M_WEKA), M_WEKA);
	}

	@Override
	public MLPlanWekaBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		MLPlanWekaBuilder builder = null;
		switch (cl.getOptionValue(MLPlanCLI.O_MODULE)) {
		case M_WEKA:
			builder = MLPlanWekaBuilder.forClassification();
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
