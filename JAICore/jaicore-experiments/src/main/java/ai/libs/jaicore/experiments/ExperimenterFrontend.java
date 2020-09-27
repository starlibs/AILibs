package ai.libs.jaicore.experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.db.sql.IRestDatabaseConfig;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterRestSQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentFailurePredictionException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

public class ExperimenterFrontend implements ILoggingCustomizable {

	private static final String MSG_NO_MORE_EXPERIMENTS = "No more experiments to conduct.";
	private static final String MSG_NOTABLE = "No table set in the database configuration!";

	private IExperimentSetConfig config;
	private IExperimentDatabaseHandle databaseHandle;
	private IExperimentSetEvaluator evaluator;
	private ExperimentDomain<?, ?, ?> domain;
	private IExperimentRunController<?> controller;
	private String loggerNameForAlgorithm;
	private ExperimentRunner runner;
	private String executorInfo; // information about the job of the compute center executing this in order to ease tracking
	private Timeout timeout;
	private Function<Experiment, Timeout> experimentSpecificTimeout;
	private final List<Consumer<IAlgorithm<?, ?>>> preRunHooks = new ArrayList<>();

	private Logger logger = LoggerFactory.getLogger("expfe");

	public ExperimenterFrontend withLoggerNameForAlgorithm(final String loggerName) {
		this.loggerNameForAlgorithm = loggerName;
		return this;
	}

	public ExperimenterFrontend withDatabaseConfig(final String databaseConfigFileName) {
		return this.withDatabaseConfig(new File(databaseConfigFileName));
	}

	public ExperimenterFrontend withDatabaseConfig(final File... databaseConfigFiles) {
		return this.withDatabaseConfig((IDatabaseConfig) ConfigFactory.create(IDatabaseConfig.class).loadPropertiesFromFileArray(databaseConfigFiles));
	}

	public ExperimenterFrontend withRestDatabaseConfig(final File... databaseConfigFiles) {
		return this.withDatabaseConfig((IRestDatabaseConfig) ConfigFactory.create(IRestDatabaseConfig.class).loadPropertiesFromFileArray(databaseConfigFiles));
	}

	public ExperimenterFrontend withDatabaseConfig(final IRestDatabaseConfig databaseConfig) {
		this.databaseHandle = new ExperimenterRestSQLHandle(databaseConfig);
		if (databaseConfig.getTable() == null) {
			throw new IllegalArgumentException(MSG_NOTABLE);
		}
		return this;
	}

	public ExperimenterFrontend withDatabaseConfig(final IDatabaseConfig databaseConfig) {
		this.databaseHandle = new ExperimenterMySQLHandle(databaseConfig);
		if (databaseConfig.getDBTableName() == null) {
			throw new IllegalArgumentException(MSG_NOTABLE);
		}
		return this;
	}

	public ExperimenterFrontend withExperimentsConfig(final File configFile) {
		return this.withExperimentsConfig((IExperimentSetConfig) ConfigFactory.create(IExperimentSetConfig.class).loadPropertiesFromFile(configFile));
	}

	public ExperimenterFrontend withExperimentsConfig(final IExperimentSetConfig config) {
		this.config = config;
		return this;
	}

	public ExperimenterFrontend withTimeout(final Timeout to) {
		this.timeout = to;
		return this;
	}

	public ExperimenterFrontend withExperimentSpecificTimeout(final Function<Experiment, Timeout> timeoutFunction) {
		this.experimentSpecificTimeout = timeoutFunction;
		return this;
	}

	public ExperimenterFrontend clearDatabase() throws ExperimentDBInteractionFailedException {
		this.databaseHandle.deleteDatabase();
		return this;
	}

	public ExperimenterFrontend withEvaluator(final IExperimentSetEvaluator evaluator) {
		this.evaluator = evaluator;
		return this;
	}

	public ExperimenterFrontend withExecutorInfo(final String executorInfo) {
		this.executorInfo = executorInfo;
		return this;
	}

	public String getExecutorInfo() {
		return this.executorInfo;
	}

	public <B extends IExperimentBuilder, I, A extends IAlgorithm<? extends I, ?>> ExperimenterFrontend withDomain(final ExperimentDomain<B, I, A> domain) {
		this.evaluator = null;
		this.withExperimentsConfig(domain.getConfig());
		this.domain = domain;
		return this;
	}

	public ExperimenterFrontend withController(final IExperimentRunController<?> controller) {
		this.controller = controller;
		return this;
	}

	public ExperimenterFrontend synchronizeDatabase()
			throws ExperimentDBInteractionFailedException, AlgorithmTimeoutedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, InterruptedException, AlgorithmExecutionCanceledException {
		ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(this.config, this.databaseHandle);
		preparer.setLoggerName(this.getLoggerName() + ".preparer");
		preparer.synchronizeExperiments();
		return this;
	}

	private void prepareEvaluator() {
		if (this.evaluator != null) {
			throw new IllegalStateException("An evaluator has already been set manually. Preparing a domain specific one afterwards and overriding the manually set is not allowed!");
		}
		if (this.controller == null) {
			throw new IllegalStateException("Cannot prepare evaluator, because no experiment controller has been set!");
		}
		if (this.domain != null) {
			this.evaluator = new AlgorithmBenchmarker(this.domain.getDecoder(), this.controller);
			((AlgorithmBenchmarker) this.evaluator).setLoggerName(this.loggerNameForAlgorithm != null ? this.loggerNameForAlgorithm : (this.getLoggerName() + ".evaluator"));
			if (this.timeout != null) {
				((AlgorithmBenchmarker) this.evaluator).setTimeout(this.timeout);
			}
			if (this.experimentSpecificTimeout != null) {
				((AlgorithmBenchmarker) this.evaluator).setExperimentSpecificTimeout(this.experimentSpecificTimeout);
			}
		}
		this.preRunHooks.forEach(((AlgorithmBenchmarker)this.evaluator)::addPreRunHook);
	}

	private ExperimentRunner getExperimentRunner() throws ExperimentDBInteractionFailedException {
		if (this.runner == null) {
			if (this.config == null) {
				throw new IllegalStateException("Cannot conduct experiments. No experiment config has been set, yet.");
			}
			if (this.databaseHandle == null) {
				throw new IllegalStateException("Cannot conduct experiments. No database handle has been set, yet.");
			}
			if (this.evaluator == null) {
				this.prepareEvaluator();
			}
			this.runner = new ExperimentRunner(this.config, this.evaluator, this.databaseHandle, this.executorInfo);
			this.runner.setLoggerName(this.getLoggerName() + ".runner");
		}
		return this.runner;
	}

	public void conductExperiment(final int id) throws ExperimentDBInteractionFailedException, InterruptedException {
		this.getExperimentRunner().conductExperiment(this.databaseHandle.getExperimentWithId(id));
	}

	public void randomlyConductExperiments() throws ExperimentDBInteractionFailedException, InterruptedException {
		if (!this.getExperimentRunner().mightHaveMoreExperiments()) {
			throw new IllegalStateException(MSG_NO_MORE_EXPERIMENTS);
		}
		this.getExperimentRunner().randomlyConductExperiments();
	}

	public void sequentiallyConductExperiments() throws ExperimentDBInteractionFailedException, InterruptedException {
		if (!this.getExperimentRunner().mightHaveMoreExperiments()) {
			throw new IllegalStateException(MSG_NO_MORE_EXPERIMENTS);
		}
		this.getExperimentRunner().sequentiallyConductExperiments();
	}

	public ExperimenterFrontend randomlyConductExperiments(final int limit) throws ExperimentDBInteractionFailedException, InterruptedException {
		if (!this.getExperimentRunner().mightHaveMoreExperiments()) {
			throw new IllegalStateException(MSG_NO_MORE_EXPERIMENTS);
		}
		this.getExperimentRunner().randomlyConductExperiments(limit);
		return this;
	}

	public ExperimenterFrontend randomlyConductExperiments(final Timeout to, final int maxExperimentRuntime) throws ExperimentDBInteractionFailedException, InterruptedException {
		long deadline = System.currentTimeMillis() + to.milliseconds();
		while (System.currentTimeMillis() + maxExperimentRuntime < deadline) {
			this.logger.info("We have time for more experiments. Conducting next.");
			this.randomlyConductExperiments(1);
			this.logger.info("Finished experiment.");
		}
		return this;
	}

	public boolean mightHaveMoreExperiments() throws ExperimentDBInteractionFailedException {
		return this.getExperimentRunner().mightHaveMoreExperiments();
	}

	public <O> O simulateExperiment(final Experiment experiment, final IExperimentRunController<O> controller) throws ExperimentEvaluationFailedException, InterruptedException, ExperimentFailurePredictionException {
		Objects.requireNonNull(controller, "A controller must be provided to simulate an experiment.");
		this.withController(controller);
		this.prepareEvaluator();
		ExperimentDBEntry experimentEntry = new ExperimentDBEntry(-1, experiment);
		Map<String, Object> results = new HashMap<>();
		this.evaluator.evaluate(experimentEntry, results::putAll);
		Experiment expCopy = new Experiment(experiment);
		expCopy.setValuesOfResultFields(results);
		return controller.getExperimentEncoding(expCopy);
	}

	public void simulateExperiment(final int id, final IExperimentRunController<?> controller) throws ExperimentEvaluationFailedException, InterruptedException, ExperimentFailurePredictionException, ExperimentDBInteractionFailedException {
		Objects.requireNonNull(this.databaseHandle, "Not database handle has been set.");
		this.databaseHandle.setup(this.config);
		this.simulateExperiment(this.databaseHandle.getExperimentWithId(id).getExperiment(), controller);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	public void addPreRunHook(final Consumer<IAlgorithm<?, ?>> hook) {
		this.preRunHooks.add(hook);
	}
}
