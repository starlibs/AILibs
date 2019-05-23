package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.RandomTreePerformanceBasedFeatureGenerator;
import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
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
public class RandomTreePerformanceBasedFeatureGeneratorTest {

	/**
	 * Tests whether there are errors when training with a small example given as a
	 * map of vectors and performance values where all values are set.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTrainWithVectorNoUnsetValues() throws Exception {
		HashMap<Vector, Double> map = new HashMap<>();
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 5.0);
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 7.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, -1 }), 3.0);
		map.put(new DenseDoubleVector(new double[] { -1, -1, 1 }), 10.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, 1 }), 30.0);

		System.out.println("Build Tree on Data:");
		System.out.println(map);

		boolean successfulTraining = false;
		try {
			RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
			treeFeatureGen.disallowNonOccurence();
			treeFeatureGen.setOccurenceValue(1);
			treeFeatureGen.setNonOccurenceValue(-1);
			treeFeatureGen.train(map);
			successfulTraining = true;

			System.out.println(treeFeatureGen);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(true, successfulTraining);
	}

	/**
	 * Tests whether there are errors when training with a small example given as a
	 * map of vectors and performance values that has some missing values.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTrainWithVectorUnsetValues() throws Exception {
		HashMap<Vector, Double> map = new HashMap<>();
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 5.0);
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 7.0);
		map.put(new DenseDoubleVector(new double[] { -1, 0, -1 }), 3.0);
		map.put(new DenseDoubleVector(new double[] { -1, -1, 1 }), 10.0);
		map.put(new DenseDoubleVector(new double[] { 0, 1, 1 }), 30.0);

		System.out.println("Build Tree on Data:");
		System.out.println(map);

		boolean successfulTraining = false;
		try {
			RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
			treeFeatureGen.setAllowNonOccurence(-1);
			treeFeatureGen.setOutgoingUnsetValueValue(0);
			treeFeatureGen.setOccurenceValue(1);
			treeFeatureGen.setNonOccurenceValue(-1);
			treeFeatureGen.train(map);

			System.out.println(treeFeatureGen);

			successfulTraining = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(true, successfulTraining);
	}

	/**
	 * Tests whether there are errors when training with a small example given as a
	 * Instances object and performance values where all values are set.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTrainWithInstancesNoUnsetValues() throws Exception {
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

		System.out.println("Build Tree on Data:");
		System.out.println(data);

		boolean successfulTraining = false;
		try {
			RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
			treeFeatureGen.disallowNonOccurence();
			treeFeatureGen.setOccurenceValue(1);
			treeFeatureGen.setNonOccurenceValue(-1);
			treeFeatureGen.train(data);

			System.out.println(treeFeatureGen);

			successfulTraining = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(true, successfulTraining);
	}

	/**
	 * Tests whether there are errors when training with a small example given as a
	 * Instances object and performance values that has some missing values.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTrainWithInstancesUnsetValues() throws Exception {
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

		System.out.println("Build Tree on Data:");
		System.out.println(data);

		boolean successfulTraining = false;
		try {
			RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
			treeFeatureGen.setAllowNonOccurence(0);
			treeFeatureGen.setNonOccurenceValue(0);
			treeFeatureGen.setOccurenceValue(1);
			treeFeatureGen.setNonOccurenceValue(-1);
			treeFeatureGen.train(data);

			System.out.println(treeFeatureGen);

			successfulTraining = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(true, successfulTraining);
	}

	/**
	 * Tests whether there are errors when constructing a feature vector for some
	 * training examples that do not contain unset values.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPredictOnTrainingDataNoUnsetValues() throws Exception {
		// Train Model
		HashMap<Vector, Double> map = new HashMap<>();
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 5.0);
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 7.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, -1 }), 3.0);
		map.put(new DenseDoubleVector(new double[] { -1, -1, 1 }), 10.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, 1 }), 30.0);

		System.out.println("Build Tree on Data:");
		System.out.println(map);

		boolean trainAndPredictSuccesfully = false;
		try {
			RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
			treeFeatureGen.disallowNonOccurence();
			treeFeatureGen.setOccurenceValue(1);
			treeFeatureGen.setNonOccurenceValue(-1);
			treeFeatureGen.train(map);

			System.out.println(treeFeatureGen);

			// Predictions for the training data
			Vector features = new DenseDoubleVector(new double[] { 1, -1, -1 });
			Vector prediction = treeFeatureGen.predict(features);
			System.out.println(features + " --> " + prediction);

			features = new DenseDoubleVector(new double[] { -1, 1, -1 });
			prediction = treeFeatureGen.predict(features);
			System.out.println(features + " --> " + prediction);

			features = new DenseDoubleVector(new double[] { -1, -1, 1 });
			prediction = treeFeatureGen.predict(features);
			System.out.println(features + " --> " + prediction);

			features = new DenseDoubleVector(new double[] { -1, 1, 1 });
			prediction = treeFeatureGen.predict(features);
			System.out.println(features + " --> " + prediction);
			trainAndPredictSuccesfully = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(true, trainAndPredictSuccesfully);
	}

	/**
	 * Tests whether there are errors when constructing a feature vector for some
	 * training examples that does contain unset values.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPredictOnTrainingDataUnsetValues() throws Exception {
		// Train Model
		HashMap<Vector, Double> map = new HashMap<>();
		map.put(new DenseDoubleVector(new double[] { 1, -1, -1 }), 5.0);
		map.put(new DenseDoubleVector(new double[] { -1, 1, -1 }), 7.0);
		map.put(new DenseDoubleVector(new double[] { 0, 0, -1 }), 3.0);
		map.put(new DenseDoubleVector(new double[] { -1, -1, 1 }), 10.0);
		map.put(new DenseDoubleVector(new double[] { 0, 1, 1 }), 30.0);

		System.out.println("Build Tree on Data:");
		System.out.println(map);

		boolean trainAndPredictSuccesfully = false;
		try {
			RandomTreePerformanceBasedFeatureGenerator treeFeatureGen = new RandomTreePerformanceBasedFeatureGenerator();
			treeFeatureGen.setAllowNonOccurence(0);
			treeFeatureGen.setNonOccurenceValue(0);
			treeFeatureGen.setOccurenceValue(1);
			treeFeatureGen.setNonOccurenceValue(-1);
			treeFeatureGen.train(map);

			System.out.println(treeFeatureGen);

			// Predictions for the training data
			Vector features = new DenseDoubleVector(new double[] { 1, -1, -1 });
			Vector prediction = treeFeatureGen.predict(features);
			System.out.println(features + " --> " + prediction);

			features = new DenseDoubleVector(new double[] { -1, 1, -1 });
			prediction = treeFeatureGen.predict(features);
			System.out.println(features + " --> " + prediction);

			features = new DenseDoubleVector(new double[] { 0, 0, 1 });
			prediction = treeFeatureGen.predict(features);
			System.out.println(features + " --> " + prediction);

			features = new DenseDoubleVector(new double[] { 0, 1, 1 });
			prediction = treeFeatureGen.predict(features);
			System.out.println(features + " --> " + prediction);

			trainAndPredictSuccesfully = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(true, trainAndPredictSuccesfully);
	}

}