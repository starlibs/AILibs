package de.upb.crc901.automl.hascoml.supervised.multiclass.weka;

import java.io.IOException;

import org.aeonbits.owner.ConfigCache;

import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML;
import de.upb.crc901.mlplan.multiclass.weka.WEKAPipelineFactory;
import hasco.serialization.ComponentLoader;

/**
 * HASCO For supervised machine learning using WEKA as a library for machine learning algorithms.
 *
 * @author wever
 */
public class HASCOForWekaML extends HASCOSupervisedML {

	/** Configuration object holding all the parameters of this HASCO instantiation. */
	private static final HASCOForWekaMLConfig CONFIG = ConfigCache.getOrCreate(HASCOForWekaMLConfig.class);

	/**
	 * Standard c'tor.
	 *
	 * @throws IOException
	 *             An IOException is thrown if the components file could not be loaded.
	 */
	public HASCOForWekaML() throws IOException {
		super(new ComponentLoader(CONFIG.componentsFile()));
		this.setFactory(new WEKAPipelineFactory());
	}

	@Override
	public HASCOForWekaMLConfig getConfig() {
		return CONFIG;
	}

}
