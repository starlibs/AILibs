package jaicore.ml.tsc.classifier;

import org.nd4j.linalg.api.ndarray.INDArray;

public class LearnShapeletsClassifier extends TSClassifier<Double> {

	private INDArray S;
	private INDArray W;
	private INDArray W_0;

	public LearnShapeletsClassifier() {

	}

	public INDArray getS() {
		return S;
	}

	public void setS(INDArray s) {
		S = s;
	}

	public INDArray getW() {
		return W;
	}

	public void setW(INDArray w) {
		W = w;
	}

	public INDArray getW_0() {
		return W_0;
	}

	public void setW_0(INDArray w_0) {
		W_0 = w_0;
	}
}
