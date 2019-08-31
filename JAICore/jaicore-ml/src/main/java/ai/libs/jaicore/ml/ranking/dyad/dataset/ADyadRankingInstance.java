package ai.libs.jaicore.ml.ranking.dyad.dataset;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public abstract class ADyadRankingInstance implements INDArrayDyadRankingInstance {

	private ILabeledInstanceSchema instanceSchema;

	protected ADyadRankingInstance(final ILabeledInstanceSchema instanceSchema) {
		this.instanceSchema = instanceSchema;
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.instanceSchema;
	}

	@Override
	public INDArray toMatrix() {
		List<INDArray> dyadList = new ArrayList<>(this.getNumAttributes());
		for (IDyad dyad : this) {
			INDArray dyadVector = Nd4j.create(dyad.toDoubleVector());
			dyadList.add(dyadVector);
		}
		INDArray dyadMatrix;
		dyadMatrix = Nd4j.vstack(dyadList);
		return dyadMatrix;
	}
}
