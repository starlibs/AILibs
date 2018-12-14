package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.exception.PredictionException;

public class LearnShapeletsClassifier extends TSClassifier<CategoricalAttributeType> {

	private List<INDArray> S;
	private INDArray W;
	private INDArray W_0;

	private int scaleR;
	private int K;
	private int minShapeLength;

	public LearnShapeletsClassifier(final int K, final double learningRate, final double regularization,
			final int scaleR, final int minShapeLength, final int maxIter) {
		super(new LearnShapeletsAlgorithm(K, learningRate, regularization, scaleR, minShapeLength, maxIter));

		this.scaleR = scaleR;
		this.K = K;
		this.minShapeLength = minShapeLength;
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

	// public INDArray getM_hat() {
	// return M_hat;
	// }
	//
	// public void setM_hat(INDArray m_hat) {
	// M_hat = m_hat;
	// }
	//
	//

	@Override
	public CategoricalAttributeType predict(IInstance instance) throws PredictionException {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public List<CategoricalAttributeType> predict(IDataset dataset) throws PredictionException {
		final List<CategoricalAttributeType> result = new ArrayList<>();
		for (IInstance inst : dataset) {
			result.add(this.predict(inst));
		}
		return result;
	}
}
