package jaicore.ml.tsc.classifier;

import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

public class LearnShapeletsClassifier extends TSClassifier<Double> {

	private List<INDArray> S;
	private INDArray W;
	private INDArray W_0;

	public LearnShapeletsClassifier(final List<INDArray> S, final INDArray W, final INDArray W_0) {
		this.S = S;
		this.W = W;
		this.W_0 = W_0;
	}

	public List<INDArray> getS() {
		return S;
	}

	public void setS(List<INDArray> s) {
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
