package ai.libs.mlplan.cli.module.slc;

import static ai.libs.mlplan.cli.MLPlanCLI.getDefault;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.mlplan.cli.MLPlanCLI;
import ai.libs.mlplan.cli.module.UnsupportedModuleConfigurationException;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlan4WekaClassificationCLIModule extends AMLPlan4ClassificationCLIModule {

	public MLPlan4WekaClassificationCLIModule() {
		super();
	}

	@Override
	public MLPlanWekaBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		// try to get label attribute (also checks whether the dataset is really a classification dataset).
		ICategoricalAttribute labelAtt = this.getLabelAttribute(fitDataset);

		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();

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

		// configure the loss function
		this.configureLoss(cl, labelAtt, builder);

		return builder;
	}

	@Override
	public List<String> getSettingOptionValues() {
		return Arrays.asList("weka", "weka-tiny");
	}

}
