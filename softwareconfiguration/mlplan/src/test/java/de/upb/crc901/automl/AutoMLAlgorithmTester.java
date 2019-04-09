package de.upb.crc901.automl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmCreationException;
import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public abstract class AutoMLAlgorithmTester extends GeneralAlgorithmTester {

	private static final Logger logger = LoggerFactory.getLogger(AutoMLAlgorithmTester.class);

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

	@Test
	public void testThatModelIsTrained() throws Exception {
		MLProblemSet problemset = (MLProblemSet) this.getProblemSet();

		IAlgorithm<Instances, Classifier> algorithm = (IAlgorithm<Instances, Classifier>) this.getAlgorithm(problemset.getDatasetSource()); // AutoML-tools should deliver a classifier
		assert algorithm != null : "The factory method has returned NULL as the algorithm object";
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		}
		algorithm.setTimeout(new TimeOut(30, TimeUnit.SECONDS));

		/* find classifier */
		Instances data = algorithm.getInput();
		logger.info("Checking that {} delivers a model on dataset {}", algorithm.getId(), algorithm.getInput().relationName());
		Classifier c = algorithm.call();
		logger.info("Identified classifier {} as solution to the problem.", WekaUtil.getClassifierDescriptor(c));
		assertNotNull("The algorithm as not returned any classifier.", c);

		/* check that some predictions can be made with the classifier */
		int n = data.size();
		for (int i = 0; i < Math.min(10, n); i++) {
			c.classifyInstance(data.get(i));
		}
	}
}
