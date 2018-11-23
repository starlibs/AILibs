package jaicore.ml.core.dataset.attribute;

/**
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

	protected AAttributeValue(final IAttributeType<D> type) {
		super();
		this.type = type;
	}

	protected AAttributeValue(final IAttributeType<D> type, final D value) {
		this(type);
		this.setValue(value);
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
	}

}
