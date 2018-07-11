package jaicore.ml.extendedtree.synthetic;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import jaicore.ml.core.Interval;
import jaicore.ml.intervaltree.ExtendedRandomForest;
import junit.framework.Assert;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomForestTest {
	
	private static String trainFile = "resources/regression_data/cpu.small.arff_RQPtrain.arff";
	
	private static String testFile = "resources/regression_data/cpu.small.arff_RQPtest.arff";

	
	private ExtendedRandomForest classifier;
	
	@Before
	public void testTrain(){
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(trainFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			data.setClassIndex(data.numAttributes() - 1);
			
			classifier = new ExtendedRandomForest();
			classifier.buildClassifier(data);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	/**
	 * Test the classifier without any cross-validation
	 */
	@Test
	public void testPredict() {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(testFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			List<Double> predictedLowers = new ArrayList<>();
			List<Double> actualLowers = new ArrayList<>();
			List<Double> predictedUppers = new ArrayList<>();
			List<Double> actualUppers = new ArrayList<>();
			for (Instance instance : data) {
				// construct the real interval
				double lower = instance.value(data.numAttributes()-2);
				double upper = instance.value(data.numAttributes()-1);
				Instance strippedInstance = new DenseInstance(data.numAttributes()-2);
				for (int i = 0; i < data.numAttributes() -2 ; i++) {
					strippedInstance.setValue(i, instance.value(i));
				}
				Interval actualInterval = new Interval(lower, upper);
				Interval predictedInterval = classifier.predictInterval(strippedInstance);
				System.out.println("Actual interval: "+ actualInterval+ ", predicted Interval "+ predictedInterval);
				predictedLowers.add(predictedInterval.getLowerBound());
				predictedUppers.add(predictedInterval.getUpperBound());
				actualLowers.add(lower);
				actualUppers.add(upper);
			}
			//construct R^2 loss
			double r2lossLower = r2Loss(predictedLowers, actualLowers);
			double r2LossUpper = r2Loss(predictedUppers, actualUppers);
			System.out.println("R^2 loss for the lower bound is " + r2lossLower);
			System.out.println("R^2 loss for the upper bound is "+ r2LossUpper);
			
			double l1LossLower = L1Loss(predictedLowers, actualLowers);
			double l1LossUpper = L1Loss(predictedUppers, actualUppers);
			System.out.println("L1 loss for the lower bound is " + l1LossLower);
			System.out.println("L1 loss for the upper bound is "+ l1LossUpper);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	private static final double L1Loss(List<Double> predicted, List<Double> actual) {
		double accumulated = 0;
		for (int i = 0; i< predicted.size(); i ++) {
			accumulated += Math.abs(predicted.get(i)-actual.get(i));
		}
		return (accumulated/predicted.size());
	}
	
	private static final double r2Loss (List<Double> predicted, List<Double> actual) {
		double actualAvg = actual.stream().mapToDouble((s)->s).average().orElseThrow(IllegalStateException::new);
		double ssTot = actual.stream().mapToDouble((s)->Math.pow(s-actualAvg, 2)).sum();
		double ssRes = 0;
		for (int i = 0; i< predicted.size(); i ++) {
			ssRes += Math.pow(predicted.get(i)-actual.get(i), 2);
		}
		return  1 - (ssRes/ssTot);
	}
}
