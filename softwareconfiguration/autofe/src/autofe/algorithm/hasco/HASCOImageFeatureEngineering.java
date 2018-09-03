package autofe.algorithm.hasco;

import java.io.IOException;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML;
import hasco.serialization.ComponentLoader;
import jaicore.basic.ILoggingCustomizable;

public class HASCOImageFeatureEngineering extends HASCOSupervisedML<FilterPipeline> implements ILoggingCustomizable {

	private static final HASCOImageFeatureEngineeringConfig CONFIG = ConfigCache.getOrCreate(HASCOImageFeatureEngineeringConfig.class);

	/** Logger for controlled output */
	private Logger logger = LoggerFactory.getLogger(HASCOImageFeatureEngineering.class);

	/** Logger name that can be used to customize logging outputs in a more convenient way. */
	private String loggerName;

	public HASCOImageFeatureEngineering(final ComponentLoader componentLoader) throws IOException {
		super(componentLoader);
		this.setPreferredNodeEvaluator(n -> {
			System.out.println("Preferred node evaluator is called.");
			return null;
		});
	}

	@Override
	public HASCOImageFeatureEngineeringConfig getConfig() {
		return CONFIG;
	}

}
