package ai.libs.jaicore.ml.core.dataset.attribute.transformer;

import ai.libs.jaicore.ml.core.dataset.attribute.IAttributeValue;

public interface ISingleAttributeTransformer {

	public double[] transformAttribute(IAttributeValue<?> attributeToTransform);

}
