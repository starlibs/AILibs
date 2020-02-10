package ai.libs.hyperopt.optimizer.pcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hyperopt.optimizer.IOptimizer;
import ai.libs.hyperopt.port.PCSBasedOptimizerGrpcServer;
import ai.libs.jaicore.basic.FileUtil;

/**
 * Implements {@link Optimizer} interface and contains common methods for
 * PCSBasedOptimizers
 *
 * @author kadirayk
 *
 */
public abstract class APCSBasedOptimizer implements IOptimizer {

	private static final Logger logger = LoggerFactory.getLogger(APCSBasedOptimizer.class);

	/* Optimizer configuration specification */
	private Double minBudget;
	private Double maxBudget;
	private Integer nIterations;
	private String executionPath;
	private Integer numThreads;

	/* Optimization task specification */
	protected Collection<Component> components;
	protected String requestedInterface;
	protected IObjectEvaluator<ComponentInstance, Double> evaluator;

	protected APCSBasedOptimizer(final PCSBasedOptimizerBuilder builder) {
		// optimizer configuration specification
		this.minBudget = builder.getMinBudget();
		this.maxBudget = builder.getMaxBudget();
		this.nIterations = builder.getnIterations();
		this.executionPath = builder.getExecutionPath();
		this.numThreads = builder.getNumThreads();

		// optimization task specification
		this.components = builder.getComponents();
		this.requestedInterface = builder.getRequestedInterface();
		this.evaluator = builder.getEvaluator();
	}

	/**
	 * Starts {@link PCSBasedOptimizerGrpcServer} in a new thread
	 */
	protected void startGrpcServer() {
		Runnable task = () -> {
			try {
				PCSBasedOptimizerGrpcServer.start(this.evaluator, this.input);
			} catch (IOException | InterruptedException e) {
				logger.error(e.getMessage());
			}
		};
		Thread thread = new Thread(task);
		thread.start();
		logger.info("started gRPC server");
	}

	/**
	 * updates the paramfile parameter in the scenario file of the optimizer
	 *
	 * @param componentName
	 * @param filePath      is the execution directory for the optimizer that
	 *                      contains a scenario.txt file
	 * @throws IOException
	 */
	protected void setPcsFileForComponent(final String componentName, final String filePath) throws IOException {
		List<String> lines = null;
		List<String> newLines = new ArrayList<>();
		lines = FileUtil.readFileAsList(filePath + "/scenario.txt");
		for (String line : lines) {
			String newLine = line;
			if (line.startsWith("paramfile")) {
				newLine = "paramfile = " + componentName + ".pcs";
			}
			newLines.add(newLine);
		}
		FileUtil.writeFileAsList(newLines, filePath + "/scenario.txt");
	}

	/**
	 * options are appended to the command
	 * @return
	 */
	public String setOptions() {
		StringBuilder command = new StringBuilder();
		command.append("python ");
		command.append(this.getExecutableScript());
		if (this.minBudget != null) {
			command.append(" --min_budget ").append(this.minBudget);
		}
		if (this.maxBudget != null) {
			command.append(" --max_budget ").append(this.maxBudget);
		}
		if (this.nIterations != null) {
			command.append(" --n_iterations ").append(this.nIterations);
		}
		if (this.numThreads > 1) {
			command.append(" --n_workers ").append(this.numThreads);
		}
		return command.toString();
	}

	public abstract String getExecutableScript();

	public Double getMinBudget() {
		return this.minBudget;
	}

	public Double getMaxBudget() {
		return this.maxBudget;
	}

	public Integer getnIterations() {
		return this.nIterations;
	}

	public String getExecutionPath() {
		return this.executionPath;
	}

	public Integer getNumThreads() {
		return this.numThreads;
	}

	public IObjectEvaluator<ComponentInstance, Double> getEvaluator() {
		return this.evaluator;
	}
}
