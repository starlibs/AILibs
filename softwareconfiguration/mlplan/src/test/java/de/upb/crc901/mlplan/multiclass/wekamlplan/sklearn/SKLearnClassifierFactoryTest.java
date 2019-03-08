package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import jaicore.ml.WekaUtil;
import jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import weka.core.Instances;

/**
 * This tests checks whether the classifier factory for scikit learn classifiers works as expected.
 * More specifically it is checked whether the factory is capable of translating different ComponentInstances
 * into executable SKLearn classifiers.
 *
 * @author wever
 */
public class SKLearnClassifierFactoryTest {

	/* Constants for the tests */
	private static final File TEST_DATASET = new File("testrsc/car.arff");
	private static final double TEST_SPLIT_RATIO = .7;
	private static final long SEED = 0;

	/* Component Instance scenarios */
	private static final File TEST_PIPE = new File("testrsc/sklearn/sklearn_classifier_factory.pipe_description.json");

	/* Objects for tests */
	private static List<Instances> stratSplit;
	private static SKLearnClassifierFactory factory;

	@BeforeClass
	public static void setup() throws FileNotFoundException, IOException {
		/* load dataset */
		Instances data = new Instances(new FileReader(TEST_DATASET));
		data.setClassIndex(data.numAttributes() - 1);
		stratSplit = WekaUtil.getStratifiedSplit(data, SEED, TEST_SPLIT_RATIO);

		/* init factory */
		factory = new SKLearnClassifierFactory();
	}

	@Test
	public void testExtractClassifier() throws Exception {
		String imports = "from sklearn.neighbors import KNeighborsClassifier\nfrom sklearn.pipeline import make_pipeline\nfrom sklearn.preprocessing import MaxAbsScaler";
		String composition = "make_pipeline(MaxAbsScaler(),KNeighborsClassifier(p=1,weights=\"distance\",n_neighbors=46))";

		ScikitLearnWrapper slw = new ScikitLearnWrapper(composition, imports);
		slw.buildClassifier(stratSplit.get(0));
		double[] prediction = slw.classifyInstances(stratSplit.get(1));

		List<Double> expected = stratSplit.get(1).stream().map(x -> x.classValue()).collect(Collectors.toList());

		assertEquals(expected.size(), prediction.length);
	}

	private void testExtractedClassifier(final String imports, final String composition) throws Exception {

	}

}
