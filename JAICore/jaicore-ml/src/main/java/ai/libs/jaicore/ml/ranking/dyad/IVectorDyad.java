package ai.libs.jaicore.ml.ranking.dyad;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;

import ai.libs.jaicore.math.linearalgebra.IVector;

public interface IVectorDyad extends IDyad {

	@Override
	public IVector getInstance();

	@Override
	public IVector getAlternative();

}
