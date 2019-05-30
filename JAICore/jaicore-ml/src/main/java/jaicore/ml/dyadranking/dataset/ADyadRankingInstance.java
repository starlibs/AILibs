package jaicore.ml.dyadranking.dataset;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.dyadranking.Dyad;

public abstract class ADyadRankingInstance implements IDyadRankingInstance {

	@Override
	public INDArray toMatrix() {
		List<INDArray> dyadList = new ArrayList<>(this.length());
		for (Dyad dyad : this) {
			INDArray dyadVector = dyad.toVector();
			dyadList.add(dyadVector);
		}
		INDArray dyadMatrix;
		dyadMatrix = Nd4j.vstack(dyadList);
		return dyadMatrix;
	}
}
