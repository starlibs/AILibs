package ai.libs.hasco.pcsbasedoptimization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.IObjectEvaluator;

/**
 * Implements {@link Optimizer} interface and contains common methods for
 * PCSBasedOptimizers
 * 
 * @author kadirayk
 *
 */
public abstract class AbstractPCSBasedOptimizer implements Optimizer {

	private static final Logger logger = LoggerFactory.getLogger(AbstractPCSBasedOptimizer.class);

	protected PCSBasedOptimizerInput input;
	protected IObjectEvaluator<ComponentInstance, Double> evaluator;

	/**
	 * Starts {@link PCSBasedOptimizerGrpcServer} in a new thread
	 */
	protected void startGrpcServer() {
		Runnable task = () -> {
			try {
				PCSBasedOptimizerGrpcServer.start(evaluator, input);
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
	protected void setPcsFileForComponent(String componentName, String filePath) throws IOException {
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

}
