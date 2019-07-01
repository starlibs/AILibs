package ai.libs.jaicore.experiments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ILoggingCustomizable;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyStartedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentUpdateFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

/**
 * This class is used to run experiments.
 *
 * @author fmohr
 *
 */
public class ExperimentRunner implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(ExperimentRunner.class);
	private static final double MAX_MEM_DEVIATION = .15;

	private final IExperimentSetConfig config;
	private final IExperimentSetEvaluator conductor;
	private final IExperimentDatabaseHandle handle;
	private final int availableMemoryInMB;

	public ExperimentRunner(final IExperimentSetConfig config, final IExperimentSetEvaluator conductor, final IExperimentDatabaseHandle databaseHandle) throws ExperimentDBInteractionFailedException {

		/* check data base configuration */
		this.config = config;
		this.conductor = conductor;
		this.handle = databaseHandle;
		this.logger.debug("Created ExperimentRunner. Now updating its configuration from the database.");
		this.logger.info("Successfully created and initialized ExperimentRunner.");
		this.handle.setup(config);
		this.availableMemoryInMB = (int)(Runtime.getRuntime().maxMemory() / 1024 / 1024);
	}

	/**
	 * Conducts a limited number of not yet conducted experiments randomly chosen
	 * from the grid.
	 *
	 * @param maxNumberOfExperiments
	 *            Limit for the number of experiments
	 * @param reload
	 *            Whether or not the experiment setup should be reloaded between two
	 *            experiment runs.
	 * @throws ExperimentDBInteractionFailedException
	 * @throws IllegalExperimentSetupException
	 */
	public void randomlyConductExperiments(final int maxNumberOfExperiments) throws ExperimentDBInteractionFailedException {
		this.logger.info("Starting to run up to {} experiments.", maxNumberOfExperiments);

		int numberOfConductedExperiments = 0;
		while (!Thread.interrupted() && (maxNumberOfExperiments <= 0 || numberOfConductedExperiments < maxNumberOfExperiments)) {
			List<ExperimentDBEntry> openRandomExperiments = this.handle.getRandomOpenExperiments(1);
			if (openRandomExperiments.isEmpty()) {
				this.logger.info("No more open experiments found.");
				break;
			}
			ExperimentDBEntry exp = openRandomExperiments.get(0);
			this.checkExperimentValidity(exp.getExperiment());
			this.logger.info("Conduct experiment with key values: {}", exp.getExperiment().getValuesOfKeyFields());
			try {
				this.conductExperiment(exp);
				numberOfConductedExperiments++;
			} catch (ExperimentAlreadyStartedException e) {
				this.logger.warn("Experiment was conducted in the meanwhile.");
			}
		}
		this.logger.info("Successfully finished {} experiments.", numberOfConductedExperiments);
	}

	/**
	 * Conducts an unbound number of randomly chosen experiments from the grid.
	 *
	 * @param reload
	 *            Whether or not the experiment setup should be reloaded between two
	 *            experiment runs.
	 * @throws IllegalExperimentSetupException
	 * @throws ExperimentDBInteractionFailedException
	 */
	public void randomlyConductExperiments() throws ExperimentDBInteractionFailedException {
		this.randomlyConductExperiments(-1);
	}

	/**
	 * Conducts a single experiment
	 *
	 * @param exp
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 * @throws ExperimentDBInteractionFailedException
	 * @throws ExperimentAlreadyStartedException
	 * @throws Exception.
	 *             These are not the exceptions thrown by the experiment itself,
	 *             because these are logged into the database. Exceptions thrown
	 *             here are technical exceptions that occur when arranging the
	 *             experiment
	 */
	public void conductExperiment(final ExperimentDBEntry expEntry) throws ExperimentDBInteractionFailedException, ExperimentAlreadyStartedException {

		/* run experiment */
		if (expEntry == null) {
			throw new IllegalArgumentException("Cannot conduct NULL experiment!");
		}
		Throwable error = null;
		try {
			double memoryDeviation = Math.abs(expEntry.getExperiment().getMemoryInMB() - this.availableMemoryInMB) * 1f / expEntry.getExperiment().getMemoryInMB();
			if (memoryDeviation > MAX_MEM_DEVIATION) {
				throw new IllegalStateException("Cannot conduct experiment " + expEntry.getExperiment() + ", because the available memory is " + this.availableMemoryInMB + " where declared is " + expEntry.getExperiment().getMemoryInMB() + ". Deviation: " + memoryDeviation);
			}
			if (expEntry.getExperiment().getNumCPUs() > Runtime.getRuntime().availableProcessors()) {
				throw new IllegalStateException("Cannot conduct experiment " + expEntry.getExperiment() + ", because only " + Runtime.getRuntime() + " CPU cores are available where declared is " + expEntry.getExperiment().getNumCPUs());
			}
			this.handle.startExperiment(expEntry);
			this.conductor.evaluate(expEntry, m -> {
				try {
					this.handle.updateExperiment(expEntry, m);
				} catch (ExperimentUpdateFailedException e) {
					this.logger.error("Error in updating experiment data. Message of {}: {}", e.getClass().getName(), e.getMessage());
				}
			});

		} catch (ExperimentEvaluationFailedException e) {
			error = e.getCause();
			this.logger.error("Experiment failed due to {}. Message: {}. Stack trace: {}", error.getClass().getName(), error.getMessage(), Arrays.asList(error.getStackTrace()).stream().map(s -> "\n\t" + s).collect(Collectors.toList()));
		}
		this.handle.finishExperiment(expEntry, error);
	}

	private void checkExperimentValidity(final Experiment experiment) {
		if (SetUtil.differenceNotEmpty(this.config.getKeyFields(), experiment.getValuesOfKeyFields().keySet())) {
			throw new IllegalArgumentException("The experiment " + experiment + " is invalid, because key fields have not been defined: " + SetUtil.difference(this.config.getKeyFields(), experiment.getValuesOfKeyFields().keySet()));
		}
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
}
