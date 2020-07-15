package ai.libs.mlplan.cli.module;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;

import ai.libs.mlplan.core.AbstractMLPlanBuilder;

public interface IMLPlanCLIModule {

	public AbstractMLPlanBuilder getMLPlanBuilderForSetting(CommandLine cl, ILabeledDataset fitDataset) throws IOException;

	public String getRunReportAsString(ILearnerRunReport runReport);

	public List<String> getSettingOptionValues();

}
