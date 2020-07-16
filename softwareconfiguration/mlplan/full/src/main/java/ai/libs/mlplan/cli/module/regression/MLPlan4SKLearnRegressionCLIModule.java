package ai.libs.mlplan.cli.module.regression;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.mlplan.cli.module.IMLPlanCLIModule;
import ai.libs.mlplan.core.AMLPlanBuilder;

public class MLPlan4SKLearnRegressionCLIModule implements IMLPlanCLIModule {

	@Override
	public AMLPlanBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSettingOptionValues() {
		return Arrays.asList("sklearn-rul");
	}

	@Override
	public String getRunReportAsString(final ISupervisedLearner learner, final ILearnerRunReport runReport) {
		// TODO Auto-generated method stub
		return null;
	}
}
