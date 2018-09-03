package jaicore.basic.algorithm;

import org.aeonbits.owner.Mutable;

public interface IAlgorithmConfig extends Mutable {
	public static final String K_CPUS = "cpus";
	public static final String K_MEMORY = "memory";
	public static final String K_TIMEOUT = "timeout";

	/**
	 * @return Number of CPU cores available for parallelization
	 */
	@Key(K_CPUS)
	@DefaultValue("1")
	public int cpus();

	/**
	 * @return The main memory that is available to be used. This is merely a documentation variable since the true memory must be set over the JVM initialization anyway and cannot be restricted inside of it
	 */
	@Key(K_MEMORY)
	@DefaultValue("256")
	public int memory();

	/**
	 * @return Overall timeout for the configuration.
	 */
	@Key(K_TIMEOUT)
	@DefaultValue("1000000000")
	public int timeout();
}
