package jaicore.ml.core.dataset.attribute.categorical;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * The categorical attribute type describes the domain a value of a respective categorical attribute value stems from.
 *
 * @author wever
 *
 */
public class CategoricalAttributeType implements ICategoricalAttributeType {

	/**
	 *
	 */
	private static final long serialVersionUID = -1258014436188830057L;

	/** The domain defining where values of categorical attributes stem from. */
	private final List<String> domain;

	/**
	 * Constructor setting the domain of the categorical attribute values.
	 *
	 * @param domain
	 *            The domain categorical values may stem from.
	 */
	public CategoricalAttributeType(final List<String> domain) {
		if (new HashSet<>(domain).size() != domain.size()) {
			throw new IllegalArgumentException("Domain contains duplicate values");
		}
		this.domain = new LinkedList<>(domain);
	}

	@Override
	public List<String> getDomain() {
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

	@Override
	public String toString() {
		return "CAT:" + this.domain.toString();
	}

}
