package jaicore.ml.core.dataset.attribute.categorical;

import java.util.Set;

import jaicore.ml.core.dataset.attribute.IAttributeType;

/**
 * Interface for categorical attribute types.
 *
 * @author wever
 */
public interface ICategoricalAttributeType extends IAttributeType<String> {

	/**
	 * @return The domain of this categorical attribute.
	 */
	public Set<String> getDomain();

}
