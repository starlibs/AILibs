package hasco.core;

import java.io.File;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@Sources({ "file:conf/hasco.properties" })
public interface HASCOConfig extends Mutable {

	public static final String K_COMPONENTS_FILE = "hasco.components_file";
	public static final String K_CPUS = "hasco.cpus";
	public static final String K_MEMORY = "hasco.memory";
	public static final String K_RANDOM_SEED = "hasco.seed";
	public static final String K_RANDOM_COMPLETIONS = "hasco.random_completions";
	public static final String K_REQUESTED_INTERFACE = "hasco.requested_interface";
	public static final String K_TIMEOUT = "hasco.timeout";

	/**
	 * @return The root file containing a json description of all available components.
	 */
	@Key(K_COMPONENTS_FILE)
	public File componentsFile();

	/**
	 * @return Number of CPU cores available for parallelization
	 */
	@Key(K_CPUS)
	@DefaultValue("1")
	public int cpus();

	/**
	 * @return The main memory that is available to be used.
	 */
	@Key(K_MEMORY)
	@DefaultValue("256")
	public int memory();

	/**
	 * @return The seed for the pseudo randomness generator.
	 */
	@Key(K_RANDOM_SEED)
	@DefaultValue("0")
	public int randomSeed();

	/**
	 * @return Number of random completions drawn with RDFS.
	 */
	@Key(K_RANDOM_COMPLETIONS)
	@DefaultValue("3")
	public int randomCompletions();

	/**
	 * @return Overall timeout for the configuration.
	 */
	@Key(K_TIMEOUT)
	@DefaultValue("60")
	public int timeout();

	/**
	 * @return The name of the interface which is requested.
	 */
	@Key(K_REQUESTED_INTERFACE)
	public String requestedInterface();

}
