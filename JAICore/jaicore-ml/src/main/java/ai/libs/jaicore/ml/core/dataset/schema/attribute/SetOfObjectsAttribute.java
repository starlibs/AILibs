package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ISetOfObjectsAttribute;

public class SetOfObjectsAttribute<O> extends ACollectionOfObjectsAttribute<O> implements ISetOfObjectsAttribute<O> {

	private static final long serialVersionUID = 4372755490714119056L;

	public SetOfObjectsAttribute(final String name) {
		super(name);
	}

	@Override
	public boolean isValidValue(final Object value) {
		// TODO implement me
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public String getStringDescriptionOfDomain() {
		// TODO implement me
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public IAttributeValue getAsAttributeValue(final Object object) {
		// TODO implement me
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public double toDouble(final Object object) {
		// TODO implement me
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		// TODO implement me
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		// TODO implement me
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	protected Collection<O> getValueAsTypeInstance(final Object object) {
		// TODO implement me
		throw new UnsupportedOperationException("Not yet implemented!");
	}

}
