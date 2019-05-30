package jaicore.ml.core.dataset;

/**
 * Type intersection interface for numeric instances on one hand and labeled instances on the other hand.
 * 
 * It is on purpose that the intersection is also with LabeledAttributeArrayInstance, because only this way,
 * objects implementing this interface can also be used in placed where ILabeledAttributeArrayInstance objects
 * are required. If this interface would only extend INumericArrayInstance and ILabeledInstance, this would not
 * be possible.
 * 
 * @author fmohr
 *
 */
public interface INumericLabeledAttributeArrayInstance<L> extends INumericArrayInstance, ILabeledAttributeArrayInstance<L> {

}
