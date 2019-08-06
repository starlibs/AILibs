package ai.libs.jaicore.ml.ranking.dyadranking.dataset;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.jaicore.ml.ranking.dyadranking.Dyad;

public abstract class ADyadRankingInstance implements IDyadRankingInstance {

	@Override
	public INDArray toMatrix() {
		List<INDArray> dyadList = new ArrayList<>(this.getNumFeatures());
		for (Dyad dyad : this) {
			INDArray dyadVector = dyad.toVector();
			dyadList.add(dyadVector);
		}
		INDArray dyadMatrix;
		dyadMatrix = Nd4j.vstack(dyadList);
		return dyadMatrix;
	}
}
