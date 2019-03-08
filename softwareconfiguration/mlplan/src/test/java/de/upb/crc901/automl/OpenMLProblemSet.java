package de.upb.crc901.automl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import jaicore.ml.openml.OpenMLHelper;
import weka.core.Instances;

public class OpenMLProblemSet extends MLProblemSet {

	private static final Integer[] ids = {3};
	private static final String API_KEY = "4350e421cdc16404033ef1812ea38c01";
	private static final List<Instances> INSTANCES = new ArrayList<>();
	private static final Logger logger = LoggerFactory.getLogger(OpenMLProblemSet.class);

	static {
		OpenMLHelper.setApiKey(API_KEY);
		for (int id : ids) {
			try {
				INSTANCES.add(OpenMLHelper.getInstancesById(id));
			} catch (IOException e) {
				logger.error("Received exception {}", org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			}
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
