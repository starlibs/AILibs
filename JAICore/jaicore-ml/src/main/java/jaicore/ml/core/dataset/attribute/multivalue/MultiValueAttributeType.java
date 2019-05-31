package jaicore.ml.core.dataset.attribute.multivalue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * The multi-value attribute type describes the domain a value of a respective multi-value attribute value stems from.
 *
 * @author wever
 *
 */
public class MultiValueAttributeType implements IMultiValueAttributeType {

	/** The domain defining where values of multi-value attributes stem from. */
	private final Set<String> domain;

	/**
	 * Constructor setting the domain of the multi-value attribute values.
	 *
	 * @param domain
	 *            The domain multi-value values may stem from.
	 */
	public MultiValueAttributeType(final Set<String> domain) {
		this.domain = domain;
	}

	@Override
	public Set<String> getDomain() {
		return this.domain;
	}

	@Override
	public boolean isValidValue(final Collection<String> value) {
		return this.domain.containsAll(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAttributeValue<Collection<String>> buildAttributeValue(final Object value) {
		return new MultiValueAttributeValue(this, (Set<String>) value);
	}

	@Override
	public IAttributeValue<Collection<String>> buildAttributeValue(final String stringDescription) {
		Set<String> value = Arrays.stream(stringDescription.split(",")).collect(Collectors.toSet());
		return this.buildAttributeValue(value);
	}

}
