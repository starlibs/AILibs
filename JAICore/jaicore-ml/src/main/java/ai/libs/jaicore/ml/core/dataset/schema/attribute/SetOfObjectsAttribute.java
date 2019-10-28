package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ISetOfObjectsAttribute;

public class SetOfObjectsAttribute<O> extends ACollectionOfObjectsAttribute<O> implements ISetOfObjectsAttribute<O> {

	private static final long serialVersionUID = 4372755490714119056L;

	public SetOfObjectsAttribute(String name) {
		super(name);
	}

	@Override
	public boolean isValidValue(Object value) {
		asd
	}

	@Override
	public String getStringDescriptionOfDomain() {
		asd
	}

	@Override
	public IAttributeValue getAsAttributeValue(Object object) {
		asd
	}

	@Override
	public double toDouble(Object object) {
		asd
	}

	@Override
	public String serializeAttributeValue(Object value) {
		/asd
	}

	@Override
	public Object deserializeAttributeValue(String string) {
		asd
	}

	@Override
	protected Collection<O> getValueAsTypeInstance(Object object) {
		asd
	}

}
