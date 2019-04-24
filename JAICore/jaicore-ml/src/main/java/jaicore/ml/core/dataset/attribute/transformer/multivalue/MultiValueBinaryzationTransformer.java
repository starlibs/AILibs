package jaicore.ml.core.dataset.attribute.transformer.multivalue;

import java.util.LinkedList;
import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.multivalue.MultiValueAttributeType;
import jaicore.ml.core.dataset.attribute.multivalue.MultiValueAttributeValue;
import jaicore.ml.core.dataset.attribute.transformer.ISingleAttributeTransformer;

/**
 * Transforms a multi-valued feature into a 0/1 Vector, where each dimension
 * represents one of the values, i.e. 1 in one dimension => the feature contains
 * this value, 0 in one dimension => the feature does not contain this value.
 * 
 * @author Lukas Brandt
 */
public class MultiValueBinaryzationTransformer implements ISingleAttributeTransformer {

	@Override
	public double[] transformAttribute(IAttributeValue<?> attributeToTransform) {
		if (!(attributeToTransform instanceof MultiValueAttributeValue)) {
			throw new IllegalArgumentException("Can only perform Multi-Value Binaryzation for multi-value attributes.");
		}

		// Get the feature value and the type of the feature value.
		MultiValueAttributeValue value = (MultiValueAttributeValue) attributeToTransform;
		MultiValueAttributeType type = (MultiValueAttributeType) value.getType();

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
