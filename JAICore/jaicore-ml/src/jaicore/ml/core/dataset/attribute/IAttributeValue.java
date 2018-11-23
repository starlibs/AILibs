package jaicore.ml.core.dataset.attribute;

/**
 * An
 *
 * @author wever
 *
 * @param <D>
 *            The type of an attribute value.
 */
public interface IAttributeValue<D> {

	public D getValue();

	public void setValue(D value);

}
