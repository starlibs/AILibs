package jaicore.ml.dyadranking.inputopt;

import java.io.IOException;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.zeroshot.inputoptimization.NegIdentityInpOptLoss;
import jaicore.ml.dyadranking.zeroshot.inputoptimization.PLNetInputOptimizer;

public class ClassifierInputOptTest {
	
	double[] dataset12Features = { 0.7207469444444443, 0.8065, 0.10388888888888889, 0.9943208333333334, 0.0535, 0.9405555555555556, 0.9741918055555554, 0.0465, 0.9483333333333334, 0.7207469444444443, 0.8065,
			0.10388888888888889, 0.9368430555555556, 0.1405, 0.8438888888888889, 0.9371906944444445, 0.1385, 0.8461111111111111, 0.9329148611111111, 0.139, 0.8455555555555555, 0.9919074999999999, 0.0765, 0.915,
			0.7207469444444443, 0.8065, 0.10388888888888889, 0.8512372222222222, 0.6285, 0.30166666666666664, 0.9191183333333334, 0.366, 0.5933333333333334, 0.7091333333333334, 0.8055, 0.105, 0.8219948611111112,
			0.655, 0.2722222222222222, 0.8862797222222223, 0.5005, 0.44388888888888883, 0.9753261111111112, 0.044, 0.951111111111111 };
	
	double[] dataset36Features = { 0.7749482924482924, 0.7155844155844155, 0.16515151515151513, 0.9727294590930956, 0.16493506493506493, 0.8075757575757576, 0.9706962481962482, 0.049783549783549784,
			0.9419191919191917, 0.7749482924482924, 0.7155844155844155, 0.16515151515151513, 0.9824459967641785, 0.05627705627705628, 0.9343434343434344, 0.9824459967641785, 0.05627705627705628,
			0.9343434343434344, 0.9818702173247627, 0.05670995670995671, 0.9338383838383838, 0.9707547334820061, 0.1974025974025974, 0.7696969696969697, 0.7746190257553894, 0.7155844155844155,
			0.16515151515151513, 0.8884072324981415, 0.4354978354978355, 0.4919191919191919, 0.9532948532948534, 0.19090909090909092, 0.7772727272727272, 0.774230180593817, 0.7142857142857143,
			0.16666666666666666, 0.8830548996458087, 0.4406926406926407, 0.4858585858585858, 0.9289848703485067, 0.3012987012987013, 0.6484848484848484, 0.9725899689536053, 0.048484848484848485, 0.9434343434343433 };
	
	double[] dataset5Features = { 0.6180102200255632, 0.4247787610619469, 0.1937120240816099, 0.8210588171934307, 0.31194690265486724, 0.5175874832527193, 0.6714053412150048, 0.415929203539823, 0.3450991876936356, 0.6015576834475163, 0.4336283185840708, 0.1875349639126567, 0.7540470500074802, 0.3075221238938053, 0.5190532326959291, 0.7593984914854257, 0.3075221238938053, 0.5181975736568458, 0.758716093894532, 0.31194690265486724, 0.50859715946767, 0.7862281086897593, 0.39601769911504425, 0.40657180577966845, 0.5603291437532364, 0.46017699115044247, 0.08014128190828411, 0.6902554504242385, 0.40707964601769914, 0.3166146261298275, 0.7281550256964924, 0.35176991150442477, 0.4163573905487383, 0.5336057568745161, 0.45353982300884954, 0.024426194988418536, 0.5362809739916099, 0.46238938053097345, 0.04575850017172064, 0.6534286979987087, 0.42920353982300885, 0.17006918614005703, 0.5985703851111869, 0.4646017699115044, 0.21277866242038226 };
	
	@Test
	public void testInputOpt() throws TrainingException, PredictionException, IOException {
		PLNetDyadRanker plNet = new PLNetDyadRanker();
		plNet.loadModelFromFile("datasets/zeroshot/RFPLNet.plnet.zip");
		
		System.out.println("PLNet loaded");

		INDArray dsFeat = Nd4j.create(dataset5Features);
		INDArray initHyperPars = Nd4j.create(new double[]{0.5, 0.5, 0.5, 0.5});
		INDArray inputMask = Nd4j.hstack(Nd4j.zeros(dsFeat.columns()), Nd4j.ones(4));
		
		INDArray init = Nd4j.hstack(dsFeat, initHyperPars);

		INDArray optimized = new PLNetInputOptimizer().optimizeInput(
				plNet, init, new NegIdentityInpOptLoss(), 0.001, 200, inputMask);
		/*
		System.out.println("OPTIMAL hyper parameters: " 
				+ optimized.getDouble(optimized.length()-2) + ", " 
				+ optimized.getDouble(optimized.length()-1));
		double C = optimized.getDouble(optimized.length()-2) * (0.5 - 0.001) + 0.001;
		double M = optimized.getDouble(optimized.length()-1) * (50 - 1) + 1;
		System.out.println("Denormalized: C = " + C + ", M = " + M);
		*/
		/*
		System.out.println("OPTIMAL hyper parameters: " 
				+ optimized.getDouble(optimized.length()-3) + ", " 
				+ optimized.getDouble(optimized.length()-2) + ", " 
				+ optimized.getDouble(optimized.length()-1));
		double C = optimized.getDouble(optimized.length()-3) * (3.0 - (-3.0)) + (-3.0);
		double L = optimized.getDouble(optimized.length()-2) * (0 - (-5.0)) + (-5.0);
		double G = optimized.getDouble(optimized.length()-1) * (3.0 - (-3.0)) + (-3.0);
		System.out.println("Denormalized: C = " + C + ", L = " + L +", G = " + G);
		*/
		
		System.out.println("OPTIMAL hyper parameters: " 
				+ optimized.getDouble(optimized.length()-4) + ", " 
				+ optimized.getDouble(optimized.length()-3) + ", " 
				+ optimized.getDouble(optimized.length()-2) + ", " 
				+ optimized.getDouble(optimized.length()-1));
		double I = optimized.getDouble(optimized.length()-4) * (300.0 - 10.0) + (10.0);
		double K = optimized.getDouble(optimized.length()-3) * (1.0 - 0.0) + (0.0);
		double M = optimized.getDouble(optimized.length()-2) * (30.0 - (1.0)) + (1.0);
		double depth = optimized.getDouble(optimized.length()-1) * (100.0 - (0.0)) + (0.0);
		System.out.println("Denormalized: I = " + I + ", K = " + K +", M = " + M +", depth = " + depth);
		
	}
}
