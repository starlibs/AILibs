package ai.libs.jaicore.experiments;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
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
	 * Returns a list of all experiments contained in the database
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
	 * Returns a list of all experiments that are currently being conducted.
	 *
	 * @return List of all experiments conducted so far
	 * @throws ExperimentDBInteractionFailedException
	 */
	public List<ExperimentDBEntry> getRunningExperiments() throws ExperimentDBInteractionFailedException;

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
	 * Creates a new experiment entry and returns it.
	 *
	 * @param experiments the experiments to be created
	 * @return The id of the created experiment
	 * @throws ExperimentDBInteractionFailedException
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 */
	public List<ExperimentDBEntry> createAndGetExperiments(final List<Experiment> experiments) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException;

	/**
	 * Updates non-keyfield values of the experiment.
	 *
	 * @param exp The experiment that is started on the current machine
	 * @throws ExperimentUpdateFailedException
	 */
	public void startExperiment(final ExperimentDBEntry exp) throws ExperimentUpdateFailedException;

	/**
	 * Updates non-keyfield values of the experiment.
	 *
	 * @param exp The experiment entry in the database
	 * @param values A key-value store where keys are names of result fields. The values will be associated to each key in the database.
	 * @throws ExperimentUpdateFailedException
	 */
	public void updateExperiment(final ExperimentDBEntry exp, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException;

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
	 * @param exp
	 * @param errror
	 * @throws ExperimentDBInteractionFailedException
	 */
	public void finishExperiment(final ExperimentDBEntry exp, final Throwable errror) throws ExperimentDBInteractionFailedException;

	/**
	 * Deletes an experiment from the database
	 *
	 * @param exp
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
}
