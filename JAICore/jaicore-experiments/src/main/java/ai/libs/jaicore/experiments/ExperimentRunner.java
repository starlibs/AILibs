package ai.libs.jaicore.experiments;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	private static final int MAX_MEM_DEVIATION = 50;

	private final IExperimentSetConfig config;
	private final IExperimentSetEvaluator conductor;
	private final IExperimentDatabaseHandle handle;
	private final Collection<Map<String, String>> keysForWhichResultsAreKnown = new HashSet<>();

	private int totalNumberOfExperiments;


	public ExperimentRunner(final IExperimentSetConfig config, final IExperimentSetEvaluator conductor, final IExperimentDatabaseHandle databaseHandle) throws ExperimentDBInteractionFailedException {

		/* check data base configuration */
		this.config = config;
		this.conductor = conductor;
		this.handle = databaseHandle;
		this.logger.debug("Created ExperimentRunner. Now updating its configuration from the database.");
		this.logger.info("Successfully created and initialized ExperimentRunner.");
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
		if (this.totalNumberOfExperiments <= 0) {
			this.logger.info("Number of total experiments is 0");
			return;
		}
		this.logger.info("Now conducting new experiment. {}/{} experiments have already been started or even been completed", this.keysForWhichResultsAreKnown.size(), this.totalNumberOfExperiments);

		int numberOfConductedExperiments = 0;
		while (!Thread.interrupted() && this.keysForWhichResultsAreKnown.size() < this.totalNumberOfExperiments && (maxNumberOfExperiments <= 0 || numberOfConductedExperiments < maxNumberOfExperiments)) {
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
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Finished experiments. Conducted {}/{}. Known experiment entries: {}", numberOfConductedExperiments, this.totalNumberOfExperiments,
					this.keysForWhichResultsAreKnown.stream().map(e -> "\n\t" + e).collect(Collectors.joining()));
		}
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
		this.keysForWhichResultsAreKnown.add(expEntry.getExperiment().getValuesOfKeyFields());
		this.logger.debug("Added experiment with keys {} to set of known experiments. Now contains {} items.", expEntry.getExperiment().getValuesOfKeyFields(), this.keysForWhichResultsAreKnown.size());
		try {
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
