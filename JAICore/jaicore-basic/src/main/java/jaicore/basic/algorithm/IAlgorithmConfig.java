package jaicore.basic.algorithm;

import org.aeonbits.owner.Mutable;

public interface IAlgorithmConfig extends Mutable {

	public static final String K_CPUS = "cpus";
	public static final String K_THREADS = "threads";
	public static final String K_MEMORY = "memory";
	public static final String K_TIMEOUT = "timeout";

	/**
	 * @return Number of CPU cores available for parallelization.
	 */
	@Key(K_CPUS)
	@DefaultValue("8")
	public int cpus();
	
	/**
	 * @return Number of threads that may be spawned by the algorithm.
	 */
	@Key(K_THREADS)
	@DefaultValue("-1")
	public int threads();
	
	/**
	 * @return The main memory that is available to be used. This is merely a documentation variable since the true memory must be set over the JVM initialization anyway and cannot be restricted inside of it.
	 */
	@Key(K_MEMORY)
	@DefaultValue("256")
	public int memory();

	/**
	 * @return Overall timeout for the algorithm in milliseconds.
	 */
	@Key(K_TIMEOUT)
	@DefaultValue("-1")
	public long timeout();
}
