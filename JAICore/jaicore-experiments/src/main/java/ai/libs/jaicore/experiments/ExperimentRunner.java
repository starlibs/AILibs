package ai.libs.jaicore.experiments;

import java.util.List;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyStartedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentUpdateFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;
import ai.libs.jaicore.logging.LoggerUtil;

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

		if (databaseHandle == null) {
			throw new IllegalArgumentException("Cannot create ExperimentRunner without database handle!");
		}

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
	 * @throws InterruptedException
	 * @throws IllegalExperimentSetupException
	 */
	public void randomlyConductExperiments(final int maxNumberOfExperiments) throws ExperimentDBInteractionFailedException, InterruptedException {
		this.logger.info("Starting to run up to {} experiments.", maxNumberOfExperiments);

		int numberOfConductedExperiments = 0;
		while ((maxNumberOfExperiments <= 0 || numberOfConductedExperiments < maxNumberOfExperiments)) {
			List<ExperimentDBEntry> openRandomExperiments = this.handle.getRandomOpenExperiments(1);
			if (openRandomExperiments.isEmpty()) {
				this.logger.info("No more open experiments found.");
				break;
			}

			/* if we WOULD conduct more experiments but are interrupted, throw an exception */
			if (Thread.interrupted()) {
				this.logger.info("Experimenter Thread is interrupted, throwing InterruptedException.");
				throw new InterruptedException();
			}

			/* get experiment, create experiment thread, run the thread, and wait for its termination
			 * the dedicated thread is created in order to avoid that interrupts on it cause the main thread
			 * to be interrupted. */
			ExperimentDBEntry exp = openRandomExperiments.get(0);
			this.checkExperimentValidity(exp.getExperiment());
			this.logger.info("Conduct experiment #{} with key values: {}", numberOfConductedExperiments + 1, exp.getExperiment().getValuesOfKeyFields());
			Thread expThread = new Thread(() -> {
				try {
					this.conductExperiment(exp);
				} catch (InterruptedException e) {
					this.logger.info("Experiment interrupted.");
					Thread.currentThread().interrupt(); // interrupt myself to make Sonar happy
				}
				catch (ExperimentDBInteractionFailedException | ExperimentAlreadyStartedException e) {
					this.logger.error(LoggerUtil.getExceptionInfo(e));
				}
			});
			expThread.start();
			expThread.join();
			numberOfConductedExperiments++;
			this.logger.info("Finished experiment #{} with key values {}", numberOfConductedExperiments, exp.getExperiment().getValuesOfKeyFields());
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
	 * @throws InterruptedException
	 */
	public void randomlyConductExperiments() throws ExperimentDBInteractionFailedException, InterruptedException {
		this.randomlyConductExperiments(-1);
	}

	/**
	 * Conducts a single experiment
	 *
	 * @param exp
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 * @throws ExperimentDBInteractionFailedException
	 * @throws ExperimentAlreadyStartedException
	 * @throws InterruptedException
	 * @throws Exception.
	 *             These are not the exceptions thrown by the experiment itself,
	 *             because these are logged into the database. Exceptions thrown
	 *             here are technical exceptions that occur when arranging the
	 *             experiment
	 */
	public void conductExperiment(final ExperimentDBEntry expEntry) throws ExperimentDBInteractionFailedException, ExperimentAlreadyStartedException, InterruptedException {

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
				throw new IllegalStateException("Cannot conduct experiment " + expEntry.getExperiment() + ", because only " + Runtime.getRuntime().availableProcessors() + " CPU cores are available where declared is " + expEntry.getExperiment().getNumCPUs());
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
			this.logger.error("Experiment failed due to {}. Message: {}. Detail info: {}", error.getClass().getName(), error.getMessage(), LoggerUtil.getExceptionInfo(error));
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
