package jaicore.ml.core.dataset.attribute.categorical;

import java.util.Set;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * The categorical attribute type describes the domain a value of a respective categorical attribute value stems from.
 *
 * @author wever
 *
 */
public class CategoricalAttributeType implements ICategoricalAttributeType {

	/** The domain defining where values of categorical attributes stem from. */
	private final Set<String> domain;

	/**
	 * Constructor setting the domain of the categorical attribute values.
	 *
	 * @param domain
	 *            The domain categorical values may stem from.
	 */
	public CategoricalAttributeType(final Set<String> domain) {
		this.domain = domain;
	}

	@Override
	public Set<String> getDomain() {
		return this.domain;
	}

	@Override
	public boolean isValidValue(final String value) {
		return this.domain.contains(value);
	}

	@Override
	public IAttributeValue<String> buildAttributeValue(final Object value) {
		return this.buildAttributeValue((String) value);
	}

	@Override
	public IAttributeValue<String> buildAttributeValue(final String stringDescription) {
		return new CategoricalAttributeValue(this, stringDescription);
	}

}
