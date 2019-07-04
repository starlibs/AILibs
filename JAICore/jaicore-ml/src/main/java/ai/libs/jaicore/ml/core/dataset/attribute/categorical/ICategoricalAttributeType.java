package ai.libs.jaicore.ml.core.dataset.attribute.categorical;

import java.util.List;

import ai.libs.jaicore.ml.core.dataset.attribute.IAttributeType;

/**
 * Interface for categorical attribute types.
 *
 * @author wever
 */
public interface ICategoricalAttributeType extends IAttributeType<String> {

	/**
	 * @return The domain of this categorical attribute.
	 */
	public List<String> getDomain();

}
