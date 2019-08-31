package ai.libs.jaicore.ml.core.tabular.dataset.attribute;

import java.util.Collection;

public abstract class ACollectionOfObjectsAttribute<O> extends AGenericObjectAttribute<Collection<O>> {

	/**
	 *
	 */
	private static final long serialVersionUID = 2086475349391194480L;

	protected ACollectionOfObjectsAttribute(final String name) {
		super(name);
	}

}
