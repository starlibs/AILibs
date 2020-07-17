package ai.libs.mlplan.cli.module.regression;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;
import ai.libs.jaicore.ml.regression.loss.dataset.R2;
import ai.libs.mlplan.cli.module.AMLPlanCLIModule;
import ai.libs.mlplan.cli.module.UnsupportedModuleConfigurationException;
import ai.libs.mlplan.core.AMLPlanBuilder;

public abstract class AMLPlan4RegressionCLIModule extends AMLPlanCLIModule {

	public static final String L_AL = "ASYMMETRIC_LOSS";
	public static final String L_MAPE = "MEAN_ABSOLUTE_PERCENTAGE_ERROR";
	public static final String L_MSE = "MEAN_SQUARED_ERROR";
	public static final String L_RMSE = "ROOT_MEAN_SQUARED_ERROR";
	public static final String L_R2 = "R2";

	public AMLPlan4RegressionCLIModule(final List<String> subModules, final String defaultModule) {
		this(subModules, defaultModule, L_RMSE);
	}

	public AMLPlan4RegressionCLIModule(final List<String> subModules, final String defaultModule, final String defaultMeasure) {
		super(subModules, defaultModule, Arrays.asList(L_AL, L_MAPE, L_MSE, L_RMSE, L_R2), defaultMeasure);
	}

	protected void configureLoss(final CommandLine cl, final AMLPlanBuilder builder) {
		switch (this.getPerformanceMeasure(cl)) {
		case L_AL:
			builder.withPerformanceMeasure(ERulPerformanceMeasure.ASYMMETRIC_LOSS);
			break;
		case L_MAPE:
			builder.withPerformanceMeasure(ERulPerformanceMeasure.MEAN_ABSOLUTE_PERCENTAGE_ERROR);
			break;
		case L_MSE:
			builder.withPerformanceMeasure(ERulPerformanceMeasure.MEAN_SQUARED_ERROR);
			break;
		case L_RMSE:
			builder.withPerformanceMeasure(ERulPerformanceMeasure.ROOT_MEAN_SQUARED_ERROR);
			break;
		case L_R2:
			builder.withPerformanceMeasure(new R2());
			break;
		default:
			throw new UnsupportedModuleConfigurationException("Chosen performance measure is not available for ML-Plan regression");
		}
	}
}
