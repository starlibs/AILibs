package jaicore.ml.core.dataset.attribute.multivalue;

import java.util.Collection;
import java.util.Set;

import jaicore.ml.core.dataset.attribute.IAttributeType;

/**
 * Interface for categorical attribute types.
 *
 * @author wever
 */
public interface IMultiValueAttributeType extends IAttributeType<Collection<String>> {

	/**
	 * @return The domain of this multi-value attribute.
	 */
	public Set<String> getDomain();

}
