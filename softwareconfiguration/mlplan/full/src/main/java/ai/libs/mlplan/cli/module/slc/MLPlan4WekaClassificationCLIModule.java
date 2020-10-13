package ai.libs.mlplan.cli.module.slc;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.mlplan.cli.MLPlanCLI;
import ai.libs.mlplan.cli.module.UnsupportedModuleConfigurationException;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;

public class MLPlan4WekaClassificationCLIModule extends AMLPlan4ClassificationCLIModule {

	private static final String M_WEKA = "weka";
	private static final String M_WEKA_TINY = "weka-tiny";

	public MLPlan4WekaClassificationCLIModule() {
		super(Arrays.asList(M_WEKA, M_WEKA_TINY), M_WEKA);
	}

	@Override
	public MLPlanWekaBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		// try to get label attribute (also checks whether the dataset is really a classification dataset).
		ICategoricalAttribute labelAtt = this.getLabelAttribute(fitDataset);

		MLPlanWekaBuilder builder;

		switch (cl.getOptionValue(MLPlanCLI.O_MODULE, this.getDefaultSettingOptionValue())) {
		case M_WEKA:
			builder = MLPlanWekaBuilder.forClassification();
			break;
		case M_WEKA_TINY:
			builder = MLPlanWekaBuilder.forClassificationWithTinySearchSpace();
			break;
		default:
			throw new UnsupportedModuleConfigurationException("The selected module is not available via this CLI module.");
		}

		// configure the loss function
		this.configureLoss(cl, labelAtt, builder);

		return builder;
	}

}
