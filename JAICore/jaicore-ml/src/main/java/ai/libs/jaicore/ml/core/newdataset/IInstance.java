package ai.libs.jaicore.ml.core.newdataset;

/**
 * This interface defines additional specifics (complementing api4.ai.ml interfaces) of instances within AILIbs.
 * These requirements are potentially worth forwarding to the api4.ai.ml repository.
 *
 * @author mwever
 *
 */
public interface IInstance extends org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance {

	/**
	 * Sets the label of this instance to a new value as provided as an argument.
	 *
	 * @param obj The new label value for this instance.
	 */
	public void setLabel(Object obj);

	/**
	 * Sets the value of the attribute at position <code>pos</code> to a new value as provided as an argument.
	 * @param pos The position where to replace the current value with the new value.
	 * @param value The new attribute value (to replace the previous value).
	 */
	public void setAttributeValue(final int pos, final Object value);

}
