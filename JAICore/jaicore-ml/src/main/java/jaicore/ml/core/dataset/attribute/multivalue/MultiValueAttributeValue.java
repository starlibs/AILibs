package jaicore.ml.core.dataset.attribute.multivalue;

import java.util.Collection;

import jaicore.ml.core.dataset.attribute.AAttributeValue;

/**
 * Multi-value attribute value as it can be part of an instance.
 *
 * @author wever
 */
public class MultiValueAttributeValue extends AAttributeValue<Collection<String>> {

	/**
	 * Standard c'tor.
	 *
	 * @param type
	 *            The type defining the domain of this multi-value attribute.
	 */
	public MultiValueAttributeValue(final IMultiValueAttributeType type) {
		super(type);
	}

	/**
	 * C'tor setting the value of this attribute as well.
	 *
	 * @param type
	 *            The type defining the domain of this multi-value attribute.
	 * @param value
	 *            The value this attribute takes.
	 */
	public MultiValueAttributeValue(final IMultiValueAttributeType type, final Collection<String> value) {
		super(type, value);
	}

}
