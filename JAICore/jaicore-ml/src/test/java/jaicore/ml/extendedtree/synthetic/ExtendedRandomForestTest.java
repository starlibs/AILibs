package jaicore.ml.extendedtree.synthetic;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.junit.Before;
import org.junit.Test;

import jaicore.ml.intervaltree.ExtendedRandomForest;
import junit.framework.Assert;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomForestTest {

	private static String[] datasets = { "boston" };
	private static double[] noise = { 0 };

	private static int noise_count = noise.length;
	private static int dataset_count = datasets.length;

	private ExtendedRandomForest[][][] classifier = new ExtendedRandomForest[dataset_count][noise_count][seedNum];
	private static final int seedNum = 10;

	private static final double[][][] l1Lower = new double[dataset_count][noise_count][seedNum];
	private static final double[][][] l1Upper = new double[dataset_count][noise_count][seedNum];

	@Before
	public void testTrain() {
		for (int dataset_index = 0; dataset_index < dataset_count; dataset_index++) {
			for (int noise_index = 0; noise_index < noise_count; noise_index++) {
				String dataset_name = getDatasetNameForIndex(dataset_index, noise_index);
				try (BufferedReader reader = Files.newBufferedReader(Paths.get(dataset_name), StandardCharsets.UTF_8)) {
					ArffReader arffReader = new ArffReader(reader);
					Instances data = arffReader.getData();
					for (int seed = 0; seed < seedNum; seed++) {
						data.setClassIndex(data.numAttributes() - 1);
						this.classifier[dataset_index][noise_index][seed] = new ExtendedRandomForest(seed);
						this.classifier[dataset_index][noise_index][seed].buildClassifier(data);
					}
					System.out.println("Finished training. " + datasets[dataset_index] + ", " + noise[noise_index]);
				} catch (Exception e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		}
	}

	private static String getDatasetNameForIndex(final int dataset_index, final int noise_index) {
		String dataset_name = datasets[dataset_index];
		double noise_val = noise[noise_index];
		String noise_str = "";
		if (noise_val == 0.0) {
			noise_str = "0";
		} else {
			noise_str = "" + noise_val;
		}
		return String.format("resources/regression_data/%s_noise_%s_RQPtrain.arff", dataset_name, noise_str);
	}

	/**
	 * Test the classifier without any cross-validation
	 */
	@Test
	public void testPredict() {
		for (int dataset_index = 0; dataset_index < dataset_count; dataset_index++) {
			for (int noise_index = 0; noise_index < noise_count; noise_index++) {
				for (int seed = 0; seed < seedNum; seed++) {
					String testfile_name = this.getTestFileName(dataset_index);
					try (BufferedReader reader = Files.newBufferedReader(Paths.get(testfile_name),
							StandardCharsets.UTF_8)) {
						ArffReader arffReader = new ArffReader(reader);
						Instances data = arffReader.getData();
						List<Double> predictedLowers = new ArrayList<>();
						List<Double> actualLowers = new ArrayList<>();
						List<Double> predictedUppers = new ArrayList<>();
						List<Double> actualUppers = new ArrayList<>();
						for (Instance instance : data) {
							// construct the real interval
							double lower = instance.value(data.numAttributes() - 2);
							double upper = instance.value(data.numAttributes() - 1);
							Instance strippedInstance = new DenseInstance(data.numAttributes() - 2);
							for (int i = 0; i < data.numAttributes() - 2; i++) {
								strippedInstance.setValue(i, instance.value(i));
							}
							Interval actualInterval = new Interval(lower, upper);
							Interval predictedInterval = this.classifier[dataset_index][noise_index][seed]
									.predictInterval(strippedInstance);

							predictedLowers.add(predictedInterval.getInf());
							predictedUppers.add(predictedInterval.getSup());
							actualLowers.add(lower);
							actualUppers.add(upper);
						}

						double l1LossLower = L1Loss(predictedLowers, actualLowers);
						double l1LossUpper = L1Loss(predictedUppers, actualUppers);
						// System.out.println("L1 loss for the lower bound is " + l1LossLower);
						// System.out.println("L1 loss for the upper bound is " + l1LossUpper);

						l1Lower[dataset_index][noise_index][seed] = l1LossLower;
						l1Upper[dataset_index][noise_index][seed] = l1LossUpper;

					} catch (Exception e) {
						e.printStackTrace();
						Assert.fail();
					}
				}
				double avgLower = Arrays.stream(l1Lower[dataset_index][noise_index]).average().getAsDouble();
				double avgUpper = Arrays.stream(l1Upper[dataset_index][noise_index]).average().getAsDouble();
				double l1Loss = (avgLower + avgUpper) / 2;
				System.out.println(datasets[dataset_index] + " " + noise[noise_index] + " " + l1Loss);
			}
		}
	}

	private String getTestFileName(final int dataset_index) {
		String dataset_name = datasets[dataset_index];
		return String.format("resources/regression_data/%s_RQPtest.arff", dataset_name);
	}

	private static final double L1Loss(final List<Double> predicted, final List<Double> actual) {
		double accumulated = 0;
		for (int i = 0; i < predicted.size(); i++) {
			accumulated += Math.abs(predicted.get(i) - actual.get(i));
		}
		return (accumulated / predicted.size());
	}

	private static final double r2Loss(final List<Double> predicted, final List<Double> actual) {
		double actualAvg = actual.stream().mapToDouble((s) -> s).average().orElseThrow(IllegalStateException::new);
		double ssTot = actual.stream().mapToDouble((s) -> Math.pow(s - actualAvg, 2)).sum();
		double ssRes = 0;
		for (int i = 0; i < predicted.size(); i++) {
			ssRes += Math.pow(predicted.get(i) - actual.get(i), 2);
		}
		return 1 - (ssRes / ssTot);
	}
}
