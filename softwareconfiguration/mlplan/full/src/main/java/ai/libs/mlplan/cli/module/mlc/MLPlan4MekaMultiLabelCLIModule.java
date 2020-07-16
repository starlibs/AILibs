package ai.libs.mlplan.cli.module.mlc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.mlplan.cli.module.IMLPlanCLIModule;
import ai.libs.mlplan.multilabel.mekamlplan.ML2PlanMekaBuilder;

public class MLPlan4MekaMultiLabelCLIModule implements IMLPlanCLIModule {

	public MLPlan4MekaMultiLabelCLIModule() {
	}

	@Override
	public ML2PlanMekaBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		return null;
	}

	@Override
	public List<String> getSettingOptionValues() {
		return Arrays.asList("meka");
	}

	@Override
	public String getRunReportAsString(final ISupervisedLearner learner, final ILearnerRunReport runReport) {
		// TODO Auto-generated method stub
		return null;
	}
}
