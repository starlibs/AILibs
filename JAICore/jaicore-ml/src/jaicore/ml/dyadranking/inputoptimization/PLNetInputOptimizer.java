package jaicore.ml.dyadranking.inputoptimization;

import java.util.Random;

import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.primitives.Pair;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.general.DyadRankingInstanceSupplier;

public class PLNetInputOptimizer {
	
	private PLNetDyadRanker plNet;
	
	public PLNetInputOptimizer(PLNetDyadRanker plNet) {
		super();
		this.plNet = plNet;
	}
	
	public INDArray optimizeInput(INDArray input, InputOptimizerLoss loss) {
		INDArray inp = input.dup();
		INDArray grad = computeInputDerivative(inp, loss);
		System.out.print(inp);
		Dyad testinpDyad = ndArrayToDyad(inp, 2, 2);
		System.out.print("PLNet output: " + plNet.getPlNet().output(inp) + " ");
		System.out.println(" input score: " + DyadRankingInstanceSupplier.inputOptimizerTestScore(testinpDyad));
		for(int i = 0; i < 50; i++) {
			System.out.println("Gradient: " + grad);
			grad.muli(0.1);
			inp.subi(grad);
			grad = computeInputDerivative(inp, loss);
			System.out.print(inp);
			System.out.print("PLNet output: " + plNet.getPlNet().output(inp) + " ");
			testinpDyad = ndArrayToDyad(inp, 2, 2);
			System.out.println(" input score: " + DyadRankingInstanceSupplier.inputOptimizerTestScore(testinpDyad));
		}
		
		return inp;
	}

	private INDArray computeInputDerivative(INDArray input, InputOptimizerLoss loss) {
		MultiLayerNetwork net = plNet.getPlNet();
		
		INDArray output = net.output(input);
		INDArray lossGradient = Nd4j.create(new double[] {loss.lossGradient(output)});
		net.setInput(input);
		net.feedForward(false, false);
		Pair<Gradient, INDArray> p = net.backpropGradient(lossGradient, null);
		INDArray grad = p.getSecond();
		
		return grad;
	}
	
	public static void main(String... args) throws TrainingException, PredictionException {
		PLNetDyadRanker testnet = new PLNetDyadRanker();
		IDataset train = DyadRankingInstanceSupplier.getInputOptTestSet(5, 2000);
		testnet.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8,6,4,4");
		testnet.train(train);
		
		int maxDyadRankingLength = 5;
		int nTestInstances = 100;
		double avgKendallTau = 0;
		
		for (int testInst = 0; testInst < nTestInstances; testInst++) {
			IDyadRankingInstance test = DyadRankingInstanceSupplier.getDyadRankingInstance(maxDyadRankingLength, 2, 2, DyadRankingInstanceSupplier.inputOptimizerTestRanker());
			IDyadRankingInstance predict = testnet.predict(test);
			
			int dyadRankingLength = test.length();
			int nConc = 0;
			int nDisc = 0;
			for (int i = 1; i < dyadRankingLength; i++) {
				for (int j = 0; j < i; j++) {
					if (DyadRankingInstanceSupplier.inputOptimizerTestRanker().compare(
							predict.getDyadAtPosition(j), predict.getDyadAtPosition(i)) <= 0) {
						nConc++;
					} else {
						nDisc++;
					}
				}
			}
			double kendallTau = 2.0 * (nConc - nDisc) / (dyadRankingLength * (dyadRankingLength - 1) );
			avgKendallTau += kendallTau;
			
		}
		avgKendallTau /= nTestInstances;
		
		System.out.println("Kendall's tau: " + avgKendallTau); 
		
		PLNetInputOptimizer inpopt = new PLNetInputOptimizer(testnet);	
		Random rng = new Random(1);
		double[] randDoubles = new double[4];
		for (int i = 0; i < 4; i++) {
			randDoubles[i] = rng.nextGaussian();
		}
		INDArray testinp = Nd4j.create(randDoubles);
		inpopt.optimizeInput(testinp, new NegIdentityInpOptLoss());
	}
	
	private static Dyad ndArrayToDyad(INDArray arr, int instSize, int altSize) {
		INDArray instSlice = arr.get(NDArrayIndex.interval(0, instSize));
		INDArray altSlice = arr.get(NDArrayIndex.interval(instSize, instSize + altSize));
		Vector instVector = new DenseDoubleVector(instSlice.toDoubleVector());
		Vector altVector = new DenseDoubleVector(altSlice.toDoubleVector());
		
		return new Dyad(instVector, altVector);
	}
}
