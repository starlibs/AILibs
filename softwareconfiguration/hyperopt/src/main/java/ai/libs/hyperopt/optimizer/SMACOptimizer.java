package ai.libs.hyperopt.optimizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hyperopt.OptimizationException;
import ai.libs.hyperopt.PCSBasedOptimizerInput;
import ai.libs.hyperopt.ScenarioFileUtil;
import ai.libs.jaicore.basic.FileUtil;

/**
 *
 * @author kadirayk
 *
 */
public class SMACOptimizer extends AbstractPCSBasedOptimizer {

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

	private Logger logger = LoggerFactory.getLogger(SMACOptimizer.class);

	public SMACOptimizer(final PCSBasedOptimizerInput input, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
		this.input = input;
		this.evaluator = evaluator;
	}

	/**
	 * Used for building SMACOptimizer with options
	 *
	 * @param input
	 * @param evaluator
	 * @return
	 */
	public static Builder getSMACOptimizerBuilder(final PCSBasedOptimizerInput input, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
		return new Builder(input, evaluator);
	}

	private SMACOptimizer(final Builder builder) {
		this.input = builder.input;
		this.evaluator = builder.evaluator;
		this.algoRunsTimelimit = builder.algoRunsTimelimit;
		this.alwaysRaceDefault = builder.alwaysRaceDefault;
		this.costForCrash = builder.costForCrash;
		this.cutoff = builder.cutoff;
		this.deterministic = builder.deterministic;
		this.memoryLimit = builder.memoryLimit;
		this.overallObj = builder.overallObj;
		this.runObj = builder.runObj;
		this.runCountLimit = builder.runCountLimit;
		this.wallClockLimit = builder.wallClockLimit;
		this.executionPath = builder.executionPath;
	}

	public static class Builder {
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

		public Builder(final PCSBasedOptimizerInput input, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
			this.input = input;
			this.evaluator = evaluator;
		}

		public SMACOptimizer build() {
			return new SMACOptimizer(this);
		}

		public Builder executionPath(final String executionPath) {
			this.executionPath = executionPath;
			return this;
		}

		/**
		 * Maximum amount of CPU-time used for optimization. Default: inf.
		 *
		 * @param algoRunsTimelimit
		 * @return
		 */
		public Builder algoRunsTimelimit(final Integer algoRunsTimelimit) {
			this.algoRunsTimelimit = algoRunsTimelimit;
			return this;
		}

		/**
		 * Race new incumbents always against default configuration.
		 *
		 * @param alwaysRaceDefault
		 * @return
		 */
		public Builder alwaysRaceDefault(final Integer alwaysRaceDefault) {
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
		public Builder costForCrash(final Double costForCrash) {
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
		public Builder cutoff(final Double cutoff) {
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
		public Builder deterministic(final Integer deterministic) {
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
		public Builder memoryLimit(final Integer memoryLimit) {
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
		public Builder overallObj(final String overallObj) {
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
		public Builder runObj(final String runObj) {
			this.runObj = runObj;
			return this;
		}

		/**
		 * Maximum number of algorithm-calls during optimization. Default: inf.
		 *
		 * @param runCountLimit
		 * @return
		 */
		public Builder runCountLimit(final Integer runCountLimit) {
			this.runCountLimit = runCountLimit;
			return this;
		}

		/**
		 * Maximum amount of wallclock-time used for optimization. Default: inf.
		 *
		 * @param wallClockLimit
		 * @return
		 */
		public Builder wallClockLimit(final Double wallClockLimit) {
			this.wallClockLimit = wallClockLimit;
			return this;
		}

	}

	@Override
	public void optimize(final String componentName) throws OptimizationException {
		if (StringUtils.isEmpty(this.executionPath)) {
			throw new OptimizationException("executionPath must be set for Optimizer");
		}
		String filePath = this.executionPath;

		this.startGrpcServer();

		String[] arr = componentName.split("\\.");
		String name = arr[arr.length - 1];

		try {
			this.setPcsFileForComponent(name, filePath);
		} catch (IOException e1) {
			this.logger.error(e1.getMessage());
			throw new OptimizationException(MessageFormat.format("Unable to set PCS file with path={0} for Component={1}", filePath, name));
		}
		this.setOptions();

		// start SMAC
		this.startSMACScript(filePath);
	}

	private void startSMACScript(final String filePath) throws OptimizationException {
		PCSBasedOptimizerConfig config;
		try {
			config = PCSBasedOptimizerConfig.get("conf/smac-optimizer-config.properties");
		} catch (IOException e1) {
			throw new OptimizationException(e1);
		}
		Integer port = config.getPort();
		ScenarioFileUtil.updateParam(filePath, "gRPC_port", String.valueOf(port));
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd " + filePath + "&& python run.py --scenario scenario.txt");

		builder.redirectErrorStream(true);
		Process p = null;
		try {
			p = builder.start();
		} catch (IOException e) {
			this.logger.error(e.getMessage());
			throw new OptimizationException(MessageFormat.format("Unable spawn python process={0} in path={1} ", "run.py", filePath));
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line = null;
		List<String> smacOutLines = new ArrayList<>();
		while (true) {
			try {
				line = r.readLine();
			} catch (IOException e) {
				this.logger.error(e.getMessage());
			}
			smacOutLines.add(line);
			if (line == null) {
				break;
			}
		}
		try {
			FileUtil.writeFileAsList(smacOutLines, "testrsc/smac.log");
		} catch (IOException e) {
			throw new OptimizationException(e);
		}
	}

	/**
	 * Options will be set in the scenario file
	 *
	 */
	public void setOptions() {
		Map<String, String> params = new HashMap<>();
		if (this.algoRunsTimelimit != null) {
			params.put("algo_runs_timelimit", String.valueOf(this.algoRunsTimelimit));
		}
		if (this.alwaysRaceDefault != null) {
			params.put("always_race_default", String.valueOf(this.alwaysRaceDefault));
		}

		if (this.costForCrash != null) {
			params.put("cost_for_crash", String.valueOf(this.costForCrash));
		}
		if (this.cutoff != null) {
			params.put("cutoff", String.valueOf(this.cutoff));
		}

		if (this.deterministic != null) {
			params.put("deterministic", String.valueOf(this.deterministic));
		}

		if (this.memoryLimit != null) {
			params.put("memory_limit", String.valueOf(this.memoryLimit));
		}
		if (this.overallObj != null) {
			params.put("overall_obj", String.valueOf(this.overallObj));
		}
		if (this.runObj != null) {
			params.put("run_obj", String.valueOf(this.runObj));
		}
		if (this.runCountLimit != null) {
			params.put("runcount_limit", String.valueOf(this.runCountLimit));
		}
		if (this.wallClockLimit != null) {
			params.put("wallclock_limit", String.valueOf(this.wallClockLimit));
		}
		ScenarioFileUtil.updateMultipleParams(this.executionPath, params);
	}

	public String getExecutionPath() {
		return this.executionPath;
	}

	public PCSBasedOptimizerInput getInput() {
		return this.input;
	}

}
