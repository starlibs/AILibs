package ai.libs.jaicore.experiments;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyStartedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentUpdateFailedException;

/**
 * This interface is used by the ExperimentRunner to get, create, and update experiment entries.
 *
 * @author fmohr
 *
 */
public interface IExperimentDatabaseHandle {

	/**
	 * Prepares everything so that upcoming calls for create and update will be managed according to the specified configuration.
	 *
	 * @param config Description of the experiment setup
	 * @throws ExperimentDBInteractionFailedException
	 */
	public void setup(final IExperimentSetConfig config) throws ExperimentDBInteractionFailedException;

	/**
	 *
	 * @param key The key attribute
	 * @throws ExperimentDBInteractionFailedException
	 */
	public Collection<String> getConsideredValuesForKey(final String key) throws ExperimentDBInteractionFailedException;

	/**
	 * Returns a list of all experiments contained in the database
	 *
	 * @return List of all experiments
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getAllExperiments() throws ExperimentDBInteractionFailedException;

	/**
	 * Returns the number of all experiments contained in the database
	 *
	 * @throws ExperimentDBInteractionFailedException
	 */
	public int getNumberOfAllExperiments() throws ExperimentDBInteractionFailedException;

	/**
	 * Returns a list of all experiments contained in the database marked as being conducted.
	 *
	 * @return List of all experiments conducted so far
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getConductedExperiments() throws ExperimentDBInteractionFailedException;

	/**
	 * Returns a list of all experiments contained in the database marked as being conducted and with the attribute values specified as in the map.
	 *
	 * @return List of all experiments conducted so far
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getConductedExperiments(Map<String, Object> fieldFilter) throws ExperimentDBInteractionFailedException;

	/**
	 * Returns a list of all experiments contained in the database marked as being conducted and with an exception.
	 *
	 * @return List of all experiments conducted so far
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getFailedExperiments() throws ExperimentDBInteractionFailedException;

	/**
	 * Returns a list of all experiments contained in the database marked as being conducted and with an exception and with the attribute values specified as in the map.
	 *
	 * @return List of all experiments conducted so far
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getFailedExperiments(Map<String, Object> fieldFilter) throws ExperimentDBInteractionFailedException;

	/**
	 * Returns a list of all experiments contained in the database that have not been started yet.
	 *
	 * @return List of all experiments conducted so far
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getOpenExperiments() throws ExperimentDBInteractionFailedException;

	/**
	 * Returns a list of all experiments contained in the database that have not been started yet.
	 *
	 * @param limit Maximum number of open experiments that should be returned
	 * @return List of all experiments conducted so far
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getRandomOpenExperiments(int limit) throws ExperimentDBInteractionFailedException;

	/**
	 * Picks an unstarted experiment, marks it as started and returns it.
	 * These operations happen atomically, so if a experiment is returned, then ownership on it can be assumed.
	 *
	 * If no experiment is returned, i.e. an empty optional, then no experiment is remaining.
	 *
	 * @return A started experiment if there are any left, or else an empty optional.
	 * @throws ExperimentDBInteractionFailedException
	 */
	public Optional<ExperimentDBEntry> startNextExperiment() throws ExperimentDBInteractionFailedException;

	/**
	 * Returns a list of all experiments that are currently being conducted.
	 *
	 * @return List of all experiments conducted so far
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getRunningExperiments() throws ExperimentDBInteractionFailedException;


	/**
	 * Gets the experiment with the given id.
	 *
	 * @param id
	 * @return
	 * @throws ExperimentDBInteractionFailedException
	 */
	public ExperimentDBEntry getExperimentWithId(int id) throws ExperimentDBInteractionFailedException;

	/**
	 * Creates a new experiment entry and returns it.
	 *
	 * @param experiment
	 * @return The id of the created experiment
	 * @throws ExperimentDBInteractionFailedException
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 */
	public ExperimentDBEntry createAndGetExperiment(final Experiment experiment) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException;

	/**
	 * Creates or fetches the experiment entries from the database.
	 * The "or" is exclusive, i.e. that if any entry exist it won't be created.
	 * In comparison to other createAndGet methods, this doesn't throw a ExperimentAlreadyExistsInDatabaseException.
	 *
	 * @param experiments the experiments to be created
	 * @return The id of the created experiment
	 * @throws ExperimentDBInteractionFailedException
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 */
	public List<ExperimentDBEntry> createOrGetExperiments(final List<Experiment> experiments) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException;

	/**
	 * Updates non-keyfield values of the experiment.
	 *
	 * @param exp The experiment that is started on the current machine
	 * @throws ExperimentUpdateFailedException
	 */
	public void startExperiment(final ExperimentDBEntry exp) throws ExperimentAlreadyStartedException, ExperimentUpdateFailedException;

	/**
	 * Updates non-keyfield values of the experiment.
	 *
	 * @param exp The experiment entry in the database
	 * @param values A key-value store where keys are names of result fields. The values will be associated to each key in the database.
	 * @throws ExperimentUpdateFailedException
	 */
	public void updateExperiment(final ExperimentDBEntry exp, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException;

	public boolean updateExperimentConditionally(final ExperimentDBEntry exp, final Map<String, String> conditions, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException;

	/**
	 * Signals that an experiment has been finished successfully.
	 * A corresponding timestamp will be attached to the experiment entry.
	 *
	 * @param exp
	 * @throws ExperimentDBInteractionFailedException
	 */
	public void finishExperiment(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException;

	/**
	 * Signals that an experiment has failed with an exception.
	 * The timestamp and the exception will be stored with the experiment.
	 *
	 * @param exp Experiment to be marked as finished
	 * @param errror If not null, the experiment
	 * @throws ExperimentDBInteractionFailedException
	 */
	public void finishExperiment(final ExperimentDBEntry exp, final Throwable errror) throws ExperimentDBInteractionFailedException;

	/**
	 * Deletes an experiment from the database
	 *
	 * @param exp Experiment to be deleted
	 * @throws ExperimentDBInteractionFailedException
	 */
	public void deleteExperiment(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException;

	/**
	 * Deletes everything known to the experiment database.
	 * Note that database is understood as an abstract term. In a true database, this could just be a table.
	 *
	 * @throws ExperimentDBInteractionFailedException
	 */
	public void deleteDatabase() throws ExperimentDBInteractionFailedException;


	/**
	 * Checks if the given experiment has been started already.
	 *
	 * @param exp Experiment used for the query.
	 * @return true iff experiment has been marked as started.
	 * @throws ExperimentDBInteractionFailedException
	 */
	public boolean hasExperimentStarted(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException;

}
