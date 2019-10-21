package ai.libs.jaicore.ml.core.dataset.attributetransformer.multivalue;

import java.util.LinkedList;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.attribute.transformer.ISingleAttributeTransformer;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

import ai.libs.jaicore.ml.core.dataset.attribute.MultiValueAttribute;
import ai.libs.jaicore.ml.core.dataset.attribute.MultiValueAttributeValue;

/**
 * Transforms a multi-valued feature into a 0/1 Vector, where each dimension
 * represents one of the values, i.e. 1 in one dimension => the feature contains
 * this value, 0 in one dimension => the feature does not contain this value.
 *
 * @author Lukas Brandt
 */
public class MultiValueBinarizationTransformer implements ISingleAttributeTransformer {

	@Override
	public double[] transformAttribute(final IAttributeValue attributeToTransform) {
		if (!(attributeToTransform instanceof MultiValueAttributeValue)) {
			throw new IllegalArgumentException("Can only perform Multi-Value Binaryzation for multi-value attributes.");
		}

		// Get the feature value and the type of the feature value.
		MultiValueAttributeValue value = (MultiValueAttributeValue) attributeToTransform;
		IMultiValueAttributeType type = (MultiValueAttribute) value.getType();

		// Created a sorted list of the domain to get a definitive mapping from the
		// values to the dimensions via the index.
		List<String> domain = new LinkedList<>();
		domain.addAll(type.getDomain());
		domain.sort(String::compareTo);

		// Assign a 0 or 1 for each vector dimension
		double[] binaryzation = new double[domain.size()];
		for (int i = 0; i < binaryzation.length; i++) {
			if (value.getValue().contains(domain.get(i))) {
				binaryzation[i] = 1;
			} else {
				binaryzation[i] = 0;
			}
		}

		return binaryzation;
	}

}
