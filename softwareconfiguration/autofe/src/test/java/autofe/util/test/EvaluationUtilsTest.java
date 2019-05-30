package autofe.util.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class EvaluationUtilsTest {
	// @Test
	public void rankKendallsTauTest() {

		// double[] ranking1 = { 0.15, 0.1, 0.2 };
		double[] ranking1 = { 0.15, 0.3 };
		// double[] ranking2 = { 0.1, 0.05, 1 };
		double[] ranking2 = { 0.1, 0.4 };

		Assert.assertEquals(1.0, EvaluationUtils.rankKendallsTau(ranking1, ranking2), 0.0001);
	}

	// @Test
	public void cocoEvaluationTest() throws InterruptedException {

		String instString = "[0.9718934893608093, 0.001479289960116148, 0.002958579920232296, 0.0, 0.001479289960116148, 0.0, 0.0, 0.0, 0.002958579920232296, 0.0, 0.0, 0.0, "
				+ "0.0, 0.0, 0.0, 0.0, 0.001479289960116148, 0.0, 0.001479289960116148, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.002958579920232296, "
				+ "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "
				+ "0.004437869880348444, 0.0, 0.001479289960116148, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "
				+ "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "
				+ "0.001479289960116148, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.001479289960116148, 0.004437869880348444, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "
				+ "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "
				+ "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "
				+ "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "
				+ "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.489645004272461, 11.947349548339844, 0.2812563180923462, "
				+ "87.98896789550781, 4175163.5, 0.0, 0.0, 0.0, 0.0, 3.0]";

		double[] arr = Arrays.stream(instString.substring(1, instString.length() - 1).split(",")).map(String::trim)
				.mapToDouble(Double::parseDouble).toArray();

		ArrayList<Attribute> atts = new ArrayList<>();
		for (int i = 0; i < arr.length - 1; i++) {
			atts.add(new Attribute("att" + i));
		}
		atts.add(new Attribute("class", Arrays.asList("3.0")));
		final Instances batch = new Instances("Test", atts, 1);
		batch.setClassIndex(batch.numAttributes() - 1);

		final Instance inst = new DenseInstance(1, arr);
		inst.setDataset(batch);
		inst.setClassValue("" + arr[arr.length - 1]);
		batch.add(inst);

		Assert.assertEquals(0.3132617035483811, EvaluationUtils.calculateCOCOForBatch(batch), 0.00001);
	}

	@Test
	public void evaluationTimingTest() throws Exception {
		DataSet dataSet = DataSetUtils.getDataSetByID(DataSetUtilsTest.CIFAR10_ID);
		DataSet train = DataSetUtils.getStratifiedSplit(dataSet, new Random(42), true, 0.7).get(0);

		long startTimestamp = System.currentTimeMillis();

		double ldaResult = EvaluationUtils.performKernelLDA(train.getInstances(), 2);
		System.out.println("Result LDA: " + ldaResult);

		System.out.println(
				"LDA evaluation on complete dataset took " + (System.currentTimeMillis() - startTimestamp) + " ms.");
		startTimestamp = System.currentTimeMillis();

		System.out.println("Result clustering: " + EvaluationUtils.performKernelClustering(train.getInstances(), 2));
		System.out.println("Cluster evaluation on complete dataset took "
				+ (System.currentTimeMillis() - startTimestamp) + " ms.");

		Assert.assertTrue(ldaResult > 0);
	}
}
