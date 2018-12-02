package jaicore.ml.core.dataset.attribute.categorical;

import jaicore.ml.core.dataset.attribute.AAttributeValue;

/**
 * Categorical attribute value as it can be part of an instance.
 *
 * @author wever
 */
public class CategoricalAttributeValue extends AAttributeValue<String> {

	/**
	 * Standard c'tor.
	 *
	 * @param type
	 *            The type defining the domain of this categorical attribute.
	 */
	public CategoricalAttributeValue(final ICategoricalAttributeType type) {
		super(type);
	}

	/**
	 * C'tor setting the value of this attribute as well.
	 *
	 * @param type
	 *            The type defining the domain of this categorical attribute.
	 * @param value
	 *            The value which this attribute takes.
	 */
	public CategoricalAttributeValue(final ICategoricalAttributeType type, final String value) {
		super(type, value);
	}

}
