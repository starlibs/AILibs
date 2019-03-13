package jaicore.ml.dyadranking.util;

import java.util.List;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A scaler that can be fit to a certain dataset and then be used to normalize
 * datasets, i.e. transform the data to have a length of 1.
 * 
 * @author Mirko Jürgens
 *
 */
public class DyadUnitIntervalScaler  extends AbstractDyadScaler {

	private static final long serialVersionUID = -6732663643697649308L;
	
	@Override
	public void transformInstances(Dyad dyad, List<Integer> ignoredIndices) {
		for (int i = 0; i < dyad.getInstance().length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = dyad.getInstance().getValue(i);
				if (value != 0.0d)
					value /= Math.sqrt(statsX[i].getSumsq());
				dyad.getInstance().setValue(i, value);
			}
		}
	}

	@Override
	public void transformAlternatives(Dyad dyad, List<Integer> ignoredIndices) {
		for (int i = 0; i < dyad.getAlternative().length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = dyad.getAlternative().getValue(i);
				if (value != 0.0d)
					value /= Math.sqrt(statsY[i].getSumsq());
				dyad.getAlternative().setValue(i, value);
			}
		}		
	}

	@Override
	public void transformInstaceVector(Vector vector, List<Integer> ignoredIndices) {
		for (int i = 0; i < vector.length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = vector.getValue(i);
				if (value != 0.0d)
					value /= Math.sqrt(statsX[i].getSumsq());
				vector.setValue(i, value);
			}
		}		
	}

}
