package ai.libs.mlplan.cli.module;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;

import ai.libs.mlplan.core.AMLPlanBuilder;

public class MLPlan4SKLearnClassificationCLIModule implements IMLPlanCLIModule {

	public MLPlan4SKLearnClassificationCLIModule() {
	}

	@Override
	public AMLPlanBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		return null;
	}

	@Override
	public List<String> getSettingOptionValues() {
		return Arrays.asList("sklearn", "sklearn-unlimited");
	}

	@Override
	public String getRunReportAsString(final ILearnerRunReport runReport) {
		return null;
	}

}
