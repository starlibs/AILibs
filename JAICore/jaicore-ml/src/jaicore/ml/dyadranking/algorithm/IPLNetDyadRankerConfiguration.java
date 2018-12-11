package jaicore.ml.dyadranking.algorithm;

import java.io.File;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;

@Sources({ "file:conf/plNet/plnet.properties" })
public interface IPLNetDyadRankerConfiguration extends IPredictiveModelConfiguration {

	public static final String K_PLNET_CONFIG_FILE = "plnet.configfile";

	@Key(K_PLNET_CONFIG_FILE)
	@DefaultValue("conf/plNet/plNetConfig.json")
	public File plNetConfig();

}
