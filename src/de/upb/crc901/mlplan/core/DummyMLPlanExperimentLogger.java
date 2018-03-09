package de.upb.crc901.mlplan.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import jaicore.ml.experiments.Experiment;
import jaicore.ml.experiments.IMultiClassClassificationExperimentDatabase;
import weka.classifiers.Classifier;

@SuppressWarnings("serial")
public class DummyMLPlanExperimentLogger implements IMultiClassClassificationExperimentDatabase {

	@Override
	public Collection<Experiment> getExperimentsForWhichARunExists() throws Exception {
		return new ArrayList<>();
	}

	@Override
	public int createRunIfDoesNotExist(Experiment experiment) throws Exception {
		return 0;
	}

	@Override
	public void updateExperiment(Experiment experiment, Map<String, String> data) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void associatedRunWithClassifier(int runId, Classifier c) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addResultEntry(int runId, double score) throws Exception {
		// TODO Auto-generated method stub
		
	}
}