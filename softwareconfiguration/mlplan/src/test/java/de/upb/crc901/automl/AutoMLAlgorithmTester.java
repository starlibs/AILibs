package de.upb.crc901.automl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithm;
import weka.classifiers.Classifier;
import weka.core.Instances;

public abstract class AutoMLAlgorithmTester extends GeneralAlgorithmTester{

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();
		problemSets.add(new OpenMLProblemSet());
		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}
	
	@Override
	public IAlgorithm<?, ?> getAlgorithm(Object problem) {
		return getAutoMLAlgorithm((Instances) problem);
	}
	
	public abstract IAlgorithm<Instances, Classifier> getAutoMLAlgorithm(Instances data);
}
