package ai.libs.hyperopt.optimizer.pcs;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.hasco.model.ComponentInstance;

public class SMACOptimizerBuilder {

	private PCSBasedOptimizerInput input;
	private IObjectEvaluator<ComponentInstance, Double> evaluator;
	private Integer algoRunsTimelimit;
	private Integer alwaysRaceDefault;
	private Double costForCrash;
	private Double cutoff;
	private Integer deterministic;
	private Integer memoryLimit;
	private String overallObj;
	private String runObj;
	private Integer runCountLimit;
	private Double wallClockLimit;
	private String executionPath;
	private Boolean paralellize = Boolean.FALSE;
	private Integer numThreads = 1;

	public SMACOptimizerBuilder(final PCSBasedOptimizerInput input, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
		this.input = input;
		this.evaluator = evaluator;
	}

	public SMACOptimizerBuilder executionPath(final String executionPath) {
		this.executionPath = executionPath;
		return this;
	}

	/**
	 * Maximum amount of CPU-time used for optimization. Default: inf.
	 *
	 * @param algoRunsTimelimit
	 * @return
	 */
	public SMACOptimizerBuilder algoRunsTimelimit(final Integer algoRunsTimelimit) {
		this.algoRunsTimelimit = algoRunsTimelimit;
		return this;
	}

	/**
	 * Race new incumbents always against default configuration.
	 *
	 * @param alwaysRaceDefault
	 * @return
	 */
	public SMACOptimizerBuilder alwaysRaceDefault(final Integer alwaysRaceDefault) {
		this.alwaysRaceDefault = alwaysRaceDefault;
		return this;
	}

	/**
	 * Defines the cost-value for crashed runs on scenarios with quality as run-obj.
	 * Default: 2147483647.0.
	 *
	 * @param costForCrash
	 * @return
	 */
	public SMACOptimizerBuilder costForCrash(final Double costForCrash) {
		this.costForCrash = costForCrash;
		return this;
	}

	/**
	 * Maximum runtime, after which the target algorithm is cancelled. Required if
	 * *run_obj* is runtime.
	 *
	 * @param cutoff
	 * @return
	 */
	public SMACOptimizerBuilder cutoff(final Double cutoff) {
		this.cutoff = cutoff;
		return this;
	}

	/**
	 * If true, SMAC assumes that the target function or algorithm is deterministic
	 * (the same static seed of 0 is always passed to the function/algorithm). If
	 * false, different random seeds are passed to the target function/algorithm.
	 *
	 * @param deterministic
	 * @return
	 */
	public SMACOptimizerBuilder deterministic(final Integer deterministic) {
		this.deterministic = deterministic;
		return this;
	}

	/**
	 * Maximum available memory the target algorithm can occupy before being
	 * cancelled in MB.
	 *
	 * @param memoryLimit
	 * @return
	 */
	public SMACOptimizerBuilder memoryLimit(final Integer memoryLimit) {
		this.memoryLimit = memoryLimit;
		return this;
	}

	/**
	 * PARX, where X is an integer defining the penalty imposed on timeouts (i.e.
	 * runtimes that exceed the cutoff-time). Default: par10.
	 *
	 * @param overallObj
	 * @return
	 */
	public SMACOptimizerBuilder overallObj(final String overallObj) {
		this.overallObj = overallObj;
		return this;
	}

	/**
	 * Defines what metric to optimize. When optimizing runtime, cutoff_time is
	 * required as well.
	 *
	 * @param runObj
	 * @return
	 */
	public SMACOptimizerBuilder runObj(final String runObj) {
		this.runObj = runObj;
		return this;
	}

	/**
	 * Maximum number of algorithm-calls during optimization. Default: inf.
	 *
	 * @param runCountLimit
	 * @return
	 */
	public SMACOptimizerBuilder runCountLimit(final Integer runCountLimit) {
		this.runCountLimit = runCountLimit;
		return this;
	}

	/**
	 * Maximum amount of wallclock-time used for optimization. Default: inf.
	 *
	 * @param wallClockLimit
	 * @return
	 */
	public SMACOptimizerBuilder wallClockLimit(final Double wallClockLimit) {
		this.wallClockLimit = wallClockLimit;
		return this;
	}

	/**
	 * Set number of Threads in paralllel mode
	 *
	 * @param numThreads
	 * @return
	 */
	public SMACOptimizerBuilder numThreads(final Integer numThreads) {
		this.numThreads = numThreads;
		return this;
	}

}
