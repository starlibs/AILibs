package ai.libs.mlplan.cli.module;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;

import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlan4WekaClassificationCLIModule implements IMLPlanCLIModule {

	public MLPlan4WekaClassificationCLIModule() {

	}

	@Override
	public MLPlanWekaBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();

		return builder;
	}

	@Override
	public List<String> getSettingOptionValues() {
		return Arrays.asList("weka", "weka-tiny");
	}

	@Override
	public String getRunReportAsString(final ILearnerRunReport runReport) {
		return null;
	}

}
