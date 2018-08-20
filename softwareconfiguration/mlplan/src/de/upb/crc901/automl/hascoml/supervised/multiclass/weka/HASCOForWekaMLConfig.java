package de.upb.crc901.automl.hascoml.supervised.multiclass.weka;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedMLConfig;

/**
 * Parameters for configuring HASCO specifically for supervised machine learning using algorithms from the library WEKA.
 *
 * @author wever
 */
@LoadPolicy(LoadType.MERGE)
@Sources({ "file:conf/hasco/hasco.properties", "file:conf/hasco/hasco-for-weka.properties" })
public interface HASCOForWekaMLConfig extends HASCOSupervisedMLConfig {

}
