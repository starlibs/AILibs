package ai.libs.automl.metamining.pipelinecharacterizing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.common.math.IVector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.mlplan.metamining.pipelinecharacterizing.RandomTreePerformanceBasedFeatureGenerator;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Tests the {@link RandomTreePerformanceBasedFeatureGenerator}. All tests rely
 * on a user to read the console output and confirm correctness since the
 * RandomTreePerformanceBasedFeatureGenerator is based on a RandomTree and thus
 * produces randomized results.
 *
 * @author Helena Graf
 *
 */
@Disabled("This project is currently not maintained")
public class RandomTreePerformanceBasedFeatureGeneratorTest extends ATest {

	private static final String MSG_TRANSFORM = "{} --> {}";
	private static final String MSG_BUILD = "Build Tree on Data:\n {}";
	private static final String MSG_GEN = "gen: {}";
	private static final String MSG_LOG = MSG_BUILD;

	/**
	 * Tests whether there are errors when training with a small example given as a
	 * map of vectors and performance values where all values are set.
	 * @throws TrainingException
	 *
	 * @throws Exception
	 */
	@Test
	public void testTrainWithVectorNoUnsetValues() throws TrainingException {
		HashMap<IVector, Double> map = new HashMap<>();
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 5.0);
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 7.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, -1 }), 3.0);
		map.put(new DenseDoubleVector(new double[] { -1, -1, 1 }), 10.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, 1 }), 30.0);

		this.logger.info(MSG_BUILD, map);

		boolean successfulTraining = false;
		RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
		treeFeatureGen.disallowNonOccurence();
		treeFeatureGen.setOccurenceValue(1);
		treeFeatureGen.setNonOccurenceValue(-1);
		treeFeatureGen.train(map);
		successfulTraining = true;

		this.logger.info("Tree: {}", treeFeatureGen);


		assertEquals(true, successfulTraining);
	}

	/**
	 * Tests whether there are errors when training with a small example given as a
	 * map of vectors and performance values that has some missing values.
	 * @throws TrainingException
	 *
	 * @throws Exception
	 */
	@Test
	public void testTrainWithVectorUnsetValues() throws TrainingException {
		HashMap<IVector, Double> map = new HashMap<>();
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 5.0);
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 7.0);
		map.put(new DenseDoubleVector(new double[] { -1, 0, -1 }), 3.0);
		map.put(new DenseDoubleVector(new double[] { -1, -1, 1 }), 10.0);
		map.put(new DenseDoubleVector(new double[] { 0, 1, 1 }), 30.0);

		this.logger.info(MSG_BUILD, map);

		boolean successfulTraining = false;
		RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
		treeFeatureGen.setAllowNonOccurence(-1);
		treeFeatureGen.setOutgoingUnsetValueValue(0);
		treeFeatureGen.setOccurenceValue(1);
		treeFeatureGen.setNonOccurenceValue(-1);
		treeFeatureGen.train(map);

		this.logger.info(MSG_GEN, treeFeatureGen);

		successfulTraining = true;

		assertEquals(true, successfulTraining);
	}

	/**
	 * Tests whether there are errors when training with a small example given as a
	 * Instances object and performance values where all values are set.
	 * @throws AlgorithmException
	 *
	 * @throws Exception
	 */
	@Test
	public void testTrainWithInstancesNoUnsetValues() throws AlgorithmException  {
		ArrayList<Attribute> attInfo = new ArrayList<>();
		attInfo.add(new Attribute("Att1"));
		attInfo.add(new Attribute("Att2"));
		attInfo.add(new Attribute("Att3"));
		attInfo.add(new Attribute("Target"));
		Instances data = new Instances("Train", attInfo, 5);
		data.add(new DenseInstance(1, new double[] { 1, -1, -1, 5 }));
		data.add(new DenseInstance(1, new double[] { 1, -1, -1, 7 }));
		data.add(new DenseInstance(1, new double[] { -1, 1, -1, 3 }));
		data.add(new DenseInstance(1, new double[] { -1, -1, 1, 10 }));
		data.add(new DenseInstance(1, new double[] { -1, 1, 1, 30 }));
		data.setClassIndex(attInfo.size() - 1);

		this.logger.info(MSG_BUILD, data);

		boolean successfulTraining = false;
		RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
		treeFeatureGen.disallowNonOccurence();
		treeFeatureGen.setOccurenceValue(1);
		treeFeatureGen.setNonOccurenceValue(-1);
		treeFeatureGen.train(data);

		this.logger.info(MSG_GEN, treeFeatureGen);

		successfulTraining = true;


		assertEquals(true, successfulTraining);
	}

	/**
	 * Tests whether there are errors when training with a small example given as a
	 * Instances object and performance values that has some missing values.
	 * @throws AlgorithmException
	 *
	 * @throws Exception
	 */
	@Test
	public void testTrainWithInstancesUnsetValues() throws AlgorithmException {
		ArrayList<Attribute> attInfo = new ArrayList<>();
		attInfo.add(new Attribute("Att1"));
		attInfo.add(new Attribute("Att2"));
		attInfo.add(new Attribute("Att3"));
		attInfo.add(new Attribute("Target"));
		Instances data = new Instances("Train", attInfo, 5);
		data.add(new DenseInstance(1, new double[] { 1, -1, -1, 5 }));
		data.add(new DenseInstance(1, new double[] { 1, -1, -1, 7 }));
		data.add(new DenseInstance(1, new double[] { -1, 0, -1, 3 }));
		data.add(new DenseInstance(1, new double[] { -1, -1, 1, 10 }));
		data.add(new DenseInstance(1, new double[] { 0, 1, 1, 30 }));
		data.setClassIndex(attInfo.size() - 1);

		this.logger.info(MSG_LOG, data);

		boolean successfulTraining = false;
		RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
		treeFeatureGen.setAllowNonOccurence(0);
		treeFeatureGen.setNonOccurenceValue(0);
		treeFeatureGen.setOccurenceValue(1);
		treeFeatureGen.setNonOccurenceValue(-1);
		treeFeatureGen.train(data);

		this.logger.info(MSG_GEN, treeFeatureGen);

		successfulTraining = true;


		assertEquals(true, successfulTraining);
	}

	/**
	 * Tests whether there are errors when constructing a feature vector for some
	 * training examples that do not contain unset values.
	 * @throws TrainingException
	 *
	 * @throws Exception
	 */
	@Test
	public void testPredictOnTrainingDataNoUnsetValues() throws TrainingException  {
		// Train Model
		HashMap<IVector, Double> map = new HashMap<>();
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 5.0);
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 7.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, -1 }), 3.0);
		map.put(new DenseDoubleVector(new double[] { -1, -1, 1 }), 10.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, 1 }), 30.0);

		this.logger.info(MSG_BUILD, map);

		boolean trainAndPredictSuccesfully = false;

		RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
		treeFeatureGen.disallowNonOccurence();
		treeFeatureGen.setOccurenceValue(1);
		treeFeatureGen.setNonOccurenceValue(-1);
		treeFeatureGen.train(map);

		this.logger.info(MSG_GEN, treeFeatureGen);

		// Predictions for the training data
		IVector features = new DenseDoubleVector(new double[] { 1, -1, -1 });
		IVector prediction = treeFeatureGen.predict(features);
		this.logger.info(MSG_TRANSFORM, features, prediction);

		features = new DenseDoubleVector(new double[] { -1, 1, -1 });
		prediction = treeFeatureGen.predict(features);
		this.logger.info(MSG_TRANSFORM, features, prediction);

		features = new DenseDoubleVector(new double[] { -1, -1, 1 });
		prediction = treeFeatureGen.predict(features);
		this.logger.info(MSG_TRANSFORM, features, prediction);

		features = new DenseDoubleVector(new double[] { -1, 1, 1 });
		prediction = treeFeatureGen.predict(features);
		this.logger.info(MSG_TRANSFORM, features, prediction);
		trainAndPredictSuccesfully = true;

		assertEquals(true, trainAndPredictSuccesfully);
	}

	/**
	 * Tests whether there are errors when constructing a feature vector for some
	 * training examples that does contain unset values.
	 * @throws TrainingException
	 *
	 * @throws Exception
	 */
	@Test
	public void testPredictOnTrainingDataUnsetValues() throws TrainingException {
		// Train Model
		HashMap<IVector, Double> map = new HashMap<>();
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 5.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, -1 }), 7.0);
		map.put(new DenseDoubleVector(new double[] { 0, 0, -1 }), 3.0);
		map.put(new DenseDoubleVector(new double[] { -1, -1, 1 }), 10.0);
		map.put(new DenseDoubleVector(new double[] { 0, 1, 1 }), 30.0);

		this.logger.info(MSG_BUILD, map);

		boolean trainAndPredictSuccesfully = false;
		RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
		treeFeatureGen.setAllowNonOccurence(0);
		treeFeatureGen.setNonOccurenceValue(0);
		treeFeatureGen.setOccurenceValue(1);
		treeFeatureGen.setNonOccurenceValue(-1);
		treeFeatureGen.train(map);

		this.logger.info("Feature gen: {}", treeFeatureGen);

		// Predictions for the training data
		IVector features = new DenseDoubleVector(new double[] { 1, -1, -1 });
		IVector prediction = treeFeatureGen.predict(features);
		this.logger.info(MSG_TRANSFORM, features, prediction);

		features = new DenseDoubleVector(new double[] { -1, 1, -1 });
		prediction = treeFeatureGen.predict(features);
		this.logger.info(MSG_TRANSFORM, features, prediction);

		features = new DenseDoubleVector(new double[] { 0, 0, 1 });
		prediction = treeFeatureGen.predict(features);
		this.logger.info(MSG_TRANSFORM, features, prediction);

		features = new DenseDoubleVector(new double[] { 0, 1, 1 });
		prediction = treeFeatureGen.predict(features);
		this.logger.info(MSG_TRANSFORM, features, prediction);

		trainAndPredictSuccesfully = true;

		assertEquals(true, trainAndPredictSuccesfully);
	}

}