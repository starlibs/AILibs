package ai.libs.jaicore.basic;

import org.aeonbits.owner.Reloadable;
import org.api4.java.algorithm.IAlgorithmConfig;

/**
 * Configuration interface to defined the access properties for a database connection
 *
 * @author fmohr
 *
 */
public interface IOwnerBasedAlgorithmConfig extends IOwnerBasedConfig, IAlgorithmConfig, Reloadable {

	public static final String K_CPUS = "cpus";
	public static final String K_THREADS = "threads";
	public static final String K_MEMORY = "memory";
	public static final String K_TIMEOUT = "timeout";

	/**
	 * @return Number of CPU cores available for parallelization.
	 */
	@Override
	@Key(K_CPUS)
	@DefaultValue("1")
	public int cpus();

	/**
	 * @return Number of threads that may be spawned by the algorithm. If set to -1, the number of CPUs is used as the number of threads. If set to 0, parallelization is deactivated.
	 */
	@Override
	@Key(K_THREADS)
	@DefaultValue("-1")
	public int threads();

	/**
	 * @return The main memory that is available to be used. This is merely a documentation variable since the true memory must be set over the JVM initialization anyway and cannot be restricted inside of it.
	 */
	@Override
	@Key(K_MEMORY)
	@DefaultValue("256")
	public int memory();

	/**
	 * @return Overall timeout for the algorithm in milliseconds.
	 */
	@Override
	@Key(K_TIMEOUT)
	@DefaultValue("-1")
	public long timeout();
}
