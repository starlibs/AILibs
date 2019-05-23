package jaicore.ml.dyadranking;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.dyadranking.algorithm.PLNetLoss;

public class PLNetLossTest {
	
	private double tolerance = Math.pow(10, -4);
	
	@Test
	public void computeLossTest1() {
		INDArray plNetOutputs = Nd4j.create(new double[]{10.0, 5.0, 2.0, 1});
		double trueLoss = 0.38632; // manually computed value
		double computedLoss = PLNetLoss.computeLoss(plNetOutputs).getDouble(0);

		assertTrue(Math.abs(trueLoss - computedLoss) < tolerance);
	}
	
	@Test
	public void computeLossTest2() {
		INDArray plNetOutputs = Nd4j.create(new double[]{-2.0, 42.123, -0.01, 0});
		double trueLoss = 44.8212; // manually computed value
		double computedLoss = PLNetLoss.computeLoss(plNetOutputs).getDouble(0);

		assertTrue(Math.abs(trueLoss - computedLoss) < tolerance);
	}
	
	@Test
	public void computeLossTest3() {
		INDArray plNetOutputs = Nd4j.create(new double[]{12.345, 6.789});
		double trueLoss = 0.00385676; // manually computed value
		double computedLoss = PLNetLoss.computeLoss(plNetOutputs).getDouble(0);
		assertTrue(Math.abs(trueLoss - computedLoss) < tolerance);
	}
	
	@Test
	public void computeGradientTest1() {
		INDArray plNetOutputs = Nd4j.create(new double[]{10.0, 5.0, 2.0, 1});
		int k = 0;
		double trueGradient = -0.0071453953; // manually computed value
		double computedGradient = PLNetLoss.computeLossGradient(plNetOutputs, k).getDouble(0);
		assertTrue(Math.abs(trueGradient - computedGradient) < tolerance);
	}
	
	@Test
	public void computeGradientTest2() {
		INDArray plNetOutputs = Nd4j.create(new double[]{10.0, 5.0, 2.0, 1});
		int k = 1;
		double trueGradient = -0.0570706464; // manually computed value
		double computedGradient = PLNetLoss.computeLossGradient(plNetOutputs, k).getDouble(0);
		assertTrue(Math.abs(trueGradient - computedGradient) < tolerance);
	}
	
	@Test
	public void computeGradientTest3() {
		INDArray plNetOutputs = Nd4j.create(new double[]{10.0, 5.0, 2.0, 1});
		int k = 3;
		double trueGradient = 0.2862117749; // manually computed value
		double computedGradient = PLNetLoss.computeLossGradient(plNetOutputs, k).getDouble(0);
		assertTrue(Math.abs(trueGradient - computedGradient) < tolerance);
	}
	
	@Test
	public void computeGradientTest4() {
		INDArray plNetOutputs = Nd4j.create(new double[]{-2.0, 4.2, 0});
		int k = 0;
		double trueGradient = -0.99800454; // manually computed value
		double computedGradient = PLNetLoss.computeLossGradient(plNetOutputs, k).getDouble(0);
		assertTrue(Math.abs(trueGradient - computedGradient) < tolerance);
	}
	
	@Test
	public void computeGradientTest5() {
		INDArray plNetOutputs = Nd4j.create(new double[]{-2.0, 4.2, 0});
		int k = 1;
		double trueGradient = 0.968486; // manually computed value
		double computedGradient = PLNetLoss.computeLossGradient(plNetOutputs, k).getDouble(0);
		assertTrue(Math.abs(trueGradient - computedGradient) < tolerance);
	}
	
	@Test
	public void computeGradientTest6() {
		INDArray plNetOutputs = Nd4j.create(new double[]{-2.0, 4.2, 0});
		int k = 2;
		double trueGradient = 0.0295186; // manually computed value
		double computedGradient = PLNetLoss.computeLossGradient(plNetOutputs, k).getDouble(0);
		assertTrue(Math.abs(trueGradient - computedGradient) < tolerance);
	}
	
	@Test
	public void computeGradientTest7() {
		INDArray plNetOutputs = Nd4j.create(new double[]{12.345, 6.789});
		int k = 0;
		double trueGradient = -0.00384933; // manually computed value
		double computedGradient = PLNetLoss.computeLossGradient(plNetOutputs, k).getDouble(0);
		assertTrue(Math.abs(trueGradient - computedGradient) < tolerance);
	}
	
	@Test
	public void computeGradientTest8() {
		INDArray plNetOutputs = Nd4j.create(new double[]{12.345, 6.789});
		int k = 1;
		double trueGradient = 0.00384933; // manually computed value
		double computedGradient = PLNetLoss.computeLossGradient(plNetOutputs, k).getDouble(0);
		assertTrue(Math.abs(trueGradient - computedGradient) < tolerance);
	}
}
