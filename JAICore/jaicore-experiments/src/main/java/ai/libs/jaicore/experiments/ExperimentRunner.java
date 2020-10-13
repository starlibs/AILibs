package ai.libs.jaicore.experiments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyStartedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentFailurePredictionException;
import ai.libs.jaicore.experiments.exceptions.ExperimentUpdateFailedException;
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

	private boolean checkMemory = true;

	private final IExperimentSetConfig config;
	private final IExperimentSetEvaluator evaluator;
	private final IExperimentDatabaseHandle handle;
	private final int availableMemoryInMB;
	private final String executorInfo;

	private final Runtime runtime = Runtime.getRuntime();

	private boolean allExperimentsFinished = false;

	public ExperimentRunner(final IExperimentSetConfig config, final IExperimentSetEvaluator evaluator, final IExperimentDatabaseHandle databaseHandle) throws ExperimentDBInteractionFailedException {
		this(config, evaluator, databaseHandle, null);
	}

	public ExperimentRunner(final IExperimentSetConfig config, final IExperimentSetEvaluator evaluator, final IExperimentDatabaseHandle databaseHandle, final String executorInfo) throws ExperimentDBInteractionFailedException {

		if (databaseHandle == null) {
			throw new IllegalArgumentException("Cannot create ExperimentRunner without database handle!");
		}

		/* check data base configuration */
		this.config = config;
		this.evaluator = evaluator;
		this.handle = databaseHandle;
		this.logger.debug("Created ExperimentRunner. Now updating its configuration from the database.");
		this.logger.info("Successfully created and initialized ExperimentRunner.");
		this.handle.setup(config);
		this.availableMemoryInMB = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
		this.executorInfo = executorInfo;
	}

	public void setCheckMemory(final boolean checkMemory) {
		this.checkMemory = checkMemory;
	}

	/**
	 * Conducts a limited number of not yet conducted experiments randomly chosen from the grid.
	 *
	 * @param maxNumberOfExperiments
	 *            Limit for the number of experiments
	 * @throws ExperimentDBInteractionFailedException
	 * @throws InterruptedException
	 */
	public void randomlyConductExperiments(final int maxNumberOfExperiments) throws ExperimentDBInteractionFailedException, InterruptedException {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Starting to run up to {} experiments.", maxNumberOfExperiments);
		}

		int numberOfConductedExperiments = 0;
		while ((maxNumberOfExperiments <= 0 || numberOfConductedExperiments < maxNumberOfExperiments)) {
			List<ExperimentDBEntry> openRandomExperiments = this.handle.getRandomOpenExperiments(maxNumberOfExperiments);
			if (openRandomExperiments.isEmpty()) {
				this.logger.info("No more open experiments found.");
				this.allExperimentsFinished = true;
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
			this.logger.info("Conduct experiment #{} with key values: {}. Memory statistics: {}MB allocated, {}MB free.", numberOfConductedExperiments + 1, exp.getExperiment().getValuesOfKeyFields(), this.runtime.totalMemory() / (1024 * 1024), this.runtime.freeMemory() / (1024 * 1024));
			Thread expThread = new Thread(() -> {
				try {
					this.handle.startExperiment(exp, this.executorInfo);
					this.conductExperiment(exp);
				} catch (InterruptedException e) {
					this.logger.info("Experiment interrupted.");
					Thread.currentThread().interrupt(); // interrupt myself to make Sonar happy
				} catch (ExperimentDBInteractionFailedException | ExperimentAlreadyStartedException e) {
					this.logger.error(LoggerUtil.getExceptionInfo(e));
				}
			}, "Thread of experiment id " + exp.getId());
			expThread.start();
			expThread.join();
			numberOfConductedExperiments++;
			this.logger.info("Finished experiment #{} with key values {}. Memory statistics: {}MB allocated, {}MB free. Now running GC.", numberOfConductedExperiments, exp.getExperiment().getValuesOfKeyFields(), this.runtime.totalMemory() / (1024 * 1024), this.runtime.freeMemory() / (1024 * 1024));
			System.gc(); // deliberately run the garbage collection to avoid memory accumulation. We found that the JVM is sometimes not able to reasonably clean up later! This is not a guarantee, but better than
			this.logger.info("GC finished. Memory statistics: {}MB allocated, {}MB free.", this.runtime.totalMemory() / (1024 * 1024), this.runtime.freeMemory() / (1024 * 1024));
		}

		this.logger.info("Successfully finished {} experiments.", numberOfConductedExperiments);
	}

	public void sequentiallyConductExperiments(final int maxNumberOfExperiments) throws ExperimentDBInteractionFailedException, InterruptedException {
		this.logger.info("Starting to run up to {} experiments.", maxNumberOfExperiments);

		int numberOfConductedExperiments = 0;
		while ((maxNumberOfExperiments <= 0 || numberOfConductedExperiments < maxNumberOfExperiments)) {
			Optional<ExperimentDBEntry> nextExperiment = this.handle.startNextExperiment(this.executorInfo);
			if (!nextExperiment.isPresent()) {
				this.logger.info("After running {}/{} experiments, no more un-started experiments were found.", numberOfConductedExperiments, maxNumberOfExperiments);
				this.allExperimentsFinished = true;
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
			ExperimentDBEntry exp = nextExperiment.get();
			this.checkExperimentValidity(exp.getExperiment());
			this.logger.info("Conduct experiment #{} with key values: {}", numberOfConductedExperiments + 1, exp.getExperiment().getValuesOfKeyFields());
			Thread expThread = new Thread(() -> {
				try {
					this.conductExperiment(exp);
				} catch (InterruptedException e) {
					this.logger.info("Experiment interrupted.");
					Thread.currentThread().interrupt(); // interrupt myself to make Sonar happy
				} catch (ExperimentDBInteractionFailedException e) {
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
	 * @throws ExperimentDBInteractionFailedException
	 * @throws InterruptedException
	 */
	public void randomlyConductExperiments() throws ExperimentDBInteractionFailedException, InterruptedException {
		this.randomlyConductExperiments(-1);
	}

	/**
	 * Conducts an unbound number of experiments from the grid.
	 *
	 * @throws ExperimentDBInteractionFailedException
	 * @throws InterruptedException
	 */
	public void sequentiallyConductExperiments() throws ExperimentDBInteractionFailedException, InterruptedException {
		this.sequentiallyConductExperiments(-1);
	}

	/**
	 * Conducts a single experiment The experiment is expected to be marked as started already.
	 *
	 * @param expEntry
	 *            the experiment to be conducted
	 * @throws ExperimentDBInteractionFailedException
	 * @throws ExperimentAlreadyStartedException
	 * @throws InterruptedException
	 * @throws Exception
	 *             These are not the exceptions thrown by the experiment itself, because these are logged into the database. Exceptions thrown here are technical exceptions that occur when arranging the experiment
	 */
	protected void conductExperiment(final ExperimentDBEntry expEntry) throws ExperimentDBInteractionFailedException, InterruptedException {
		/* run experiment */
		if (expEntry == null) {
			throw new IllegalArgumentException("Cannot conduct NULL experiment!");
		}
		assert this.handle.hasExperimentStarted(expEntry);
		Throwable error = null;
		try {
			if (this.checkMemory) {
				double memoryDeviation = Math.abs(expEntry.getExperiment().getMemoryInMB() - this.availableMemoryInMB) * 1f / expEntry.getExperiment().getMemoryInMB();
				if (memoryDeviation > MAX_MEM_DEVIATION) {
					this.logger.error("Cannot conduct experiment {}, because the available memory is {} where declared is {}. Deviation: {}", expEntry.getExperiment(), this.availableMemoryInMB, expEntry.getExperiment().getMemoryInMB(),
							memoryDeviation);
					return;
				}
			}
			if (expEntry.getExperiment().getNumCPUs() > Runtime.getRuntime().availableProcessors()) {
				this.logger.error("Cannot conduct experiment {}, because only {} CPU cores are available where declared is {}", expEntry.getExperiment(), Runtime.getRuntime().availableProcessors(), expEntry.getExperiment().getNumCPUs());
				return;
			}
			this.evaluator.evaluate(expEntry, m -> {
				try {
					this.logger.info("Updating experiment with id {} in the following entries (enable DEBUG for values): {}", expEntry.getId(), m.keySet());
					this.logger.debug("Update map is: {}", m);
					this.handle.updateExperiment(expEntry, m);
				} catch (ExperimentUpdateFailedException e) {
					this.logger.error("Error in updating experiment data. Message of {}: {}.\nStack trace:\n {}", e.getClass().getName(), e.getMessage(), LoggerUtil.getExceptionInfo(e));
				}
			});

		} catch (ExperimentEvaluationFailedException e) {
			error = e.getCause();
		} catch (ExperimentFailurePredictionException | RuntimeException e) {
			error = e;
		}
		if (error != null) {
			this.logger.error("Experiment failed due to {}. Message: {}. Detail info: {}", error.getClass().getName(), error.getMessage(), LoggerUtil.getExceptionInfo(error));
		}
		this.handle.finishExperiment(expEntry, error);
	}

	private void checkExperimentValidity(final Experiment experiment) {
		ExperimentSetAnalyzer analyzer = new ExperimentSetAnalyzer(this.config);
		List<String> keyFields = this.config.getKeyFields().stream().map(k -> analyzer.getNameTypeSplitForAttribute(k).getX()).collect(Collectors.toList());
		if (SetUtil.differenceNotEmpty(keyFields, experiment.getValuesOfKeyFields().keySet())) {
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

	public boolean mightHaveMoreExperiments() {
		return !this.allExperimentsFinished;
	}
}
