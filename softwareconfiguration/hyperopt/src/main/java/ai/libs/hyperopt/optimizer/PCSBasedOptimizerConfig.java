package ai.libs.hyperopt.optimizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;

/**
 *
 * @author kadirayk
 *
 */
public interface PCSBasedOptimizerConfig extends Mutable, Accessible {

	public static final String PCS_BASED_OPTIMIZER_IP = "pcs_based_optimizer.ip";
	public static final String PCS_OPTIMIZER_PORT = "pcs_based_optimizer.port";

	@Key(PCS_BASED_OPTIMIZER_IP)
	@DefaultValue("localhost")
	public String getIP();

	@Key(PCS_OPTIMIZER_PORT)
	@DefaultValue("8080")
	public Integer getPort();

	public static PCSBasedOptimizerConfig get(final String file) throws IOException {
		return get(new File(file));
	}

	public static PCSBasedOptimizerConfig get(final File file) throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(file));
		return ConfigFactory.create(PCSBasedOptimizerConfig.class, props);
	}

}
