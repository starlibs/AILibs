package autofe.algorithm.hasco;

import org.aeonbits.owner.Config.Sources;

import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;

@Sources({ "file:conf/autofeml-complete.properties" })
public interface MLPlanFEWekaClassifierConfig extends MLPlanClassifierConfig, HASCOFeatureEngineeringConfig {

	public static final String MLPLAN_SUBSAMPLING_FACTOR = "autofe.mlplanSubsampleRatioFactor";

	@Key(MLPLAN_SUBSAMPLING_FACTOR)
	@DefaultValue("5")
	public int mlplanSubsamplingFactor();
}
