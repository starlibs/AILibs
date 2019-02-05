package jaicore.ml.dyadranking.inputopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.inputoptimization.NegIdentityInpOptLoss;
import jaicore.ml.dyadranking.inputoptimization.PLNetInputOptimizer;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;
import jaicore.ml.dyadranking.util.DyadNormalScaler;

public class J48InputOptTest {
	
	private static final String datapath = "datasets/zeroshot/J48train.dr";
	
	double[] dataset12Features = { 0.7207469444444443, 0.8065, 0.10388888888888889, 0.9943208333333334, 0.0535, 0.9405555555555556, 0.9741918055555554, 0.0465, 0.9483333333333334, 0.7207469444444443, 0.8065,
			0.10388888888888889, 0.9368430555555556, 0.1405, 0.8438888888888889, 0.9371906944444445, 0.1385, 0.8461111111111111, 0.9329148611111111, 0.139, 0.8455555555555555, 0.9919074999999999, 0.0765, 0.915,
			0.7207469444444443, 0.8065, 0.10388888888888889, 0.8512372222222222, 0.6285, 0.30166666666666664, 0.9191183333333334, 0.366, 0.5933333333333334, 0.7091333333333334, 0.8055, 0.105, 0.8219948611111112,
			0.655, 0.2722222222222222, 0.8862797222222223, 0.5005, 0.44388888888888883, 0.9753261111111112, 0.044, 0.951111111111111 };
	
	@Test
	public void testJ48InputOpt() throws TrainingException, PredictionException {
		File inputFile = new File(datapath);
		DyadRankingDataset data = new DyadRankingDataset();
		
		try {
			data.deserialize(new FileInputStream(inputFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		DyadRankingDataset trainData = new DyadRankingDataset(data.subList(0, (int) 0.8 * data.size()));
		DyadRankingDataset testData = new DyadRankingDataset(data.subList((int) 0.8 * data.size(), data.size()));
		
		DyadNormalScaler scaler = new DyadNormalScaler();
		scaler.fit(trainData);
		scaler.transformAlternatives(trainData);
		scaler.transformAlternatives(testData);
		
		PLNetDyadRanker plNet = new PLNetDyadRanker();
		plNet.train(trainData);
		
		double avgKendallTau = 0.0d;
		avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, plNet);
		System.out.println("Average Kendall's tau for " + plNet.getClass().getSimpleName() + ": " + avgKendallTau);
		
		INDArray ds12feat = Nd4j.create(dataset12Features);
		INDArray initHyperPars = Nd4j.create(new double[]{0.5, 0.1});
		INDArray inputMask = Nd4j.hstack(Nd4j.zeros(ds12feat.columns()), Nd4j.create(new double[]{1.0, 1.0}));
		
		INDArray init = Nd4j.hstack(ds12feat, initHyperPars);
		scaler.printMaxima();
		scaler.printMinima();
		INDArray optimized = PLNetInputOptimizer.optimizeInput(
				plNet, init, new NegIdentityInpOptLoss(), 0.1, 100, inputMask);
		System.out.println("OPTIMAL hyper parameters: " + optimized.getDouble(optimized.length()-2, optimized.length()-1));
	}
}
