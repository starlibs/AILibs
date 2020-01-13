package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Collection;

public abstract class ACollectionOfObjectsAttribute<O> extends AGenericObjectAttribute<Collection<O>> {

	private static final long serialVersionUID = 2086475349391194480L;

	protected ACollectionOfObjectsAttribute(final String name) {
		super(name);
	}

}
