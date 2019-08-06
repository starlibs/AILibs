package ai.libs.jaicore.ml.core.dataset.sampling;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.api4.java.ai.ml.dataset.INumericFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;

public interface IClusterableInstances<Y> extends INumericFeatureInstance, ILabeledInstance<Y>, Clusterable {

	@Override
	public default double[] getPoint() {
		return toDoubleVector();
	}
}
