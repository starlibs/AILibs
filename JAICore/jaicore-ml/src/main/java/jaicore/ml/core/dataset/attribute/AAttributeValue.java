package jaicore.ml.core.dataset.attribute;

/**
 * An abstract class for attribute values implementing basic functionality to
 * store its value as well as getter and setters.
 *
 * @author wever
 *
 * @param <D>
 *            The domain of values.
 */
public abstract class AAttributeValue<D> implements IAttributeValue<D> {

	/** The value of this attribute. */
	private D value;

	/** The attribute type of this attribute value. */
	private final IAttributeType<D> type;

	/**
	 * Constructor creating a new attribute value for a certain type. The value
	 * remains unset.
	 *
	 * @param type
	 *            The type of the attribute value.
	 */
	protected AAttributeValue(final IAttributeType<D> type) {
		super();
		this.type = type;
	}

	/**
	 * Constructor creating a new attribute value for a certain type together with a
	 * value.
	 *
	 * @param type
	 *            The type of the attribute value.
	 * @param value
	 *            The value of this attribute.
	 */
	protected AAttributeValue(final IAttributeType<D> type, final D value) {
		this(type);
		this.setValue(value);
	}

	/**
	 * @return The attribute type of this attribute value.
	 */
	@Override
	public IAttributeType<D> getType() {
		return this.type;
	}

	@Override
	public D getValue() {
		return this.value;
	}

	@Override
	public void setValue(final D value) {
		if (!this.type.isValidValue(value)) {
			throw new IllegalArgumentException("The attribute value does not conform the domain of the attribute type.");
		}
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
		result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		AAttributeValue other = (AAttributeValue) obj;
		if (this.type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!this.type.equals(other.type)) {
			return false;
		}
		if (this.value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!this.value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
