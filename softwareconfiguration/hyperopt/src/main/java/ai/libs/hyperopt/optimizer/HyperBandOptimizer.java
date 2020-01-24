package ai.libs.hyperopt.optimizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
public class HyperBandOptimizer extends AbstractPCSBasedOptimizer {

	private Logger logger = LoggerFactory.getLogger(HyperBandOptimizer.class);
	private Double minBudget;
	private Double maxBudget;
	private Integer nIterations;
	private String executionPath;

	public HyperBandOptimizer(final PCSBasedOptimizerInput input, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
		this.input = input;
		this.evaluator = evaluator;
	}

	/**
	 * returns a builder for the optional parameters, mandatory parameters must be
	 * given
	 *
	 * @param input
	 * @param evaluator
	 * @return
	 */
	public static Builder getHyperBandOptimizerBuilder(final PCSBasedOptimizerInput input, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
		return new Builder(input, evaluator);
	}

	private HyperBandOptimizer(final Builder builder) {
		this.input = builder.input;
		this.evaluator = builder.evaluator;
		this.minBudget = builder.minBudget;
		this.maxBudget = builder.maxBudget;
		this.nIterations = builder.nIterations;
		this.executionPath = builder.executionPath;
	}

	public static class Builder {
		private PCSBasedOptimizerInput input;
		private IObjectEvaluator<ComponentInstance, Double> evaluator;
		private Double minBudget;
		private Double maxBudget;
		private Integer nIterations;
		private String executionPath;

		public Builder(final PCSBasedOptimizerInput input, final IObjectEvaluator<ComponentInstance, Double> evaluator) {
			this.input = input;
			this.evaluator = evaluator;
		}

		/**
		 * executionPath is the folder that BOHBOptimizer script runs, pcs files will
		 * also be created here
		 *
		 * @param executionPath
		 * @return
		 */
		public Builder executionPath(final String executionPath) {
			this.executionPath = executionPath;
			return this;
		}

		/**
		 * The smallest budget to consider. Needs to be positive!
		 *
		 * @param minBudget
		 * @return
		 */
		public Builder minBudget(final Double minBudget) {
			this.minBudget = minBudget;
			return this;
		}

		/**
		 * The largest budget to consider. Needs to be larger than min_budget!
		 *
		 * @param maxBudget
		 * @return
		 */
		public Builder maxBudget(final Double maxBudget) {
			this.maxBudget = maxBudget;
			return this;
		}

		/**
		 * Number of iterations
		 *
		 * @param nIterations
		 * @return
		 */
		public Builder nIterations(final Integer nIterations) {
			this.nIterations = nIterations;
			return this;
		}

		public HyperBandOptimizer build() {
			return new HyperBandOptimizer(this);
		}

	}

	@Override
	public void optimize(final String componentName) throws OptimizationException {
		if (StringUtils.isEmpty(this.executionPath)) {
			throw new OptimizationException("executionPath must be set for Optimizer");
		}
		String filePath = this.executionPath;

		// start server thread
		this.startGrpcServer();

		String[] arr = componentName.split("\\.");
		String name = arr[arr.length - 1];

		try {
			this.setPcsFileForComponent(name, filePath);
		} catch (IOException e1) {
			this.logger.error(e1.getMessage());
			throw new OptimizationException(MessageFormat.format("Unable to set PCS file with path={0} for Component={1}", filePath, name));
		}

		String command = this.setOptions();

		// start HpBandSter
		this.startHyperBandScript(filePath, command);
	}

	/**
	 * Start the python script in its execution directory
	 *
	 * @param filePath
	 * @param command
	 * @throws OptimizationException
	 */
	private void startHyperBandScript(final String filePath, final String command) throws OptimizationException {
		PCSBasedOptimizerConfig config;
		try {
			config = PCSBasedOptimizerConfig.get("conf/smac-optimizer-config.properties");
		} catch (IOException e1) {
			throw new OptimizationException(e1);
		}
		Integer port = config.getPort();
		ScenarioFileUtil.updateParam(filePath, "gRPC_port", String.valueOf(port));

		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd " + filePath + "&& " + command);
		builder.redirectErrorStream(true);
		Process p = null;
		try {
			p = builder.start();
		} catch (IOException e) {
			this.logger.error(e.getMessage());
			throw new OptimizationException(MessageFormat.format("Unable spawn python process={0} in path={1} ", command, filePath));
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line = null;
		List<String> hpbandOutLines = new ArrayList<>();
		while (true) {
			try {
				line = r.readLine();
			} catch (IOException e) {
				this.logger.error(e.getMessage());
			}
			hpbandOutLines.add(line);
			if (line == null) {
				break;
			}
		}
		try {
			FileUtil.writeFileAsList(hpbandOutLines, "testrsc/hpband.log");
		} catch (IOException e) {
			throw new OptimizationException(e);
		}
	}

	/**
	 * options are appended to the command
	 *
	 * @return
	 */
	public String setOptions() {
		StringBuilder command = new StringBuilder("python HpBandSterOptimizer.py");
		if (this.minBudget != null) {
			command.append(" --min_budget ").append(this.minBudget);
		}
		if (this.maxBudget != null) {
			command.append(" --max_budget ").append(this.maxBudget);
		}
		if (this.nIterations != null) {
			command.append(" --n_iterations ").append(this.nIterations);
		}
		return command.toString();
	}

}
