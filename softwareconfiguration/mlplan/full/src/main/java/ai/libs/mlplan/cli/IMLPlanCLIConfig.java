package ai.libs.mlplan.cli;

import org.aeonbits.owner.Config;

public interface IMLPlanCLIConfig extends Config {

	@Key("mlplancli.config.timeunit.def")
	@DefaultValue("SECONDS")
	public String getDefaultTimeUnit();

}
