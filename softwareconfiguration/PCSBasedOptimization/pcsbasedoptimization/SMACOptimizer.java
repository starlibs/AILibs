package ai.libs.hasco.pcsbasedoptimization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.IObjectEvaluator;

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

	public SMACOptimizer(PCSBasedOptimizerInput input, IObjectEvaluator<ComponentInstance, Double> evaluator) {
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
	public static Builder SMACOptimizerBuilder(PCSBasedOptimizerInput input,
			IObjectEvaluator<ComponentInstance, Double> evaluator) {
		return new Builder(input, evaluator);
	}

	private SMACOptimizer(Builder builder) {
		input = builder.input;
		evaluator = builder.evaluator;
		algoRunsTimelimit = builder.algoRunsTimelimit;
		alwaysRaceDefault = builder.alwaysRaceDefault;
		costForCrash = builder.costForCrash;
		cutoff = builder.cutoff;
		deterministic = builder.deterministic;
		memoryLimit = builder.memoryLimit;
		overallObj = builder.overallObj;
		runObj = builder.runObj;
		runCountLimit = builder.runCountLimit;
		wallClockLimit = builder.wallClockLimit;
		executionPath = builder.executionPath;
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

		public Builder(PCSBasedOptimizerInput input, IObjectEvaluator<ComponentInstance, Double> evaluator) {
			this.input = input;
			this.evaluator = evaluator;
		}

		public SMACOptimizer build() {
			return new SMACOptimizer(this);
		}

		public Builder executionPath(String executionPath) {
			this.executionPath = executionPath;
			return this;
		}

		/**
		 * Maximum amount of CPU-time used for optimization. Default: inf.
		 * 
		 * @param algoRunsTimelimit
		 * @return
		 */
		public Builder algoRunsTimelimit(Integer algoRunsTimelimit) {
			this.algoRunsTimelimit = algoRunsTimelimit;
			return this;
		}

		/**
		 * Race new incumbents always against default configuration.
		 * 
		 * @param alwaysRaceDefault
		 * @return
		 */
		public Builder alwaysRaceDefault(Integer alwaysRaceDefault) {
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
		public Builder costForCrash(Double costForCrash) {
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
		public Builder cutoff(Double cutoff) {
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
		public Builder deterministic(Integer deterministic) {
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
		public Builder memoryLimit(Integer memoryLimit) {
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
		public Builder overallObj(String overallObj) {
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
		public Builder runObj(String runObj) {
			this.runObj = runObj;
			return this;
		}

		/**
		 * Maximum number of algorithm-calls during optimization. Default: inf.
		 * 
		 * @param runCountLimit
		 * @return
		 */
		public Builder runCountLimit(Integer runCountLimit) {
			this.runCountLimit = runCountLimit;
			return this;
		}

		/**
		 * Maximum amount of wallclock-time used for optimization. Default: inf.
		 * 
		 * @param wallClockLimit
		 * @return
		 */
		public Builder wallClockLimit(Double wallClockLimit) {
			this.wallClockLimit = wallClockLimit;
			return this;
		}

	}

	@Override
	public void optimize(String componentName) throws OptimizationException {
		if (StringUtils.isEmpty(executionPath)) {
			throw new OptimizationException("executionPath must be set for Optimizer");
		}
		String filePath = executionPath;

		startGrpcServer();

		String[] arr = componentName.split("\\.");
		String name = arr[arr.length - 1];

		try {
			setPcsFileForComponent(name, filePath);
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			throw new OptimizationException(
					MessageFormat.format("Unable to set PCS file with path={0} for Component={1}", filePath, name));
		}
		setOptions();

		// start SMAC
		startSMACScript(filePath);
	}

	private void startSMACScript(String filePath) throws OptimizationException {
		PCSBasedOptimizerConfig config = PCSBasedOptimizerConfig.get("conf/smac-optimizer-config.properties");
		Integer port = config.getPort();
		ScenarioFileUtil.updateParam(filePath, "gRPC_port", String.valueOf(port));
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
				"cd " + filePath + "&& python run.py --scenario scenario.txt");

		builder.redirectErrorStream(true);
		Process p = null;
		try {
			p = builder.start();
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new OptimizationException(
					MessageFormat.format("Unable spawn python process={0} in path={1} ", "run.py", filePath));
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line = null;
		List<String> smacOutLines = new ArrayList<>();
		while (true) {
			try {
				line = r.readLine();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			smacOutLines.add(line);
			System.out.println("SMAC out: " + line);
			if (line == null) {
				break;
			}
		}
		try {
			FileUtil.writeFileAsList(smacOutLines, "testrsc/smac.log");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Options will be set in the scenario file
	 * 
	 */
	public void setOptions() {
		Map<String, String> params = new HashMap<>();
		if (algoRunsTimelimit != null) {
			params.put("algo_runs_timelimit", String.valueOf(algoRunsTimelimit));
		}
		if (alwaysRaceDefault != null) {
			params.put("always_race_default", String.valueOf(alwaysRaceDefault));
		}

		if (costForCrash != null) {
			params.put("cost_for_crash", String.valueOf(costForCrash));
		}
		if (cutoff != null) {
			params.put("cutoff", String.valueOf(cutoff));
		}

		if (deterministic != null) {
			params.put("deterministic", String.valueOf(deterministic));
		}

		if (memoryLimit != null) {
			params.put("memory_limit", String.valueOf(memoryLimit));
		}
		if (overallObj != null) {
			params.put("overall_obj", String.valueOf(overallObj));
		}
		if (runObj != null) {
			params.put("run_obj", String.valueOf(runObj));
		}
		if (runCountLimit != null) {
			params.put("runcount_limit", String.valueOf(runCountLimit));
		}
		if (wallClockLimit != null) {
			params.put("wallclock_limit", String.valueOf(wallClockLimit));
		}
		ScenarioFileUtil.updateMultipleParams(executionPath, params);
	}

	public String getExecutionPath() {
		return executionPath;
	}
	
	public PCSBasedOptimizerInput getInput() {
		return input;
	}

}
