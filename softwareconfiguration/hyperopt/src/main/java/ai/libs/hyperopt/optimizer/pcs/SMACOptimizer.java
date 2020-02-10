package ai.libs.hyperopt.optimizer.pcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hyperopt.optimizer.OptimizationException;
import ai.libs.hyperopt.util.ScenarioFileUtil;
import ai.libs.jaicore.basic.FileUtil;

/**
 *
 * @author kadirayk
 *
 */
public class SMACOptimizer extends APCSBasedOptimizer {

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
	private Boolean paralellize;
	private Integer numThreads;

	private Logger logger = LoggerFactory.getLogger(SMACOptimizer.class);

	public SMACOptimizer(final PCSBasedOptimizerInput input, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
		this.input = input;
		this.evaluator = evaluator;
	}

	private SMACOptimizer(final SMACOptimizerBuilder builder) {
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
		this.numThreads = builder.numThreads;
	}

	@Override
	public void optimize() throws OptimizationException {
		if (StringUtils.isEmpty(this.executionPath)) {
			throw new OptimizationException("executionPath must be set for Optimizer");
		}
		String filePath = this.executionPath;

		this.startGrpcServer();

		String[] arr = this.input.getRequestedComponent().split("\\.");
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
		PCSBasedOptimizerConfig config = PCSBasedOptimizerConfig.get("conf/smac-optimizer-config.properties");
		Integer port = config.getPort();
		ScenarioFileUtil.updateParam(filePath, "gRPC_port", String.valueOf(port));

		StringBuilder command = new StringBuilder();
		command.append("cd ").append(filePath).append("&& python run.py --scenario scenario.txt");

		if (this.paralellize) {
			command.append(" --shared_model True --input_psmac_dirs smac3-output*");

			ExecutorService executor = Executors.newFixedThreadPool(this.numThreads);
			for (int i = 0; i < this.numThreads; i++) {
				executor.submit(() -> this.executeCommand(command.toString(), filePath));
			}
			executor.shutdown();
			try {
				if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException ex) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		} else {
			this.executeCommand(command.toString(), filePath);
		}

	}

	private void executeCommand(final String command, final String filePath) {
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);

		builder.redirectErrorStream(true);
		Process p = null;
		try {
			p = builder.start();
		} catch (IOException e) {
			this.logger.error(e.getMessage());
			this.logger.error(MessageFormat.format("Unable spawn python process={0} in path={1} ", "run.py", filePath));
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
	@Override
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
