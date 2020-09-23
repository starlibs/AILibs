package ai.libs.jaicore.experiments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

public class ExperimentDatabasePreparer implements ILoggingCustomizable {
	private Logger logger = LoggerFactory.getLogger(ExperimentDatabasePreparer.class);

	private final IExperimentSetConfig experimentConfig;
	private final ExperimentSetAnalyzer configAnalyzer;
	private final IExperimentDatabaseHandle handle;
	private final int memoryLimit;
	private final int cpuLimit;

	public ExperimentDatabasePreparer(final IExperimentSetConfig config, final IExperimentDatabaseHandle databaseHandle) {

		if (config.getKeyFields() == null) {
			throw new IllegalArgumentException("Configuration has not defined any key fields. Make sure to specify the " + IExperimentSetConfig.KEYFIELDS + " entry in the config file.");
		}

		/* check data base configuration */
		if (config.getMemoryLimitInMB() == null) {
			throw new IllegalArgumentException("Memory field (" + IExperimentSetConfig.MEM_MAX + ") must be set in configuration");
		}
		if (config.getNumberOfCPUs() == null) {
			throw new IllegalArgumentException("Max CPU field (" + IExperimentSetConfig.CPU_MAX + ") must be set in configuration");
		}
		if (config.getKeyFields() == null) {
			throw new IllegalArgumentException("Key fields (" + IExperimentSetConfig.KEYFIELDS + ") entry must be set in configuration!");
		}
		if (config.getResultFields() == null) {
			throw new IllegalArgumentException("Result fields (" + IExperimentSetConfig.RESULTFIELDS + ") entry must be set in configuration!");
		}

		/* store most relevant information (since accessing the config object is costly) */
		this.memoryLimit = config.getMemoryLimitInMB();
		this.cpuLimit = config.getNumberOfCPUs();

		/* create analyzer */
		this.configAnalyzer = new ExperimentSetAnalyzer(config);

		/* synchronize information with the database */
		this.handle = databaseHandle;
		this.experimentConfig = config;
		this.logger.info("Successfully created and initialized ExperimentDatabasePreparer.");
	}

	/**
	 * Installs not all but only a sub-sample of the defined experiments.
	 * The experiments are sampled based on Latin Hypercube sampling.
	 *
	 * @param numberOfExperiments
	 * @return
	 * @throws AlgorithmExecutionCanceledException
	 * @throws InterruptedException
	 * @throws IllegalExperimentSetupException
	 * @throws AlgorithmTimeoutedException
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> installSubGridOfExperiments(final int numberOfExperiments) throws AlgorithmTimeoutedException, IllegalExperimentSetupException, InterruptedException, AlgorithmExecutionCanceledException, ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException {

		/* setup the table */
		this.logger.info("Creating experiment table if not existent.");
		this.handle.setup(this.experimentConfig);
		this.logger.info("Table ready. Now synchronizing experiments.");

		List<List<String>> tmpPossibleKeyCombinations = this.configAnalyzer.getAllPossibleKeyCombinationsAsList();
		Collection<List<String>> chosenExperiments = SetUtil.getSubGridRelationFromRelation(tmpPossibleKeyCombinations, numberOfExperiments);
		Collection<Map<String, String>> experimentsAsMaps = this.configAnalyzer.mapListTuplesToKeyValueMap(chosenExperiments);
		List<ExperimentDBEntry> entries = this.handle.createOrGetExperiments(experimentsAsMaps.stream().map(t -> new Experiment(this.memoryLimit, this.cpuLimit, t)).collect(Collectors.toList()));
		this.logger.info("Ids of {} inserted entries: {}", entries.size(), entries.stream().map(ExperimentDBEntry::getId).collect(Collectors.toList()));
		return entries;
	}

	/**
	 * Creates all experiments in the database that should exist with respect to the configuration but have not been created yet.
	 *
	 * @return
	 * @throws ExperimentDBInteractionFailedException
	 * @throws IllegalExperimentSetupException
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 * @throws AlgorithmTimeoutedException
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 */
	public List<ExperimentDBEntry> synchronizeExperiments()
			throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {

		/* setup the table */
		this.logger.info("Creating experiment table if not existent.");
		this.handle.setup(this.experimentConfig);
		this.logger.info("Table ready. Now synchronizing experiments.");

		/* get set of all POSSIBLE experiments and all CREATED experiments */
		List<Map<String, String>> tmpPossibleKeyCombinations = new ArrayList<>(this.configAnalyzer.getAllPossibleKeyCombinations());
		this.logger.debug("Determined {} possible combinations. Will now remove keys that are already contained.", tmpPossibleKeyCombinations.size());
		Collection<ExperimentDBEntry> installedExperiments = this.handle.getAllExperiments();
		this.logger.debug("Identified {} installed experiments. Removing these from the list of all possible experiments.", installedExperiments.size());

		/* now determine the experiments that are currently missing and alert if there are created experiments that are not in line with the current config anymore */
		int removed = 0;
		for (ExperimentDBEntry experiment : installedExperiments) {
			if (tmpPossibleKeyCombinations.contains(experiment.getExperiment().getValuesOfKeyFields())) {
				tmpPossibleKeyCombinations.remove(experiment.getExperiment().getValuesOfKeyFields());
				removed++;
			} else {
				this.logger.warn("Experiment with id {} and keys {} seems outdated. The reason can be an illegal key name or an outdated value for one of the keys. Enable DEBUG mode for more details.", experiment.getId(),
						experiment.getExperiment().getValuesOfKeyFields());
			}
		}
		this.logger.debug("{} experiments already exist. Number of experiments that will be created now is {}.", removed, tmpPossibleKeyCombinations.size());

		/* add experiments that have not been created yet */
		if (tmpPossibleKeyCombinations.isEmpty()) {
			return new ArrayList<>(0);
		}
		List<ExperimentDBEntry> entries = this.handle.createOrGetExperiments(tmpPossibleKeyCombinations.stream().map(t -> new Experiment(this.memoryLimit, this.cpuLimit, t)).collect(Collectors.toList()));
		this.logger.info("Ids of {} inserted entries: {}", entries.size(), entries.stream().map(ExperimentDBEntry::getId).collect(Collectors.toList()));
		return entries;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.handle instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.handle).setLoggerName(name + ".handle");
		}
	}

	public ExperimentSetAnalyzer getConfigAnalyzer() {
		return this.configAnalyzer;
	}

	public IExperimentDatabaseHandle getHandle() {
		return this.handle;
	}
}
