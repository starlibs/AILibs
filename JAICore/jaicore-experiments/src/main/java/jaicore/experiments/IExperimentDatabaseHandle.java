package jaicore.experiments;

import java.util.Collection;
import java.util.Map;

import jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import jaicore.experiments.exceptions.ExperimentUpdateFailedException;

public interface IExperimentDatabaseHandle {

	public void setup(final IExperimentSetConfig config) throws ExperimentDBInteractionFailedException;

	public Collection<ExperimentDBEntry> getConductedExperiments() throws ExperimentDBInteractionFailedException;

	public ExperimentDBEntry createAndGetExperimentIfNotConducted(final Experiment experiment) throws ExperimentDBInteractionFailedException;

	public void updateExperiment(final ExperimentDBEntry exp, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException;

	public void finishExperiment(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException;

	public void finishExperiment(final ExperimentDBEntry exp, final Throwable errror) throws ExperimentDBInteractionFailedException;
}
