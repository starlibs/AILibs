package jaicore.ml.core.dataset.attribute.transformer;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

public interface ISingleAttributeTransformer {

	public double[] transformAttribute(IAttributeValue<?> attributeToTransform);

}
