package de.upb.crc901.automl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmCreationException;
import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.sets.SetUtil.Pair;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public abstract class AutoMLAlgorithmCoreFunctionalityTester extends GeneralAlgorithmTester {

	private static final Logger logger = LoggerFactory.getLogger(AutoMLAlgorithmCoreFunctionalityTester.class);

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() throws IOException, Exception {
		List<Object> problemSets = new ArrayList<>();
		problemSets.add(new OpenMLProblemSet(3)); // kr-vs-kp
		problemSets.add(new OpenMLProblemSet(1150)); // AP_Breast_Lung
		problemSets.add(new OpenMLProblemSet(1156)); // AP_Omentum_Ovary
		//				problemSets.add(new OpenMLProblemSet(1152)); // AP_Prostate_Ovary
		//				problemSets.add(new OpenMLProblemSet(1240)); // AirlinesCodrnaAdult
		//				problemSets.add(new OpenMLProblemSet(1457)); // amazon
		//				problemSets.add(new OpenMLProblemSet(149)); // CovPokElec
		//				problemSets.add(new OpenMLProblemSet(41103)); // cifar-10
		//				problemSets.add(new OpenMLProblemSet(40668)); // connect-4
		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) throws AlgorithmCreationException {
		Pair<DataSource, String> sourceClassAttributePair = (Pair<DataSource, String>) problem;
		try {
			Instances dataset = sourceClassAttributePair.getX().getDataSet();
			Attribute targetAttribute = dataset.attribute(sourceClassAttributePair.getY());
			dataset.setClassIndex(targetAttribute.index());
			return this.getAutoMLAlgorithm(dataset);
		} catch (Exception e) {
			throw new AlgorithmCreationException(e);
		}
	}

	public abstract IAlgorithm<Instances, Classifier> getAutoMLAlgorithm(Instances data);
}
