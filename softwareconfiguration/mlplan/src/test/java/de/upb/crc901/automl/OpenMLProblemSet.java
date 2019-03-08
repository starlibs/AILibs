package de.upb.crc901.automl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import jaicore.ml.openml.OpenMLHelper;
import weka.core.Instances;

public class OpenMLProblemSet extends MLProblemSet {

	private final static Integer[] ids = {3};
	private final static String apiKey = "4350e421cdc16404033ef1812ea38c01";
	private final static List<Instances> INSTANCES = new ArrayList<>();
	
	static {
		OpenMLHelper.setApiKey(apiKey);
		try {
			for (int id : ids) {
				INSTANCES.add(OpenMLHelper.getInstancesById(id));
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public OpenMLProblemSet() {
		super("openml.org");
	}

	@Override
	public Instances getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		return INSTANCES.get(0);
	}

	@Override
	public Instances getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		return INSTANCES.get(0);
	}

	@Override
	public List<Instances> getProblems() {
		return INSTANCES;
	}
}
