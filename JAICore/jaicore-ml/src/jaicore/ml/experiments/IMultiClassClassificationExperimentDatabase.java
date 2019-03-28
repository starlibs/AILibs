package jaicore.ml.experiments;

import java.util.Collection;
import java.util.Map;

import weka.classifiers.Classifier;

public interface IMultiClassClassificationExperimentDatabase {
	
	public Collection<MLExperiment> getExperimentsForWhichARunExists() throws Exception;
	
	public int createRunIfDoesNotExist(MLExperiment experiment) throws Exception;
	
	public void updateExperiment(MLExperiment experiment, Map<String,String> data) throws Exception;
	
	/**
	 * This method tells the logger the classifier object that is used for the run.
	 * Specific loggers may retrieve important information from the classifier. 
	 * 
	 * @param runId
	 * @param c
	 */
	public void associatedRunWithClassifier(int runId, Classifier c) throws Exception;
	
	public void addResultEntry(int runId, double score) throws Exception;
}
