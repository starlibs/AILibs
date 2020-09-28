package ai.libs.mlplan.cli.module;

import java.util.List;

public abstract class AMLPlanCLIModule implements IMLPlanCLIModule {

	private List<String> subModules;
	private String defaultModule;

	private List<String> performanceMeasures;
	private String defaultPerformanceMeasure;

	protected AMLPlanCLIModule(final List<String> subModules, final String defaultModule, final List<String> performanceMeasures, final String defaultPerformanceMeasure) {
		if (!(subModules.contains(defaultModule) && performanceMeasures.contains(defaultPerformanceMeasure))) {
			throw new IllegalArgumentException("The default value needs to be contained in the list of available options");
		}
		this.subModules = subModules;
		this.defaultModule = defaultModule;
		this.performanceMeasures = performanceMeasures;
		this.defaultPerformanceMeasure = defaultPerformanceMeasure;
	}

	@Override
	public String getDefaultSettingOptionValue() {
		return this.defaultModule;
	}

	@Override
	public List<String> getSettingOptionValues() {
		return this.subModules;
	}

	@Override
	public String getDefaultPerformanceMeasure() {
		return this.defaultPerformanceMeasure;
	}

	@Override
	public List<String> getPerformanceMeasures() {
		return this.performanceMeasures;
	}

}
