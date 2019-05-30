package jaicore.experiments;

import java.util.Collection;
import java.util.Map;

import jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import jaicore.experiments.exceptions.ExperimentUpdateFailedException;

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
	 * Returns a list of all experiments contained in the database. There is no need for those to be in line with the latest configuration.
	 * 
	 * @return List of all experiments
	 * @throws ExperimentDBInteractionFailedException
	 */
	public Collection<ExperimentDBEntry> getConductedExperiments() throws ExperimentDBInteractionFailedException;

	/**
	 * * Creates a new experiment entry and returns it.
	 * 
	 * @param experiment
	 * @param experiment
	 * @return The id of the created experiment
	 * @throws ExperimentDBInteractionFailedException
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 */
	public ExperimentDBEntry createAndGetExperiment(final Experiment experiment) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException;
	
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
}
