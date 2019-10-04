package ai.libs.jaicore.experiments;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.GetPropertyFailedException;
import org.api4.java.common.attributedobjects.IGetter;

import ai.libs.jaicore.basic.IDatabaseConfig;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

public class ExperimenterFrontend {

	private IExperimentSetConfig config;
	private IExperimentDatabaseHandle databaseHandle;
	private IExperimentSetEvaluator evaluator;
	private ExperimentDomain<?, ?, ?, ?> domain;

	public ExperimenterFrontend withDatabaseConfig(final String databaseConfigFileName) {
		return this.withDatabaseConfig(new File(databaseConfigFileName));
	}

	public ExperimenterFrontend withDatabaseConfig(final File databaseConfigFile) {
		return this.withDatabaseConfig((IDatabaseConfig)ConfigFactory.create(IDatabaseConfig.class).loadPropertiesFromFile(databaseConfigFile));
	}

	public ExperimenterFrontend withDatabaseConfig(final IDatabaseConfig databaseConfig) {
		this.databaseHandle = new ExperimenterMySQLHandle(databaseConfig);
		return this;
	}

	public ExperimenterFrontend withExperimentsConfig(final File configFile) {
		return this.withExperimentsConfig((IExperimentSetConfig)ConfigFactory.create(IExperimentSetConfig.class).loadPropertiesFromFile(configFile));
	}

	public ExperimenterFrontend withExperimentsConfig(final IExperimentSetConfig config) {
		this.config = config;
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

	public <B extends IExperimentBuilder, I, A extends IAlgorithm<? extends I,?>, Z> ExperimenterFrontend withAlgorithmExperimentDomain(final ExperimentDomain<B, I, A, Z> domain) {
		this.withEvaluator(new AlgorithmBenchmarker(domain.getDecoder()));
		this.withExperimentsConfig(domain.getConfig());
		this.domain = domain;
		return this;
	}

	public ExperimenterFrontend synchronizeDatabase()
			throws ExperimentDBInteractionFailedException, AlgorithmTimeoutedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, InterruptedException, AlgorithmExecutionCanceledException {
		ExperimentDatabasePreparer preparer = new ExperimentDatabasePreparer(this.config, this.databaseHandle);
		preparer.synchronizeExperiments();
		return this;
	}

	public void randomlyConductExperiments() throws ExperimentDBInteractionFailedException, InterruptedException {
		new ExperimentRunner(this.config, this.evaluator, this.databaseHandle).randomlyConductExperiments();
	}

	public ExperimenterFrontend randomlyConductExperiments(final int limit) throws ExperimentDBInteractionFailedException, InterruptedException {
		new ExperimentRunner(this.config, this.evaluator, this.databaseHandle).randomlyConductExperiments(limit);
		return this;
	}

	public Map<String, Object> simulateExperiment(final Experiment experiment) throws ExperimentEvaluationFailedException, InterruptedException {
		ExperimentDBEntry experimentEntry = new ExperimentDBEntry(-1, experiment);
		Map<String, Object> results = new HashMap<>();
		if (this.domain != null) {
			this.evaluator = new AlgorithmBenchmarker(this.domain.getDecoder(), this.domain.getResultUpdaterComputer().apply(experiment), this.domain.getTerminationCriterionComputer().apply(experiment));
		}
		this.evaluator.evaluate(experimentEntry, r -> results.putAll(r));
		return results;
	}

	public <O> O simulateExperiment(final Experiment experiment, final IGetter<Map<String, Object>, O> resultInterpreter) throws ExperimentEvaluationFailedException, InterruptedException, GetPropertyFailedException {
		return resultInterpreter.getPropertyOf(this.simulateExperiment(experiment));
	}
}
