package ai.libs.jaicore.ml.core.dataset.sampling;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.api4.java.ai.ml.core.dataset.ContainsNonNumericAttributesException;
import org.api4.java.ai.ml.core.dataset.INumericLabeledAttributeArrayInstance;

public interface IClusterableInstances<L> extends INumericLabeledAttributeArrayInstance<L>, Clusterable {

	@Override
	public default double[] getPoint() {
		try {
			return getAsDoubleVector();
		} catch (ContainsNonNumericAttributesException e) {
			e.printStackTrace();
			return null;
		}
	}
}
