package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Set;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ISetOfObjectsAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ISetOfObjectsAttributeValue;

public class SetOfObjectsAttributeValue<O> implements ISetOfObjectsAttributeValue<O> {

	private Set<O> setOfObjects;
	private ISetOfObjectsAttribute<O> attribute;

	public SetOfObjectsAttributeValue(Set<O> setOfObjects, ISetOfObjectsAttribute<O> attribute) {
		super();
		this.setOfObjects = setOfObjects;
		this.attribute = attribute;
	}

	@Override
	public IAttribute getAttribute() {
		return attribute;
	}

	@Override
	public Set<O> getValue() {
		return setOfObjects;
	}

}
