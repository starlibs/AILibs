package jaicore.experiments;

import org.aeonbits.owner.Config.Sources;

@Sources({ "file:testrsc/experiment.cfg" })
public interface IExperimentTesterConfig extends IExperimentSetConfig {
	public static final String A = "A";
	public static final String B = "B";

	@Key(A)
	public String getA();

	@Key(B)
	public String getB();
}
