package ai.libs.mlplan.cli.module.slc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.mlplan.cli.MLPlanCLI;
import ai.libs.mlplan.cli.module.IMLPlanCLIModule;
import ai.libs.mlplan.cli.module.UnsupportedModuleConfigurationException;
import ai.libs.mlplan.multiclass.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlan4ScikitLearnClassificationCLIModule extends AMLPlan4ClassificationCLIModule implements IMLPlanCLIModule {

	private static final String M_SKLEARN = "sklearn";
	private static final String M_ULSKLEARN = "sklearn-unlimited";
	private static final List<String> MODULE_OPTION_VALUES = Arrays.asList(M_SKLEARN, M_ULSKLEARN);

	public MLPlan4ScikitLearnClassificationCLIModule() {
		super();
	}

	@Override
	public MLPlanScikitLearnBuilder getMLPlanBuilderForSetting(final CommandLine cl, final ILabeledDataset fitDataset) throws IOException {
		ICategoricalAttribute labelAtt = this.getLabelAttribute(fitDataset);

		// get the respective builder
		MLPlanScikitLearnBuilder builder;
		switch (cl.getOptionValue(MLPlanCLI.O_MODULE, "sklearn")) {
		case M_SKLEARN:
			builder = MLPlanScikitLearnBuilder.forClassification();
			break;
		case M_ULSKLEARN:
			builder = MLPlanScikitLearnBuilder.forClassificationWithUnlimitedLength();
			break;
		default:
			throw new UnsupportedModuleConfigurationException("Unknown module configured for scikit-learn classification module.");
		}

		// configure classification loss
		this.configureLoss(cl, labelAtt, builder);

		return builder;
	}

	@Override
	public List<String> getSettingOptionValues() {
		return MODULE_OPTION_VALUES;
	}

}
