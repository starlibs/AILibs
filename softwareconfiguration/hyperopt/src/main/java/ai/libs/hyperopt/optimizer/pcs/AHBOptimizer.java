package ai.libs.hyperopt.optimizer.pcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hyperopt.optimizer.OptimizationException;
import ai.libs.hyperopt.util.ScenarioFileUtil;
import ai.libs.jaicore.basic.FileUtil;

public abstract class AHBOptimizer extends APCSBasedOptimizer {

	protected AHBOptimizer(final PCSBasedOptimizerBuilder builder) {
		super(builder);
	}

	private Logger logger = LoggerFactory.getLogger(AHBOptimizer.class);

	@Override
	public void optimize() throws OptimizationException {
		if (StringUtils.isEmpty(this.executionPath)) {
			throw new OptimizationException("executionPath must be set for Optimizer");
		}
		String filePath = this.getExecutionPath();

		// start server thread
		this.startGrpcServer();

		String[] arr = this.input.getRequestedComponent().split("\\.");
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
	public void startHyperBandScript(final String filePath, final String command) throws OptimizationException {
		PCSBasedOptimizerConfig config = PCSBasedOptimizerConfig.get("conf/smac-optimizer-config.properties");
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
			System.out.println("HpBandSter out: " + line);
			if (line == null) {
				break;
			}
		}
		try {
			FileUtil.writeFileAsList(hpbandOutLines, "testrsc/hpband.log");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
