package ai.libs.jaicore.ml.ranking.dyad;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.common.math.IVector;

public interface IVectorDyad extends IDyad {

	@Override
	public IVector getContext();

	@Override
	public IVector getAlternative();

}
