package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import timeseriesweka.classifiers.ShapeletTransformClassifier;

public class ShapeletTransformRefTest {
	private static final String CAR_TRAIN = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\Car\\Car_TRAIN.arff";
	private static final String CAR_TEST = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\Car\\Car_TEST.arff";

	private static final String ARROW_HEAD_TRAIN = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\ArrowHead\\ArrowHead\\ArrowHead_TRAIN.arff";
	private static final String ARROW_HEAD_TEST = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\ArrowHead\\ArrowHead\\ArrowHead_TEST.arff";

	private static final String ITALY_POWER_DEMAND_TRAIN = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\ItalyPowerDemand\\ItalyPowerDemand_TRAIN.arff";
	private static final String ITALY_POWER_DEMAND_TEST = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\ItalyPowerDemand\\ItalyPowerDemand_TEST.arff";

	static class BagnallClassLoader extends ClassLoader {

	}

	@Test
	public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
			PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {

		Logger.getLogger("jaicore").setLevel(Level.DEBUG);

		// Initialize classifiers
		final int k = 10;
		final int seed = 42;
		ShapeletTransformTSClassifier ownClf = new ShapeletTransformTSClassifier(k, seed);


		ShapeletTransformClassifier refClf = new ShapeletTransformClassifier();
		refClf.setNumberOfShapelets(k);
		refClf.setSeed(42);
		

		Map<String, Object> result = TSClassifierTest.compareClassifiers(refClf, ownClf, seed, null, null,
				new File(ITALY_POWER_DEMAND_TRAIN), new File(ITALY_POWER_DEMAND_TEST));
		System.out.println(result.toString());
	}
}
