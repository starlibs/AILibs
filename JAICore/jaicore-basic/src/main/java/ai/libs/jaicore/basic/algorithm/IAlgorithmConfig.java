package ai.libs.jaicore.basic.algorithm;

import ai.libs.jaicore.basic.IConfig;

public interface IAlgorithmConfig extends IConfig {

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
	 * @return Number of threads that may be spawned by the algorithm. If set to -1, the number of CPUs is used as the number of threads. If set to 0, parallelization is deactivated.
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
